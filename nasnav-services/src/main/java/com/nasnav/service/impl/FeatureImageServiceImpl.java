package com.nasnav.service.impl;

import com.nasnav.commons.utils.FunctionalUtils;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.service.FeatureImageService;
import com.nasnav.service.FeaturesService;
import com.nasnav.service.FileService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.helpers.ProductImagesImportHelper;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.Valid;
import java.util.*;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.enumerations.ProductFeatureType.IMG_SWATCH;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.util.FileUtils.validateImgBulkZip;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class FeatureImageServiceImpl implements FeatureImageService {
	private final FeaturesService featuresService;
	private final ProductImagesImportHelper helper;
	private final ProductVariantsRepository productVariantsRepository;
	private final FileService fileService;
	private final SecurityService securityService;
	private final ProductFeaturesRepository featureRepo;
	private final ProductExtraAttributesEntityRepository attrValuesRepo;
	private final ExtraAttributesRepository attrRepo;

	@Override
	public void saveSwatchImagesBulk(Set<ImportedSwatchImage> importedImgs, SwatchImageBulkUpdateDTO metaData) {
		if (metaData.isDeleteOldImages()) {
			deleteFeatureSwatchImages(metaData);
		}
		doSaveSwatchImagesBulk(importedImgs, metaData);
	}

	@Override
	public void updateSwatchImagesBulk(
			@Valid MultipartFile zip, @Valid MultipartFile csv, @Valid SwatchImageBulkUpdateDTO metaData) {
		validateSwatchImageBulkRequest(zip, metaData);
		Set<ImportedSwatchImage> importedImgs = extractSwatchImgsToImport(zip, csv, metaData);
		saveSwatchImagesBulk(importedImgs, metaData);
	}

	private Set<ImportedSwatchImage> extractSwatchImgsToImport(MultipartFile zip, MultipartFile csv,
			SwatchImageBulkUpdateDTO metaData) {
		return helper.extractImgsToImport(zip, csv, new ProductImageBulkUpdateDTO(metaData.isIgnoreErrors()))
				.stream()
				.map(img -> new ImportedSwatchImage(img, metaData.getFeatureId()))
				.collect(toSet());
	}

	private void validateSwatchImageBulkRequest(MultipartFile zip, SwatchImageBulkUpdateDTO metaData) {
		validateSwatchBulkMetaData(metaData);
		validateImgBulkZip(zip);
	}

	private void doSaveSwatchImagesBulk(Set<ImportedSwatchImage> importedImgs, SwatchImageBulkUpdateDTO metaData) {
		SwatchDataCache cache = createSwatchDataCache(importedImgs, metaData);
		List<ProductExtraAttributesEntity> extraEntities = importedImgs
				.stream()
				.map(e -> saveSwatchFile(e, metaData.isCrop()))
				.map(saved -> createExtraAttrValueEntity(saved, cache))
				.collect(toList());
		attrValuesRepo.saveAll(extraEntities);
	}

	private SwatchDataCache createSwatchDataCache(Set<ImportedSwatchImage> importedImgs,
			SwatchImageBulkUpdateDTO metaData) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		ExtraAttributesEntity attrEntity = getExtraAttributesEntity(metaData);
		Map<Long, ProductExtraAttributesEntity> existingAttrValues = createAttrValuesCache(attrEntity);
		Map<Long, ProductVariantsEntity> variants = createVariantsCache(importedImgs, orgId);
		return new SwatchDataCache(existingAttrValues, variants, attrEntity);
	}

	private void validateSwatchBulkMetaData(SwatchImageBulkUpdateDTO metaData) {

		if (anyIsNull(metaData, metaData.getFeatureId())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$IMG$0003);
		}
		ProductFeaturesEntity feature = getProductFeaturesEntity(metaData);
		if (!Objects.equals(IMG_SWATCH.getValue(), feature.getType())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$IMG$0008, IMG_SWATCH.name());
		}
	}

	private ProductFeaturesEntity getProductFeaturesEntity(SwatchImageBulkUpdateDTO metaData) {
		Long featureId = metaData.getFeatureId();
		Long orgId = securityService.getCurrentUserOrganizationId();
		return featureRepo
				.findByIdAndOrganization_Id(featureId.intValue(), orgId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, P$IMG$0004, featureId));
	}

	private Map<Long, ProductExtraAttributesEntity> createAttrValuesCache(ExtraAttributesEntity attrEntity) {
		return attrValuesRepo
				.findByExtraAttribute(attrEntity)
				.stream()
				.collect(toMap(val -> val.getVariant().getId(), val -> val, FunctionalUtils::getFirst));
	}

	private Map<Long, ProductVariantsEntity> createVariantsCache(Set<ImportedSwatchImage> importedImgs, Long orgId) {
		return importedImgs
				.stream()
				.map(ImportedSwatchImage::getVariantId)
				.collect(
						collectingAndThen(toList(),
								ids -> productVariantsRepository.findByIdInAndProductEntity_OrganizationId(ids, orgId)))
				.stream()
				.collect(toMap(ProductVariantsEntity::getId, FunctionalUtils::self, FunctionalUtils::getFirst));
	}

	private ProductExtraAttributesEntity createExtraAttrValueEntity(
			SavedImportedSwatchImage saved, SwatchDataCache cache) {
		Long variantId = saved.getVariantId();
		ProductVariantsEntity variant = ofNullable(cache.getVariants().get(saved.getVariantId()))
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, P$VAR$0001, variantId));
		ProductExtraAttributesEntity valueEntity = ofNullable(cache.getExistingAttrValues().get(saved.getVariantId()))
				.orElseGet(ProductExtraAttributesEntity::new);
		valueEntity.setExtraAttribute(cache.getAttrEntity());
		valueEntity.setValue(saved.getUrl());
		valueEntity.setVariant(variant);
		return valueEntity;
	}

	private ExtraAttributesEntity getExtraAttributesEntity(SwatchImageBulkUpdateDTO metaData) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return getExtraAttrId(metaData)
				.flatMap(id -> attrRepo.findByIdAndOrganizationId(id, orgId))
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, P$IMG$0009, metaData.getFeatureId()));
	}

	private Optional<Integer> getExtraAttrId(SwatchImageBulkUpdateDTO metaData) {
		ProductFeaturesEntity feature = getProductFeaturesEntity(metaData);
		return featuresService.getAdditionalDataExtraAttrId(feature);
	}

	private SavedImportedSwatchImage saveSwatchFile(ImportedSwatchImage swatch, boolean crop) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		String url = fileService.saveFile(swatch.getImage(), orgId, crop);
		return new SavedImportedSwatchImage(swatch, url);
	}

	private void deleteFeatureSwatchImages(SwatchImageBulkUpdateDTO metaData) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		Integer id = getExtraAttrId(metaData).orElse(-1);
		attrValuesRepo.deleteByIdAndOrganizationId(id, orgId);
	}
}
