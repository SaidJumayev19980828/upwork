package com.nasnav.service.impl;

import static com.nasnav.cache.Caches.ORGANIZATIONS_EXTRA_ATTRIBUTES;
import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.commons.utils.StringUtils.encodeUrl;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.enumerations.ExtraAttributeType.INVISIBLE;
import static com.nasnav.enumerations.ExtraAttributeType.getExtraAttributeType;
import static com.nasnav.enumerations.ProductFeatureType.COLOR;
import static com.nasnav.enumerations.ProductFeatureType.IMG_SWATCH;
import static com.nasnav.enumerations.ProductFeatureType.STRING;
import static com.nasnav.exceptions.ErrorCodes.G$PRAM$0001;
import static com.nasnav.exceptions.ErrorCodes.GEN$0022;
import static com.nasnav.exceptions.ErrorCodes.ORG$EXTRATTR$0001;
import static com.nasnav.exceptions.ErrorCodes.ORG$FTR$0001;
import static com.nasnav.exceptions.ErrorCodes.ORG$FTR$0002;
import static com.nasnav.exceptions.ErrorCodes.ORG$FTR$0003;
import static com.nasnav.exceptions.ErrorCodes.P$FTR$0001;
import static com.nasnav.exceptions.ErrorCodes.P$FTR$0002;
import static com.nasnav.exceptions.ErrorCodes.P$PRO$0007;
import static com.nasnav.exceptions.ErrorCodes.P$VAR$008;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static java.util.Arrays.asList;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.cache.annotation.CacheResult;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.ExtraAttributesRepository;
import com.nasnav.dao.ProductExtraAttributesEntityRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.VariantFeatureValuesRepository;
import com.nasnav.dto.ExtraAttributeDTO;
import com.nasnav.dto.ExtraAttributeDefinitionDTO;
import com.nasnav.dto.ExtraAttributesRepresentationObject;
import com.nasnav.dto.ProductFeatureDTO;
import com.nasnav.dto.ProductFeatureUpdateDTO;
import com.nasnav.enumerations.ExtraAttributeType;
import com.nasnav.enumerations.ProductFeatureType;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.ExtraAttributesEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.response.ProductFeatureUpdateResponse;
import com.nasnav.service.FeaturesService;
import com.nasnav.service.ProductService;
import com.nasnav.service.SecurityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeaturesServiceImpl implements FeaturesService {
	private static  final String EXTRA_ATTRIBUTE_ID = "extra_attribute_id";
	private static final Set<Integer> FEATURE_TYPE_WITH_EXTRA_DATA = setOf(IMG_SWATCH.getValue(), COLOR.getValue());
	private final VariantFeatureValuesRepository variantFeatureValuesRepo;
	private final ProductFeaturesRepository featureRepo;
	private final ExtraAttributesRepository extraAttributesRepository;
	private final ProductExtraAttributesEntityRepository productExtraAttrRepo;
	private final SecurityService securityService;
	private final ProductService productService;

	@Override
	public List<ProductFeatureType> getProductFeatureTypes() {
		return asList(ProductFeatureType.values());
	}

	private Optional<ExtraAttributesEntity> createExtraAttributesIfNeeded(ProductFeaturesEntity entity) {
		Integer type = entity.getType();
		if (nonNull(type) && FEATURE_TYPE_WITH_EXTRA_DATA.contains(type)) {
			return getSavedExtraAttrInFeatureConfig(entity)
					.or(() -> findExistingExtraAttrInDb(entity))
					.or(() -> doCreateExtraAttribute(entity));
		}
		return Optional.empty();
	}

	private ProductFeaturesEntity saveFeatureToDb(ProductFeatureUpdateDTO featureDto, OrganizationEntity org) {
		ProductFeaturesEntity entity = new ProductFeaturesEntity();
		entity.setOrganization(org);

		Operation opr = featureDto.getOperation();

		if (opr.equals(Operation.UPDATE)) {
			entity = featureRepo.findByIdAndOrganization_Id(featureDto.getFeatureId(), org.getId())
					.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$FTR$0003, featureDto.getFeatureId()));
		}
		if (featureDto.isUpdated("name")) {
			entity.setName(featureDto.getName());
		}

		setPnameOrGenerateDefault(featureDto, entity, opr);

		if (featureDto.isUpdated("description")) {
			entity.setDescription(featureDto.getDescription());
		}
		if (featureDto.isUpdated("level")) {
			if (opr.equals(Operation.CREATE) && featureDto.getLevel() == 0) {
				entity.setLevel(0);
			} else {
				entity.setLevel(featureDto.getLevel());
			}
		}
		if (featureDto.isUpdated("type")) {
			var type = ofNullable(featureDto.getType()).orElse(STRING).getValue();
			entity.setType(type);
			var attr = createExtraAttributesIfNeeded(entity);
			if (attr.isPresent()) {
				addExtraAttrToFeatureExtraData(entity, attr.get());
			}
		}

		return featureRepo.save(entity);
	}

	@Override
	@Transactional
	public ProductFeatureUpdateResponse updateProductFeature(ProductFeatureUpdateDTO featureDto) {
		OrganizationEntity org = securityService.getCurrentUserOrganization();
		Long orgId = org.getId();

		validateProductFeature(featureDto, orgId);
		ProductFeaturesEntity entity = saveFeatureToDb(featureDto, org);

		return new ProductFeatureUpdateResponse(entity.getId());
	}

	@Override
	public List<ProductFeatureDTO> getProductFeatures(Long orgId) {
		return featureRepo
				.findByOrganizationId(orgId)
				.stream()
				.map(this::toProductFeatureDTO)
				.collect(toList());
	}

	private ProductFeatureDTO toProductFeatureDTO(ProductFeaturesEntity entity) {
		ProductFeatureType type = ProductFeatureType
				.getProductFeatureType(entity.getType())
				.orElse(STRING);
		Map<String, ?> extraData = ofNullable(entity.getExtraData())
				.map(JSONObject::new)
				.map(JSONObject::toMap)
				.orElse(emptyMap());
		ProductFeatureDTO dto = new ProductFeatureDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setPname(entity.getPname());
		dto.setDescription(entity.getDescription());
		dto.setLevel(entity.getLevel());
		dto.setType(type);
		dto.setExtraData(extraData);
		return dto;
	}

	private void doRemoveProductFeature(ProductFeaturesEntity feature) {
		variantFeatureValuesRepo.deleteByFeature_Id(feature.getId());
		featureRepo.delete(feature);
	}

	private void validateProductFeatureToDelete(ProductFeaturesEntity feature) {
		if (variantsHasTheFeature(feature)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$FTR$0002, feature.getId());
		}
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void removeProductFeature(Integer featureId) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		ProductFeaturesEntity feature = featureRepo
				.findByIdAndOrganization_Id(featureId, orgId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, P$FTR$0001, featureId));
		validateProductFeatureToDelete(feature);
		doRemoveProductFeature(feature);
	}

	private boolean variantsHasTheFeature(ProductFeaturesEntity feature) {
		return !productService.getVariantsWithFeature(feature).isEmpty();
	}

	private Optional<ExtraAttributesEntity> findExistingExtraAttrInDb(ProductFeaturesEntity entity) {
		var orgId = securityService.getCurrentUserOrganizationId();
		String name = getAdditionalDataExtraAttrName(entity);
		return extraAttributesRepository.findByNameAndOrganizationId(name, orgId);
	}

	private Optional<ExtraAttributesEntity> getSavedExtraAttrInFeatureConfig(ProductFeaturesEntity entity) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return getAdditionalDataExtraAttrId(entity)
				.flatMap(id -> extraAttributesRepository.findByIdAndOrganizationId(id, orgId));
	}

	private Optional<ExtraAttributesEntity> doCreateExtraAttribute(ProductFeaturesEntity entity) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		String name = getAdditionalDataExtraAttrName(entity);

		ExtraAttributesEntity attr = new ExtraAttributesEntity();
		attr.setType(INVISIBLE.getValue());
		attr.setName(name);
		attr.setOrganizationId(orgId);
		return Optional.of(extraAttributesRepository.save(attr));
	}

	private void addExtraAttrToFeatureExtraData(ProductFeaturesEntity entity, ExtraAttributesEntity attr) {
		String featureExtraDataBefore = ofNullable(entity.getExtraData()).orElse("{}");
		String featureExtraDataAfter = new JSONObject(featureExtraDataBefore)
				.put(EXTRA_ATTRIBUTE_ID, attr.getId())
				.toString();
		entity.setExtraData(featureExtraDataAfter);
	}

	@Override
	public Optional<Integer> getAdditionalDataExtraAttrId(ProductFeaturesEntity feature) {
		return ofNullable(feature.getExtraData())
				.map(JSONObject::new)
				.filter(json -> json.has(EXTRA_ATTRIBUTE_ID))
				.map(json -> json.getInt(EXTRA_ATTRIBUTE_ID));
	}

	@Override
	@CacheResult(cacheName = ORGANIZATIONS_EXTRA_ATTRIBUTES)
	public List<ExtraAttributesRepresentationObject> getOrganizationExtraAttributesById(Long organizationId) {
		List<ExtraAttributesEntity> extraAttributes;
		if (organizationId == null) {
			extraAttributes = extraAttributesRepository.findAll();
		} else {
			extraAttributes = extraAttributesRepository.findByOrganizationId(organizationId);
		}

		return extraAttributes.stream()
				.map(extraAttribute -> (ExtraAttributesRepresentationObject) extraAttribute.getRepresentation())
				.collect(toList());
	}

	private Integer createExtraAttribute(ExtraAttributeDTO extraAttrDTO) {
		validateDTORequiredFields(extraAttrDTO);

		Long orgId = securityService.getCurrentUserOrganizationId();

		ExtraAttributesEntity extraAttrEntity = new ExtraAttributesEntity();
		extraAttrEntity.setOrganizationId(orgId);
		setExtraAttributesEntityFromDTO(extraAttrEntity, extraAttrDTO);

		return extraAttributesRepository.save(extraAttrEntity).getId();
	}

	@Override
	public Integer createUpdateExtraAttributes(ExtraAttributeDTO extraAttrDTO, String operation) {

		if (operation.equalsIgnoreCase("create"))
			return createExtraAttribute(extraAttrDTO);
		else if (operation.equalsIgnoreCase("update"))
			return updateExtraAttributes(extraAttrDTO);
		else
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0007);
	}

	private Integer updateExtraAttributes(ExtraAttributeDTO extraAttrDTO) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		Integer attrId = extraAttrDTO.getId();

		ExtraAttributesEntity extraAttrEntity = extraAttributesRepository
				.findByIdAndOrganizationId(attrId, orgId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$EXTRATTR$0001, attrId));

		setExtraAttributesEntityFromDTO(extraAttrEntity, extraAttrDTO);

		return extraAttributesRepository.save(extraAttrEntity).getId();
	}

	private void validateProductFeatureForCreate(ProductFeatureUpdateDTO featureDto, Long orgId) {
		if (!featureDto.areRequiredForCreatePropertiesProvided().booleanValue()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0022);
		}
		if (isBlankOrNull(featureDto.getName())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$FTR$0001);
		}
		if (featureRepo.existsByNameAndOrganizationId(featureDto.getName(), orgId)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$FTR$0002);
		}
	}

	@Override
	public void deleteExtraAttribute(Integer attrId) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		if (!extraAttributesRepository.existsByIdAndOrganizationId(attrId, orgId))
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$EXTRATTR$0001, attrId);

		productExtraAttrRepo.deleteByIdAndOrganizationId(attrId, orgId);

		extraAttributesRepository.deleteByIdAndOrganizationId(attrId, orgId);
	}

	@Override
	public List<ExtraAttributeDefinitionDTO> getExtraAttributes() {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return extraAttributesRepository
				.findByOrganizationId(orgId)
				.stream()
				.map(this::createExtraAttributeDTO)
				.collect(toList());
	}

	private void validateProductFeature(ProductFeatureUpdateDTO featureDto, Long orgId) {
		if (!featureDto.areRequiredAlwaysPropertiesPresent().booleanValue()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0022);
		}

		Operation opr = featureDto.getOperation();
		validateOperation(featureDto.getOperation());

		if (opr.equals(Operation.CREATE)) {
			validateProductFeatureForCreate(featureDto, orgId);
		} else if (opr.equals(Operation.UPDATE)) {
			validateProductFeatureForUpdate(featureDto);
		}
	}

	private void setPnameOrGenerateDefault(ProductFeatureUpdateDTO featureDto, ProductFeaturesEntity entity,
			Operation opr) {

		if (featureDto.isUpdated("pname") && !isBlankOrNull(featureDto.getPname())) {
			entity.setPname(featureDto.getPname());
		} else if (opr.equals(Operation.CREATE)) {
			String defaultPname = encodeUrl(featureDto.getName());
			entity.setPname(defaultPname);
		}

	}

	private void validateOperation(Operation opr) {
		if (opr == null) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, "operation");
		}
		if (!opr.equals(Operation.CREATE) && !opr.equals(Operation.UPDATE)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0007);
		}
	}

	private void validateProductFeatureForUpdate(ProductFeatureUpdateDTO featureDto) {
		if (!featureDto.areRequiredForUpdatePropertiesProvided().booleanValue()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0022);
		}
		if (featureDto.isUpdated("name") && isBlankOrNull(featureDto.getName())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$FTR$0001);
		}
	}

	private ExtraAttributeDefinitionDTO createExtraAttributeDTO(ExtraAttributesEntity entity) {
		ExtraAttributeType type = getExtraAttributeType(entity.getType())
				.orElse(ExtraAttributeType.STRING);
		Boolean invisible = Objects.equals(type, INVISIBLE);
		ExtraAttributeDefinitionDTO dto = new ExtraAttributeDTO();
		dto.setIconUrl(entity.getIconUrl());
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setType(type);
		dto.setInvisible(invisible);
		return dto;
	}

	private void validateDTORequiredFields(ExtraAttributeDTO extraAttrDTO) {
		ExtraAttributeType defaultType = ExtraAttributeType.STRING;

		if (extraAttrDTO.getType() == null)
			extraAttrDTO.setType(defaultType);

		if(Objects.isNull(extraAttrDTO.getName())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$VAR$008);
		}

	}

	private void setExtraAttributesEntityFromDTO(ExtraAttributesEntity extraAttrEntity, ExtraAttributeDTO extraAttrDTO) {
		String attrName = extraAttrDTO.getName();
		String attrIconUrl = extraAttrDTO.getIconUrl();
		ExtraAttributeType attrType = extraAttrDTO.getType();

		ofNullable(attrName)
				.ifPresent(extraAttrEntity::setName);

		ofNullable(attrIconUrl)
				.ifPresent(extraAttrEntity::setIconUrl);

		ofNullable(attrType)
				.map(ExtraAttributeType::getValue)
				.ifPresent(extraAttrEntity::setType);
	}

	@Override
	public String getAdditionalDataExtraAttrName(ProductFeaturesEntity feature) {
		String typeName = ofNullable(feature.getType())
				.flatMap(ProductFeatureType::getProductFeatureType)
				.map(ProductFeatureType::name)
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, P$FTR$0001, feature.getType()));
		return format("$%s$%s", feature.getPname(), typeName);
	}
}
