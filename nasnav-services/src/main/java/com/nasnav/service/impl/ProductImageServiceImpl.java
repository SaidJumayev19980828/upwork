package com.nasnav.service.impl;

import com.nasnav.commons.utils.FunctionalUtils;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportImageBulkRuntimeException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.ProductImageDeleteResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.service.FeaturesService;
import com.nasnav.service.FileService;
import com.nasnav.service.OrganizationService;
import com.nasnav.service.ProductImageService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.helpers.CachingHelper;
import com.nasnav.service.helpers.ProductImagesImportHelper;
import com.nasnav.service.model.*;
import com.querydsl.sql.SQLQueryFactory;
import com.sun.istack.logging.Logger;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.netty.http.client.HttpClient;

import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nasnav.commons.utils.StringUtils.*;
import static com.nasnav.constatnts.EntityConstants.Operation.CREATE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.*;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.querydsl.sql.QProductImages.productImages;
import static com.nasnav.querydsl.sql.QProductVariants.productVariants;
import static com.nasnav.querydsl.sql.QProducts.products;
import static com.nasnav.util.FileUtils.validateImgBulkZip;
import static com.querydsl.core.types.dsl.Expressions.cases;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.logging.Level.SEVERE;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;



@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

	private static final int IMG_DOWNLOAD_TIMEOUT_SEC = 90;

	private final ProductImagesImportHelper helper;

	private Logger logger = Logger.getLogger(ProductServiceImpl.class);
	
	@Autowired
	private  ProductRepository productRepository;

	@Autowired
	private  ProductImagesRepository productImagesRepository;

	
	@Autowired
	private  ProductVariantsRepository productVariantsRepository;

	
	@Autowired
	private  FileService fileService;
	
	
	@Autowired
	private EmployeeUserRepository empRepo;
	
	@Autowired
	private SecurityService securityService;
	

	@Autowired
	private CachingHelper cachingHelper;
	
	@Autowired
	private JdbcTemplate template;
	
	@Autowired
	private SQLQueryFactory queryFactory;

	@Override
	public ProductImageUpdateResponse updateProductImage(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException {
		validateProductImg(file, imgMetaData);
		
		return saveProductImg(file, imgMetaData);
	}
	
	
	
	


	/**
	 * assert all required parameters are validated before calling this, so we don't validate twice and
	 * validation should be centralized !
	 * if something fails here due to invalid data , then the validation should be fixed!
	 * */
	private ProductImageUpdateResponse saveProductImg(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException {
		Operation opr = imgMetaData.getOperation();
		
		if(opr.equals(Operation.CREATE))
			return saveNewProductImg(file, imgMetaData);
		else
			return saveUpdatedProductImg(file, imgMetaData);
	}




	private ProductImageUpdateResponse saveUpdatedProductImg(MultipartFile file, ProductImageUpdateDTO imgMetaData) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		BaseUserEntity user =  empRepo.getOneByEmail(auth.getName());		
		
		
		Long imgId = imgMetaData.getImageId();		
		ProductImagesEntity entity = productImagesRepository.findById(imgId).get();
		

		String url = null;
		String oldUrl = null;
		if(file != null && !file.isEmpty()) {
			 url = fileService.saveFile(file, user.getOrganizationId());
			 oldUrl = entity.getUri();
		}
		if(url != null)
			entity.setUri(url);
		
		//to update a value , it should be already present in the JSON		
		if(imgMetaData.isUpdated("priority"))
			entity.setPriority( imgMetaData.getPriority() );
		
		if(imgMetaData.isUpdated("type"))
			entity.setType( imgMetaData.getType() );
		
		if(imgMetaData.isUpdated("productId")) {
			Long productId = imgMetaData.getProductId();
			Optional<ProductEntity> productEntity = productRepository.findById( productId );
			entity.setProductEntity(productEntity.get());
		}
		
		if(imgMetaData.isUpdated("variantId")) {
			Optional.ofNullable( imgMetaData.getVariantId() )
					.flatMap(productVariantsRepository::findById)
					.ifPresent(entity::setProductVariantsEntity);
		}		
		
		entity = productImagesRepository.save(entity);
		
		if(url != null && oldUrl != null) {
			fileService.deleteFileByUrl(oldUrl);
		}
		
		return new ProductImageUpdateResponse(entity.getId(), url);
	}




	private ProductImageUpdateResponse saveNewProductImg(MultipartFile file, ProductImageUpdateDTO imgMetaData)
			throws BusinessException {
		List<ProductImageUpdateDTO> metaDataList = Arrays.asList(imgMetaData);
		
		return saveNewProductImgsUsingSameUrl(file, metaDataList)
					.stream()
					.findFirst()
					.orElseThrow(() -> new BusinessException(ERR_NO_IMG_IMPORT_RESPONSE, ERR_NO_IMG_IMPORT_RESPONSE, INTERNAL_SERVER_ERROR));
	}
	
	
	
	
	
	
	private List<ProductImageUpdateResponse> saveNewProductImgsUsingSameUrl(MultipartFile file, List<ProductImageUpdateDTO> imgMetaDataList)
			throws BusinessException {
		if(imgMetaDataList == null || imgMetaDataList.isEmpty()) {
			throw new BusinessException(ERR_NO_IMG_DATA_PROVIDED, ERR_NO_IMG_DATA_PROVIDED, NOT_ACCEPTABLE);
		}
		
		for(ProductImageUpdateDTO metaData: imgMetaDataList) {
			validateProductImg(file, metaData);
		}
		
		String url = saveFile(file);
		
		List<ProductImageUpdateResponse> responses = new ArrayList<>();
		for(ProductImageUpdateDTO metaData: imgMetaDataList) {
			responses.add( saveNewProductImgMetaData(metaData, url) );
		}
		
		return responses;
	}

	private String saveFile(MultipartFile file) {
		Long userOrgId =  securityService.getCurrentUserOrganizationId();		
		return fileService.saveFile(file, userOrgId);
	}

	private ProductImageUpdateResponse saveNewProductImgMetaData(ProductImageUpdateDTO imgMetaData, String url) {
		Long imgId = saveProductImgToDB(imgMetaData, url);		
		return new ProductImageUpdateResponse(imgId, url);
	}




	private Long saveProductImgToDB(ProductImageUpdateDTO imgMetaData, String uri) {
		Long productId = imgMetaData.getProductId();
		ProductEntity productEntity = productRepository.findById( productId ).get();

		List<ProductImagesEntity> imagesEntities = new ArrayList<>();

		if (imgMetaData.getVariantId() != null) {
			imagesEntities.addAll(saveImageToAllVariantsWithHighLevelFeatures(imgMetaData, productEntity, uri));
			if (imagesEntities.isEmpty()) {
				ProductVariantsEntity variant = productVariantsRepository.findById( imgMetaData.getVariantId() ).get();
				ProductImagesEntity entity = new ProductImagesEntity();
				entity.setPriority(imgMetaData.getPriority());
				entity.setProductEntity(productEntity);
				entity.setType(imgMetaData.getType());
				entity.setUri(uri);
				entity.setProductVariantsEntity(variant);
				imagesEntities.add(entity);
			}
		} else {
			ProductImagesEntity entity = new ProductImagesEntity();
			entity.setPriority(imgMetaData.getPriority());
			entity.setProductEntity(productEntity);
			entity.setType(imgMetaData.getType());
			entity.setUri(uri);
			imagesEntities.add(entity);
		}
		productImagesRepository.saveAll(imagesEntities);
		return imagesEntities.get(0).getId();
	}

	private List<ProductImagesEntity> saveImageToAllVariantsWithHighLevelFeatures(ProductImageUpdateDTO imgMetaData,
																				  ProductEntity productEntity,
																				  String uri) {
		List<ProductImagesEntity> imagesEntities = new ArrayList<>();
		ProductVariantsEntity variant = productVariantsRepository.findByVariantId(imgMetaData.getVariantId());
		Map<ProductFeaturesEntity, String> mainFeatures = variant.getFeatureValues()
				.stream()
				.filter(v -> v.getFeature().getLevel() > 0)
				.collect(toMap(VariantFeatureValueEntity::getFeature, VariantFeatureValueEntity::getValue));
		for (ProductVariantsEntity v : productEntity.getProductVariants()) {
			boolean include = false;
			for (VariantFeatureValueEntity value : v.getFeatureValues()) {
				if (mainFeatures.get(value.getFeature()) != null && mainFeatures.get(value.getFeature()).equals(value.getValue())) {
					include = true;
					break;
				}
			}
			if (include) {
				ProductImagesEntity entity = new ProductImagesEntity();
				entity.setPriority(imgMetaData.getPriority());
				entity.setProductEntity(productEntity);
				entity.setType(imgMetaData.getType());
				entity.setUri(uri);
				entity.setProductVariantsEntity(v);
				imagesEntities.add(entity);
			}
		}
		return imagesEntities;
	}


	private void validateProductImg(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException {
		if(imgMetaData == null)
			throw new BusinessException("No Metadata provided for product image!", "INVALID PARAM", NOT_ACCEPTABLE);
		
		if(!imgMetaData.isRequiredPropertyProvided("operation"))
			throw new BusinessException("No operation provided!", "INVALID PARAM:operation", NOT_ACCEPTABLE);
					
		
		if(imgMetaData.getOperation().equals( CREATE )) {
			validateNewProductImg(file, imgMetaData);
		}else {
			validateUpdatedProductImg(file, imgMetaData);
		}
			
	}




	private void validateUpdatedProductImg(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException {
		if(!imgMetaData.areRequiredForUpdatePropertiesProvided()) {
			throw new BusinessException(
					String.format("Missing required parameters! required parameters for updating existing image are: [%s] and provided parameters are [%s]"
							, imgMetaData.getRequiredPropertyNamesForDataUpdate()
							, imgMetaData.toString())
					, "MISSING PARAM"
					, NOT_ACCEPTABLE);
		}
		
		validateImgId(imgMetaData);
		
		if(file != null)
			validateProductImgFile(file);	
		
	}




	private void validateImgId(ProductImageUpdateDTO imgMetaData) throws BusinessException {
		//based on previous validations assert imageId is provided
		Long orgId = securityService.getCurrentUserOrganizationId();
		Long imgId = imgMetaData.getImageId();
		
		if( !productImagesRepository.existsByIdAndProductEntity_OrganizationId(imgId, orgId))
			throw new BusinessException(
					String.format("No product image exists with id: %d !", imgId)
					, "INVALID PARAM:image_id"
					, NOT_ACCEPTABLE);
	}




	private void validateNewProductImg(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException {
		if(!imgMetaData.areRequiredForCreatePropertiesProvided()) {
			throw new BusinessException(
					format("Missing required parameters! required parameters for adding new image are:[%s], and provided parameters are [%s]"
							, imgMetaData.getRequiredPropertyNamesForDataCreate()
							, imgMetaData.toString())
					, "MISSING PARAM"
					, NOT_ACCEPTABLE);
		}
		
		validateProductOfImg(imgMetaData);
		
		validateProductImgFile(file);		
	}




	private void validateProductOfImg(ProductImageUpdateDTO imgMetaData) throws BusinessException {
		
		Long productId = imgMetaData.getProductId();		
		Optional<ProductEntity> product = productRepository.findById(productId);
		if( !product.isPresent() )
			throw new BusinessException(
					String.format("Product Id :[%d] doesnot exists!", productId)
					, "INVALID PARAM:product_id"
					, NOT_ACCEPTABLE);
		
		
		validateUserCanModifyProduct(product);		
		
		validateProductVariantForImg(imgMetaData, productId);
			
	}




	private void validateProductVariantForImg(ProductImageUpdateDTO imgMetaData, Long productId) throws BusinessException {
		Long variantId = imgMetaData.getVariantId();		
		if(variantId != null ) {
			Optional<ProductVariantsEntity> variant = productVariantsRepository.findById(variantId);
			if(variantId != null && !variant.isPresent())
				throw new BusinessException(
						String.format("Product variant with id [%d] doesnot exists!", variantId)
						, "INVALID PARAM:variant_id"
						, NOT_ACCEPTABLE);

			
			if(variantNotForProduct(variant, productId))
				throw new BusinessException(
						String.format("Product variant with id [%d] doesnot belong to product with id [%d]!", variantId, productId)
						, "INVALID PARAM:variant_id"
						, NOT_ACCEPTABLE);
		}
	}



	/**
	 * product must follow the organization of the user 
	 * */
	private void validateUserCanModifyProduct(Optional<ProductEntity> product) throws BusinessException {
		BaseUserEntity user = securityService.getCurrentUser();
		Long productOrg = product.map(p -> p.getOrganizationId()).orElse(null);
		
		if(!Objects.equals( user.getOrganizationId() , productOrg))
			throw new BusinessException(
					format(ERR_USER_CANNOT_MODIFY_PRODUCT, user.getEmail(), productOrg)
					, "INSUFFICIENT RIGHTS"
					, HttpStatus.FORBIDDEN);
	}
	
	
	
	
	
	
	/**
	 * product must follow the organization of the user 
	 * */
	private void validateUserCanModifyProduct(Long productId) throws BusinessException {
		Optional<ProductEntity> product = productRepository.findById(productId);
		validateUserCanModifyProduct(product);
	}



	

	private Boolean variantNotForProduct(Optional<ProductVariantsEntity> variant, Long productId) {
		Boolean variantNotForProduct = variant.get().getProductEntity() == null 
											|| !variant.get().getProductEntity().getId().equals(productId);
		return variantNotForProduct;
	}



	

	private void validateProductImgFile(MultipartFile file) throws BusinessException {
		if(file == null || file.isEmpty() || file.getContentType() == null)
			throw new BusinessException(
					"No image file provided!"
					, "MISSIG PARAM:image"
					, NOT_ACCEPTABLE);
		
		String mimeType = file.getContentType();
		if(!mimeType.startsWith("image"))
			throw new BusinessException(
					String.format("Invalid file type[%]! only MIME 'image' types are accepted!", mimeType)
					, "MISSIG PARAM:image"
					, NOT_ACCEPTABLE);
	}



	
	@Override
	@Transactional
	public ProductImageDeleteResponse deleteImage(Long imgId, Long productId, Long brandId) throws BusinessException {
		validateOneParameterProvided(imgId, productId, brandId);

		if (!isBlankOrNull(productId)) {
			deleteProductImages(productId);
		}
		else if (!isBlankOrNull(imgId)){
			productId = deleteSingleProductImages(imgId);
		}else if (!isBlankOrNull(brandId)){
			deleteBrandProductsImages(brandId);
		}

		return new ProductImageDeleteResponse(productId);
	}

	private void validateOneParameterProvided(Long ...params) {
		if(!hasOneNonNull(params)){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$IMG$0012);
		}
	}

	private void deleteProductImages(Long productId) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		List<String> images =  productImagesRepository.findUrlsByProductIdAndOrganizationId(productId, orgId);
		productImagesRepository.deleteByProductEntity_IdAndProductEntity_organizationId(productId, orgId);
		for(String img : images) {
			fileService.deleteFileByUrl(img);
		}
	}

	private Long deleteSingleProductImages(Long imgId) throws BusinessException {
		Long orgId = securityService.getCurrentUserOrganizationId();
		ProductImagesEntity img =
				productImagesRepository.findByIdAndProductEntity_OrganizationId(imgId, orgId)
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, P$IMG$0011, imgId, orgId));

		Long productId = ofNullable(img.getProductEntity())
							.map(prod -> prod.getId())
							.orElse(null);

		Long cnt = productImagesRepository.countByUri(img.getUri());
		productImagesRepository.deleteById(imgId);

		deleteImgFileIfNotUsed(img, cnt);

		return productId;
	}

	private void deleteImgFileIfNotUsed(ProductImagesEntity img, Long cnt) throws BusinessException {
		if(cnt <= 1) {
			fileService.deleteFileByUrl(img.getUri());
		}
	}

	private void deleteBrandProductsImages(Long brandId) {
		Long orgId = securityService.getCurrentUserOrganizationId();

		List<String> existingImages = productImagesRepository
										.findUrlsByBrandIdAndOrganizationId(brandId, orgId);

		productImagesRepository.deleteByBrandId(brandId, orgId);

		existingImages
				.stream()
				.forEach(fileService::deleteFileByUrl);
	}




	private void validateImgToDelete(ProductImagesEntity img) throws BusinessException {
		Long orgId = Optional.ofNullable(img.getProductEntity())
								.map(prod -> prod.getOrganizationId())
								.orElse(null);
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		BaseUserEntity user =  empRepo.getOneByEmail(auth.getName());
		
		if(!user.getOrganizationId().equals(orgId)) {
			throw new BusinessException(
					String.format("User from organization of id[%d] have no rights to delete product image of id[%d]",orgId, img.getId())
					, "UNAUTHRORIZED"
					, HttpStatus.FORBIDDEN);
		}
	}





	@Override
	@Transactional(rollbackFor = Throwable.class)
	public List<ProductImageUpdateResponse> updateProductImageBulk(@Valid MultipartFile zip, @Valid MultipartFile csv,
			@Valid ProductImageBulkUpdateDTO metaData) throws BusinessException {		
		validateUpdateImageBulkRequest(zip, csv, metaData);
		Set<ImportedImage> importedImgs = new HashSet<>(helper.extractImgsToImport(zip, csv, metaData));
		return saveImgsBulk(importedImgs, metaData.isDeleteOldImages());
	}

	
	
	


	@Override
	@Transactional(rollbackFor = Throwable.class)
	public List<ProductImageUpdateResponse> saveImgsBulk(Set<ImportedImage> importedImgs) throws BusinessException {		
		List<String> errors = new ArrayList<>();
		List<ProductImageUpdateResponse> responses = new ArrayList<>();
		
		Map<String, List<ImportedImage>> imgsGroupedByFile = groupImagesByPath(importedImgs);
		
		for(String fileName : imgsGroupedByFile.keySet()) {
			saveSingleImgToAllItsVariants(fileName, imgsGroupedByFile, errors, responses);
		}
		
		if(!errors.isEmpty()) {
			rollbackImgBulkImport(responses);
			throwExceptionWithErrorList(errors);
		}
		
		return responses;
	}
	
	
	
	
	
	
	@Override
	@Transactional(rollbackFor = Throwable.class)
	public List<ProductImageUpdateResponse> saveImgsBulk(Set<ImportedImage> importedImgs, boolean deleteOldImages) throws BusinessException {		
		if(deleteOldImages) {
			deleteProductImages(importedImgs);
		}
		return saveImgsBulk(importedImgs);
	}

	private void deleteProductImages(Set<ImportedImage> importedImgs) {
		Long orgId = securityService.getCurrentUserOrganizationId();

		List<ProductImageUpdateDTO> dtos = importedImgs.stream().map(ImportedImage::getImgMetaData).collect(toList());
		List<Long> productIds =
				dtos.stream().map(ProductImageUpdateDTO::getProductId).filter(Objects::nonNull).collect(toList());
		List<Long> variantIds =
				dtos.stream().map(ProductImageUpdateDTO::getVariantId).filter(Objects::nonNull).collect(toList());

		if (productIds.isEmpty())
			return;

		List<String> existingImages;
		if (variantIds.isEmpty()) {
			existingImages = productImagesRepository
					.findByProductsIds(productIds)
					.stream()
					.map(ProductImagesEntity::getUri)
					.collect(toList());
			productImagesRepository.deleteByProductIdIn(productIds);
		} else {
			existingImages = productImagesRepository
					.findByProductVariantsEntity_IdInOrderByPriority(new HashSet<>(variantIds))
					.stream()
					.map(ProductImagesEntity::getUri)
					.collect(toList());
			productImagesRepository.deleteByVariantIdIn(variantIds);
		}

		deletePhysicalProductImages(existingImages);
	}

	private void deleteOrgProductImages() {
		Long orgId = securityService.getCurrentUserOrganizationId();

		List<String> existingImages = productImagesRepository
				.findByProductAndBundle_OrganizationId(orgId)
				.stream()
				.map(ProductImagesEntity::getUri)
				.collect(toList());

		productImagesRepository.deleteByProductEntity_organizationId(orgId);

		deletePhysicalProductImages(existingImages);
	}

	private void deletePhysicalProductImages(List<String> existingImages) {
		existingImages
				.stream()
				.forEach(fileService::deleteFileByUrl);
	}

	private Set<ImportedImage> fetchImgsToImportFromUrls(@Valid MultipartFile csv,
			@Valid ProductImageBulkUpdateDTO metaData) {
				
		Map<String,List<VariantIdentifier>> fileIdentifiersMap = helper.createFileToVariantIdsMap(csv);
		
		return readImgsFromUrls(fileIdentifiersMap, metaData);
	}

	private Set<ImportedImage> readImgsFromUrls(
			Map<String, List<VariantIdentifier>> fileIdentifiersMap
			, ProductImageBulkUpdateDTO metaData) {
		
		Mono<VariantCache> variantCacheMono = 
				createVariantCache(fileIdentifiersMap);
		
		return variantCacheMono
				.flatMapMany( variantCache ->
						Flux
						.fromIterable(fileIdentifiersMap.entrySet())
						.map(this::toVariantIdentifierAndUrlPair)
						.window(20)		//get the images in batches of 20 per second
						.delayElements(Duration.ofSeconds(1))
						.flatMap(pair -> toImportedImages(pair, metaData, variantCache))
						.distinct()
				)
				.collectList()
				.map(HashSet::new)
				.blockOptional()
				.orElse(new HashSet<>());
	}
	
	
	
	
	@Override
	public Flux<ImportedImage> readImgsFromUrls(
			Map<String, List<VariantIdentifier>> imgToProductsMapping
			, ProductImageBulkUpdateDTO metaData
			, WebClient client) {
		
		Mono<VariantCache> variantCacheMono = createVariantCache(imgToProductsMapping);
		
		return variantCacheMono
				.flatMapMany( variantCache -> 
					fetchImportedImagesInBatches(imgToProductsMapping, metaData, client, variantCache)
				);
	}

	/**
	 * Pre-fetch The product variants of the images from the database and cache them.
	 * @return a cache of ProductVariantsEntity
	 * */
	public  Mono<VariantCache> createVariantCache(Map<String, List<VariantIdentifier>> fileIdentifiersMap) {
		List<VariantIdentifier> variantIdentifiers = 
				fileIdentifiersMap
				.values()
				.stream()
				.flatMap(List::stream)
				.collect(toList());
		return cachingHelper.createVariantCacheMono(variantIdentifiers);
	}






	private Flux<ImportedImage> fetchImportedImagesInBatches(
			Map<String, List<VariantIdentifier>> imgToProductsMapping, ProductImageBulkUpdateDTO metaData,
			WebClient client, VariantCache variantCache) {
		return Flux
				.fromIterable(imgToProductsMapping.entrySet())
				.map(this::toVariantIdentifierAndUrlPair)
				.window(20)		//get the images in batches of 20 per second
				.delayElements(Duration.ofSeconds(1))
				.flatMap(pair -> toImportedImages(pair, metaData, variantCache, client))
				.distinct();
	}






	
	private VariantIdentifierAndUrlPair toVariantIdentifierAndUrlPair(Map.Entry<String, List<VariantIdentifier>> ent) {
		return new VariantIdentifierAndUrlPair(ent.getKey(), ent.getValue());
	}
	
	
	
	
	private Flux<ImportedImage> toImportedImages(Flux<VariantIdentifierAndUrlPair> imgDetails
			, ProductImageBulkUpdateDTO metaData
			, VariantCache variantCache){
		WebClient client = buildWebClient();		
		return toImportedImages(imgDetails, metaData, variantCache, client);
	}
	
	
	
	
	
	private Flux<ImportedImage> toImportedImages(Flux<VariantIdentifierAndUrlPair> imgDetails
			, ProductImageBulkUpdateDTO metaData
			, VariantCache variantCache
			, WebClient client){
		return imgDetails
				.flatMap(details -> toImportedImage(details, metaData, variantCache, client));
	}
	
	
	
	
	
	private Flux<ImportedImage> toImportedImage(VariantIdentifierAndUrlPair imgDetails
			, ProductImageBulkUpdateDTO metaData
			, VariantCache variantCache
			, WebClient client) {
		String url = imgDetails.getUrl();
		if(isBlankOrNull(url)) {
			return Flux.empty();
		}		
		//TODO: try to use reflection to check if the webclient have a base url or not.
		//until this is done; webclients are assumed to have no base url and all URL's are assumed to be absolute.
		String httpUrl = !startsWithAnyOfAndIgnoreCase(url, "http://", "https://") ? "http://" + url : url;
		
		Mono<MultipartFile> imgFile = fetchImageData(client, httpUrl);
		
		List<VariantIdentifier> variantIdentifiers = imgDetails.getIdentifier(); 
		Flux<ProductImageUpdateDTO> importedImgsMetaData = createImgsMetaData(metaData, variantCache, variantIdentifiers);
			
		return Flux
				.zip(imgFile, importedImgsMetaData.buffer(), (file, mDataList) ->  combineImageFileAndMetaData(file, mDataList, url))
				.flatMap(importedImg -> importedImg);
	}

	
	
	
	private Flux<ImportedImage> combineImageFileAndMetaData(MultipartFile file, List<ProductImageUpdateDTO> mDataList, String url){
		return Flux.fromIterable(mDataList)
				 .map(mData -> new ImportedImage(file, mData, url));
	}





	private Flux<ProductImageUpdateDTO> createImgsMetaData(ProductImageBulkUpdateDTO metaData,
			VariantCache variantCache, List<VariantIdentifier> variantIdentifiers) {
		return Flux.fromIterable(variantIdentifiers)
					.map(identifier -> helper.getProductVariant(identifier, variantCache, metaData))
					.filter(Optional::isPresent)
					.map(Optional::get)
					.map(variant -> helper.createImgMetaData(metaData, variant));
	}






	private Mono<MultipartFile> fetchImageData(WebClient client, String httpUrl) {
		return client
				.get()
				.uri(httpUrl)
				.exchange()
				.timeout(Duration.ofSeconds(IMG_DOWNLOAD_TIMEOUT_SEC), Mono.error(() -> new TimeoutException()))
				.doOnEach(signal -> logFailedImageFetch(signal, httpUrl))
				.onErrorReturn(ClientResponse.create(INTERNAL_SERVER_ERROR).build())
				.filter(res -> res.rawStatusCode() < 400)
				.flatMap(res -> res.bodyToMono(byte[].class))				
				.map(bytes -> readUrlAsMultipartFile(httpUrl, bytes));
	}
	
	
	
	
	private void logFailedImageFetch(Signal<ClientResponse> signal, String httpUrl) {
		if(signal.isOnError()) {
			String message = format("Exception thrown while fetching image data from URL[%s]", httpUrl);
			logger.log(SEVERE, message, signal.getThrowable());
		}else if(signal.isOnNext() && !Objects.equals(signal.get().statusCode(), OK)) {
			String message = format("Failed to fetch image data from URL[%s]! returned status is [%s]", httpUrl, signal.get().statusCode());
			logger.log(SEVERE, message);
		}
	}

	private Map<String, List<ImportedImage>> groupImagesByPath( Set<ImportedImage> allImportedImgs) throws BusinessException {
		return allImportedImgs
				.stream()
				.collect(groupingBy(ImportedImage::getPath));
	}

	private void saveSingleImgToAllItsVariants(String fileName, Map<String, List<ImportedImage>> imgMetadataMap,
			List<String> errors, List<ProductImageUpdateResponse> responses) {
		MultipartFile file = getMulitpartFile(imgMetadataMap, fileName);			
		List<ProductImageUpdateDTO> imgMetaDataList = getImgsMetaDataList(imgMetadataMap, fileName);
		
		try {
			responses.addAll( saveNewProductImgsUsingSameUrl(file, imgMetaDataList) );
		}catch(Exception e) {
			logger.log(SEVERE, e.getMessage(), e);
			imgMetadataMap.get(fileName)
						.forEach(img -> errors.add( createImgImportErrMsg(img, e) ));
		}
	}






	private List<ProductImageUpdateDTO> getImgsMetaDataList(Map<String, List<ImportedImage>> groupedByFile,
			String fileName) {
		return groupedByFile
				.get(fileName)
				.stream()
				.map(ImportedImage::getImgMetaData)
				.collect(toList());
	}






	private MultipartFile getMulitpartFile(Map<String, List<ImportedImage>> groupedByFile, String fileName) {
		return groupedByFile
				.get(fileName)
				.stream()
				.map(ImportedImage::getImage)
				.findFirst()
				.orElse(null);
	}



	

	private void rollbackImgBulkImport(List<ProductImageUpdateResponse> responses) throws BusinessException {
		for(ProductImageUpdateResponse res: responses) {
			deleteImage( res.getImageId(), null, null);
		}
		
		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
	}
	
	



	private void throwExceptionWithErrorList(List<String> errors) throws BusinessException {
		throw new ImportImageBulkRuntimeException(errors);
	}




	private String createImgImportErrMsg(ImportedImage img, Exception e) {
		StringBuilder msg = new StringBuilder();
		msg.append( String.format("Error importing image file with name[%s]", img.getImage().getOriginalFilename()) );
		msg.append( System.getProperty("line.separator") );
		msg.append("Error Message: " + e.getMessage());
		String msgString = msg.toString();
		return msgString;
	}

	void validateVariantExistance(Optional<ProductVariantsEntity> variantsEntity, String variantId) throws BusinessException {
		if (!variantsEntity.isPresent())
			throw new BusinessException("Provided variant_id("+variantId+") doesn't match any existing variant!", "INVALID_PARAM: variant_id", NOT_ACCEPTABLE);
	}

	private MultipartFile readUrlAsMultipartFile(String httpUrl, byte[] bytes){
		try {
			String extension = getFileExtension(bytes);
			String fileName = createImageFileNameFromUrl(httpUrl, extension);
			FileItem fileItem = helper.createFileItem(fileName);
			readIntoFileItem(bytes, fileItem);			
			return new CommonsMultipartFile(fileItem);
		} catch (IOException e) {
			logger.logException(e, SEVERE);
			throw new RuntimeException(e);
		}		
	}

	private String getFileExtension(byte[] bytes) {		
		try {
			String contentType = new Tika().detect(bytes);
			TikaConfig config = TikaConfig.getDefaultConfig();
			MimeType mimeType = config.getMimeRepository().forName(contentType);
			String extension = mimeType.getExtension();
			return extension;
		} catch (MimeTypeException e) {
			logger.logException(e, Level.WARNING);
			return "png";
		}		
	}

	private String createImageFileNameFromUrl(String httpUrl, String extension) {
		String[] parts = httpUrl.split("/");
		String lastPart = parts[parts.length - 1];
		
		return urlPartHasExtension(lastPart) ? 
				lastPart: "img" + extension;
	}

	private boolean urlPartHasExtension(String lastPart) {
		String patternStr = "([^\\s]+(\\.(?i)(.+))$)";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(lastPart);
		boolean hasExtension = matcher.matches();
		return hasExtension;
	}

	private void readIntoFileItem(byte[] bytes, FileItem fileItem) throws IOException {
		OutputStream fos = fileItem.getOutputStream();
		fos.write(bytes);
	}
	private void validateUpdateImageBulkRequest(@Valid MultipartFile zip, @Valid MultipartFile csv,
			@Valid ProductImageBulkUpdateDTO metaData) throws BusinessException {
		validateImageBulkMetadata(metaData);
		validateImgBulkZip(zip);
	}

	private void validateImageBulkMetadata(ProductImageBulkUpdateDTO metaData) throws BusinessException {
		if(metaData.getPriority() == null || metaData.getType() == null) {
			throw new BusinessException(
					"Missing required metadata parameters, required parameters are [type, priority]!"
					, "MISSING PARAM"
					, NOT_ACCEPTABLE);
		}
	}

	@Override
	public String getProductCoverImage(Long productId) {
		return productImagesRepository
				.findByProductEntity_IdAndPriorityOrderByPriority(productId, 0)
				.stream()
				.filter(Objects::nonNull)
				.sorted( comparing(ProductImagesEntity::getId))
				.findFirst()
				.map(img-> img.getUri())
				.orElse(null);
	}
	
	
	

	
	
	private Boolean isProductCoverImage(ProductImagesEntity img) {
		return Objects.equals(img.getPriority(),0);
	}
	
	





	@Override
	public List<ProductImgDetailsDTO> getProductImgs(Long productId) throws BusinessException {
		validateProductToFetchItsImgs(productId);
		
		return getProductAndVariantsImageEntities(Collections.singletonList(productId))
				.stream()
				.map(this::toProductImgDetailsDTO)
				.collect(toList());
	}






	private Set<ProductImagesEntity> getProductAndVariantsImageEntities(List<Long> productIds) {
		
		Set<Long> variantIds = productVariantsRepository.findVariantIdByProductIdIn(productIds);
		
		Set<ProductImagesEntity> imgs = new HashSet<>();
		imgs.addAll(productImagesRepository.findByProductEntity_IdIn(productIds));
		imgs.addAll(productImagesRepository.findByProductVariantsEntity_IdInOrderByPriority(variantIds));
		return imgs;
	}


	@Override
	public Map<Long,List<ProductImagesEntity>> getProductsImageList(List<Long> productIds) {

		Map<Long,List<ProductImagesEntity>> productImgsMap = getProductAndVariantsImageEntities(productIds)
						.stream()
						.collect(groupingBy(i -> i.getProductEntity().getId()))
						.entrySet()
						.stream()
						.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		return productImgsMap;
	}
	
	
	
	
	
	private void validateProductToFetchItsImgs(Long productId) throws BusinessException{
		if(!productRepository.existsById(productId)) {
			throw new BusinessException("INVALID PARAM: product_id", ERR_NO_PRODUCT_EXISTS_WITH_ID, NOT_ACCEPTABLE);
		}
		
		validateUserCanModifyProduct(productId);
	}






	private ProductImgDetailsDTO toProductImgDetailsDTO(ProductImagesEntity entity) {
		Long variantId = 
				ofNullable(entity.getProductVariantsEntity())
				.map(ProductVariantsEntity::getId)
				.orElse(null);
		Long productId =
				ofNullable(entity.getProductEntity())
				.map(ProductEntity::getId)
				.orElse(null);
		
		ProductImgDetailsDTO dto = new ProductImgDetailsDTO();
		dto.setImageId(entity.getId());
		dto.setPriority(entity.getPriority());
		dto.setProductId(productId);
		dto.setType(entity.getType());
		dto.setUri(entity.getUri());		
		dto.setVariantId(variantId);		
		
		return dto;
	}






	@Override
	@Transactional(rollbackFor = Throwable.class)
	public List<ProductImageUpdateResponse> updateProductImageBulkViaUrl(MultipartFile csv,
			@Valid ProductImageBulkUpdateDTO metaData) throws BusinessException {
		validateImageBulkMetadata(metaData);
		Set<ImportedImage> importedImgs = fetchImgsToImportFromUrls(csv, metaData);;
		return saveImgsBulk(importedImgs, metaData.isDeleteOldImages());
	}
	
	
	
	
	
	private WebClient buildWebClient() {
		return WebClient
        		.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().wiretap(true)
                ))
                .build();
	}






	@Override
	public void deleteAllImages(boolean isConfirmed) throws BusinessException {
		if(!isConfirmed) {
			throw new BusinessException("Delete operation for all images is not confirmed!", "INVALID PARAM: confirm", NOT_ACCEPTABLE);
		}

		deleteOrgProductImages();
	}


	@Override
	public List<ProductImageDTO> getProductsAndVariantsImages(List<Long> productsIdList, List<Long> variantsIdList) {
		if ((productsIdList == null || productsIdList.isEmpty()) && (variantsIdList == null || variantsIdList.isEmpty())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0001);
        }
		return productImagesRepository.getProductsAndVariantsImages(productsIdList, variantsIdList);
	}


	@Override
	public Map<Long, String> getProductsImagesMap(List<Long> productsIdList, List<Long> variantsIdList) {
		return 	getProductsAndVariantsImages(productsIdList, variantsIdList)
					.stream()
					.filter(Objects::nonNull)
					.collect(groupingBy(ProductImageDTO::getProductId))
					.entrySet()
					.stream()
					.map(this::getProductCoverImageUrlMapEntry)
					.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}



	@Override
	public Map<Long, String> getProductsImagesMap(Map<Long, List<ProductImageDTO>> getProductsAllImagesMap) {
		return 	getProductsAllImagesMap
				.entrySet()
				.stream()
				.map(this::getProductCoverImageUrlMapEntry)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}


	@Override
	public Map<Long, List<ProductImageDTO>> getProductsAllImagesMap(List<Long> productsIdList, List<Long> variantsIdList) {
		return getProductsAndVariantsImages(productsIdList, variantsIdList)
				.stream()
				.filter(Objects::nonNull)
				.filter(i -> Objects.nonNull(i.getProductId()))
				.collect(groupingBy(ProductImageDTO::getProductId));

	}


	private Map.Entry<Long, String> getProductCoverImageUrlMapEntry(Map.Entry<Long, List<ProductImageDTO>> mapEntry){
		String uri = mapEntry.getValue()
				.stream()
				.min(comparing(ProductImageDTO::getPriority))
				.map(ProductImageDTO::getImagePath)
				.orElse(null);

		return new AbstractMap.SimpleEntry<>(mapEntry.getKey(), uri);
	}





	/**
	 * as this method can be used to get images of soft-deleted variants, it should use
	 * native queries
	 * @return a map of variant id and its cover image. cover image are selected based
	 * on the method electVariantCoverImage.
	 * */
	@Override
	public Map<Long, Optional<String>> getVariantsCoverImages(List<Long> variantsIds) {
		if(variantsIds.isEmpty()) {
			return emptyMap();
		}
		
		List<ProductVariantPair> productVariantIdPairs = getProductVariantPairs(variantsIds);

		List<ProductImageDTO> allImgs = getAllImgsFor(variantsIds, productVariantIdPairs);
		
		return productVariantIdPairs
				.stream()
				.map(variant -> getVariantImgs(variant, allImgs))
				.map(this::electVariantCoverImage)
				.collect(toMap(VariantCoverImg::getVariantId, VariantCoverImg::getImgUrl));
	}



	private List<ProductImageDTO> getAllImgsFor(List<Long> variantsIds
							, List<ProductVariantPair> productVariantIdPairs) {
		List<Long> productsIds = 
				productVariantIdPairs
				.stream()
				.map(ProductVariantPair::getProductId)
				.collect(toList());
		
		String variantImgsQuery = 
				queryFactory
				.select(productImages.id.as("id")
						, productImages.uri.as("imagePath")
						, products.id.as("productId")
						, productVariants.id.as("variantId")
						, productImages.priority.as("priority"))
				.from(productImages)
				.leftJoin(products)
					.on(productImages.productId.eq(products.id))
				.leftJoin(productVariants)
					.on(productImages.variantId.eq(productVariants.id))
				.where(products.id.in(productsIds)
						.or(productVariants.id.in(variantsIds)))
				.orderBy(
						cases()
						.when(productImages.variantId.isNull())
						.then(0)
						.otherwise(1).asc()
						, productImages.priority.asc())
				.getSQL().getSQL();
				
				
		return template.query(variantImgsQuery, new BeanPropertyRowMapper<>(ProductImageDTO.class));
	}
	
	
	
	
	private List<ProductVariantPair> getProductVariantPairs(List<Long> variantsIds){
		String productIdsQuery = 
				queryFactory
				.select(productVariants.productId.as("productId")
						, productVariants.id.as("variantId"))
				.from(productVariants)
				.where(productVariants.id.in(variantsIds))
				.getSQL().getSQL();
		
		
		return template.query(productIdsQuery
						 , new BeanPropertyRowMapper<>(ProductVariantPair.class));
	}






	private VariantBasicDataWithImgs getVariantImgs(ProductVariantPair variant, List<ProductImageDTO> allImgs) {
		return allImgs
				.stream()
				.filter(img -> imgBelongToVariantOrItsProduct(variant, img))
				.collect(
						collectingAndThen(
								toList()
								, imgs -> new VariantBasicDataWithImgs(variant, imgs)));
	}






	private boolean imgBelongToVariantOrItsProduct(ProductVariantPair variant, ProductImageDTO img) {
		return Objects.equals(variant.getProductId(), img.getProductId()) 
						|| Objects.equals(variant.getVariantId(), img.getVariantId());
	}
	
	
	
	
	private VariantCoverImg electVariantCoverImage(VariantBasicDataWithImgs variant) {
		Optional<String> coverImg = 
				variant
				.getImgs()
				.stream()
				.sorted(variantCoverImgSorter())
				.findFirst()
				.map(ProductImageDTO::getImagePath);
		return new VariantCoverImg(variant.getVariant().getVariantId(), coverImg);
	}
	
	
	
	private Comparator<ProductImageDTO> variantCoverImgSorter(){
		return Comparator
				.comparingInt(this::variantImgFirst)
				.thenComparingInt(ProductImageDTO::getPriority);
	}
	
	
	
	private Integer variantImgFirst(ProductImageDTO img) {
		return nonNull(img.getVariantId()) ? 0 : 1; 
	}
	
	
	@Override
	@Transactional
	public void deleteVarientImages(Long varId) {
		List<String> images =  productImagesRepository.findUrlsByVariantIdAndOrganizationId(varId);
		productImagesRepository.deleteByVariantIdIn(Arrays.asList(varId));
		for(String img : images) {
			fileService.deleteFileByUrl(img);
		}
	}
}








@Data
@AllArgsConstructor
class VariantBasicDataWithImgs{
	private ProductVariantPair variant;
	private List<ProductImageDTO> imgs;
}



@Data
@AllArgsConstructor
class VariantCoverImg{
	private Long variantId;
	private Optional<String> imgUrl;
}



@Data
class ProductVariantPair{
	Long productId;
	Long variantId;
}



@Data
@EqualsAndHashCode(callSuper = true)
class SavedImportedSwatchImage extends ImportedSwatchImage{
	private String url;

	public SavedImportedSwatchImage(ImportedSwatchImage img, String url){
		setImage(img.getImage());
		setVariantId(img.getVariantId());
		setFeatureId(img.getFeatureId());
		this.url = url;
	}
}


@Data
@AllArgsConstructor
class SwatchDataCache{
	private Map<Long, ProductExtraAttributesEntity> existingAttrValues;
	private Map<Long, ProductVariantsEntity> variants;
	private ExtraAttributesEntity attrEntity;
}

