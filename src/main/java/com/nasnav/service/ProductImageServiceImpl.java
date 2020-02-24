package com.nasnav.service;

import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import static com.nasnav.constatnts.EntityConstants.Operation.CREATE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_CSV_PARSE_FAILURE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_IMPORTING_IMGS;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_IMPORTING_IMG_FILE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_NO_IMG_DATA_PROVIDED;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_NO_IMG_IMPORT_RESPONSE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_NO_PRODUCT_EXISTS_WITH_BARCODE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_NO_PRODUCT_EXISTS_WITH_ID;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_NO_VARIANT_FOUND;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_PRODUCT_IMG_BULK_IMPORT;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_READ_ZIP;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_USER_CANNOT_MODIFY_PRODUCT;
import static com.nasnav.integration.enums.MappingType.PRODUCT_VARIANT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ArrayUtils.toPrimitive;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.validation.Valid;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dto.ProductImageBulkUpdateDTO;
import com.nasnav.dto.ProductImageUpdateDTO;
import com.nasnav.dto.ProductImageUpdateIdentifier;
import com.nasnav.dto.ProductImgDetailsDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductImagesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.response.ProductImageDeleteResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.service.model.ImportedImage;
import com.sun.istack.logging.Logger;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;



@Service
public class ProductImageServiceImpl implements ProductImageService {
	
	private static final int PRODUCT_IMAGE = 7;

	private static final String NO_IMG_FOUND_URL = "no_img_found.jpg";

	private Logger logger = Logger.getLogger(ProductService.class);
	
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
	private IntegrationService integrationService;

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




	private ProductImageUpdateResponse saveUpdatedProductImg(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException {
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
		
		if(url != null)
			entity.setUri(url);
		
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
					.orElseThrow(() -> new BusinessException(ERR_NO_IMG_IMPORT_RESPONSE, ERR_NO_IMG_IMPORT_RESPONSE, HttpStatus.INTERNAL_SERVER_ERROR));
	}
	
	
	
	
	
	
	private List<ProductImageUpdateResponse> saveNewProductImgsUsingSameUrl(MultipartFile file, List<ProductImageUpdateDTO> imgMetaDataList)
			throws BusinessException {
		if(imgMetaDataList == null || imgMetaDataList.isEmpty()) {
			throw new BusinessException(ERR_NO_IMG_DATA_PROVIDED, ERR_NO_IMG_DATA_PROVIDED, HttpStatus.NOT_ACCEPTABLE);
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
	


	
	

	private String saveFile(MultipartFile file) throws BusinessException {
		Long userOrgId =  securityService.getCurrentUserOrganizationId();		
		return fileService.saveFile(file, userOrgId);
	}
	
	
	
	
	
	
	private ProductImageUpdateResponse saveNewProductImgMetaData(ProductImageUpdateDTO imgMetaData, String url)
			throws BusinessException {		
		Long imgId = saveProductImgToDB(imgMetaData, url);		
		return new ProductImageUpdateResponse(imgId, url);
	}




	private Long saveProductImgToDB(ProductImageUpdateDTO imgMetaData, String uri) throws BusinessException {
		Long productId = imgMetaData.getProductId();
		Optional<ProductEntity> productEntity = productRepository.findById( productId );
		
		
		ProductImagesEntity entity = new ProductImagesEntity();
		entity.setPriority(imgMetaData.getPriority());
		entity.setProductEntity(productEntity.get());		
		entity.setType(imgMetaData.getType());
		entity.setUri(uri);
		Optional.ofNullable( imgMetaData.getVariantId() )
				.flatMap(productVariantsRepository::findById)
				.ifPresent(entity::setProductVariantsEntity);
		
		entity = productImagesRepository.save(entity);
		
		return entity.getId();
	}




	private void validateProductImg(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException {
		if(imgMetaData == null)
			throw new BusinessException("No Metadata provided for product image!", "INVALID PARAM", HttpStatus.NOT_ACCEPTABLE);
		
		if(!imgMetaData.isRequiredPropertyProvided("operation"))
			throw new BusinessException("No operation provided!", "INVALID PARAM:operation", HttpStatus.NOT_ACCEPTABLE);
					
		
		if(imgMetaData.getOperation().equals( Operation.CREATE )) {
			validateNewProductImg(file, imgMetaData);
		}else {
			validateUpdatedProductImg(file, imgMetaData);
		}
			
	}




	private void validateUpdatedProductImg(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException {
		if(!imgMetaData.areRequiredForUpdatePropertiesProvided()) {
			throw new BusinessException(
					String.format("Missing required parameters! required parameters for updating existing image are: %s", imgMetaData.getRequiredPropertiesForDataUpdate())
					, "MISSING PARAM"
					, HttpStatus.NOT_ACCEPTABLE);
		}
		
		validateProductOfImg(imgMetaData);		
		
		validateImgId(imgMetaData);
		
		if(file != null)
			validateProductImgFile(file);	
		
	}




	private void validateImgId(ProductImageUpdateDTO imgMetaData) throws BusinessException {
		//based on previous validations assert imageId is provided 
		Long imgId = imgMetaData.getImageId();
		
		if( !productImagesRepository.existsById(imgId))
			throw new BusinessException(
					String.format("No product image exists with id: %d !", imgId)
					, "INVALID PARAM:image_id"
					, HttpStatus.NOT_ACCEPTABLE);
	}




	private void validateNewProductImg(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException {
		if(!imgMetaData.areRequiredForCreatePropertiesProvided()) {
			throw new BusinessException(
					String.format("Missing required parameters! required parameters for adding new image are: %s", imgMetaData.getRequiredPropertiesForDataCreate())
					, "MISSING PARAM"
					, HttpStatus.NOT_ACCEPTABLE);
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
					, HttpStatus.NOT_ACCEPTABLE);		
		
		
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
						, HttpStatus.NOT_ACCEPTABLE);

			
			if(variantNotForProduct(variant, productId))
				throw new BusinessException(
						String.format("Product variant with id [%d] doesnot belong to product with id [%d]!", variantId, productId)
						, "INVALID PARAM:variant_id"
						, HttpStatus.NOT_ACCEPTABLE);
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
					, HttpStatus.NOT_ACCEPTABLE);
		
		String mimeType = file.getContentType();
		if(!mimeType.startsWith("image"))
			throw new BusinessException(
					String.format("Invalid file type[%]! only MIME 'image' types are accepted!", mimeType)
					, "MISSIG PARAM:image"
					, HttpStatus.NOT_ACCEPTABLE);
	}



	
	@Override
	public ProductImageDeleteResponse deleteImage(Long imgId) throws BusinessException {
		ProductImagesEntity img = 
				productImagesRepository.findById(imgId)
				 				.orElseThrow(()-> new BusinessException("No Image exists with id ["+ imgId+"] !", "INVALID PARAM:image_id", HttpStatus.NOT_ACCEPTABLE));
		
		Long productId = Optional.ofNullable(img.getProductEntity())
								.map(prod -> prod.getId())
								.orElse(null);					
		
		validateImgToDelete(img);		
		
		Long cnt = productImagesRepository.countByUri(img.getUri());
		productImagesRepository.deleteById(imgId);
		
		deleteImgFileIfNotUsed(img,cnt);		
		
		return new ProductImageDeleteResponse(productId);
	}






	private void deleteImgFileIfNotUsed(ProductImagesEntity img, Long cnt) throws BusinessException {
		if(cnt <= 1) {
			fileService.deleteFileByUrl(img.getUri());
		}
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
		List<ImportedImage> importedImgs = extractImgsToImport(zip, csv, metaData);
		return saveImgsBulk(importedImgs);
	}

	
	
	



	private List<ProductImageUpdateResponse> saveImgsBulk(List<ImportedImage> importedImgs) throws BusinessException {		
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
	
	
	
	
	
	





	private List<ImportedImage> fetchImgsToImportFromUrls(@Valid MultipartFile csv,
			@Valid ProductImageBulkUpdateDTO metaData) throws BusinessException {				
		List<String> errors = new ArrayList<>();		
		Map<String,List<ProductImageUpdateIdentifier>> fileIdentifiersMap = createFileToVariantIdsMap(csv);
		
		List<ImportedImage> imgs = readImgsFromUrls(fileIdentifiersMap, metaData);
		
		if(!errors.isEmpty()) {
			String errorsJson = getErrorMsgAsJson(errors);
			throw new BusinessException(ERR_IMPORTING_IMGS, errorsJson , INTERNAL_SERVER_ERROR);
		}
		
		return imgs;
	}






	private List<ImportedImage> readImgsFromUrls(
			Map<String, List<ProductImageUpdateIdentifier>> fileIdentifiersMap
			, ProductImageBulkUpdateDTO metaData) {
		
		VariantCache variantCache = createVariantCache(fileIdentifiersMap);
		
		return Flux
				.fromIterable(fileIdentifiersMap.entrySet())
				.map(this::toVariantIdentifierAndUrlPair)
				.window(20)		//get the images in batches of 20 per second
				.delayElements(Duration.ofSeconds(1))
				.flatMap(pair -> toImportedImages(pair, metaData, variantCache))
				.buffer()
				.blockFirst();
	}





	/**
	 * Pre-fetch The product variants of the images from the database and cache them.
	 * @return a cache of ProductVariantsEntity
	 * */
	private VariantCache createVariantCache(Map<String, List<ProductImageUpdateIdentifier>> fileIdentifiersMap) {
		Map<String, ProductVariantsEntity> idToVariantMap = getIdToVariantMap(fileIdentifiersMap);
		Map<String, ProductVariantsEntity> externalIdToVariantMap = getExternalIdToVariantMap(fileIdentifiersMap);
		Map<String, ProductVariantsEntity> barcodeToVariantMap = getBarcodeToVariantMap(fileIdentifiersMap);
		
		return new VariantCache(idToVariantMap, externalIdToVariantMap, barcodeToVariantMap);
	}




	
	private VariantIdentifierAndUrlPair toVariantIdentifierAndUrlPair(Map.Entry<String, List<ProductImageUpdateIdentifier>> ent) {
		return new VariantIdentifierAndUrlPair(ent.getKey(), ent.getValue());
	}
	
	
	
	
	private Flux<ImportedImage> toImportedImages(Flux<VariantIdentifierAndUrlPair> imgDetails
			, ProductImageBulkUpdateDTO metaData
			, VariantCache variantCache){
		WebClient client = buildWebClient();		
		return imgDetails
				.flatMap(details -> toImportedImage(details, metaData, variantCache, client));
	}
	
	
	
	
	
	private Flux<ImportedImage> toImportedImage(VariantIdentifierAndUrlPair imgDetails
			, ProductImageBulkUpdateDTO metaData
			, VariantCache variantCache
			, WebClient client) {
		String url = imgDetails.getUrl();
		if(StringUtils.isBlankOrNull(url)) {
			throw new RuntimeBusinessException("Empty url was provided!", "INVALID PARAM:csv", NOT_ACCEPTABLE);
		}		
		String httpUrl = !url.startsWith("http://") ? "http://" + url : url;
		
		Flux<MultipartFile> imgFile = readImageDataFromUrl(client, httpUrl);
		
		List<ProductImageUpdateIdentifier> variantIdentifiers = imgDetails.getIdentifier(); 
		Flux<ProductImageUpdateDTO> importedImgsMetaData = createImgsMetaData(metaData, variantCache, variantIdentifiers);
			
		return Flux.zip(imgFile, importedImgsMetaData, (file, mData) -> new  ImportedImage(file, mData, url));
	}






	private Flux<ProductImageUpdateDTO> createImgsMetaData(ProductImageBulkUpdateDTO metaData,
			VariantCache variantCache, List<ProductImageUpdateIdentifier> variantIdentifiers) {
		return Flux.fromIterable(variantIdentifiers)
					.map(identifier -> getProductVariant(identifier, variantCache))
					.map(variant -> creatImgMetaData(metaData, variant));
	}






	private Flux<MultipartFile> readImageDataFromUrl(WebClient client, String httpUrl) {
		return client
				.get()
				.uri(httpUrl)
				.retrieve()
				.bodyToFlux(Byte.class)
				.buffer()
				.map(bytes -> readUrlAsMultipartFile(httpUrl, bytes));
	}
	
	
	
	
	
	private ProductImageUpdateDTO creatImgMetaData(ProductImageBulkUpdateDTO metaData, ProductVariantsEntity variant) {
		Long variantId = variant.getId();
		Long productId = variant.getProductEntity().getId();
		
		ProductImageUpdateDTO imgMetaData = new ProductImageUpdateDTO();
		imgMetaData.setOperation(CREATE);
		imgMetaData.setPriority(metaData.getPriority());
		imgMetaData.setType(metaData.getType());	
		
		if(Objects.equals(metaData.getType(), PRODUCT_IMAGE)) {
			imgMetaData.setProductId(productId);
		}else {
			imgMetaData.setVariantId(variantId);
		}
		
		return imgMetaData;		
	}
	
	
	
	
	private ProductVariantsEntity getProductVariant(ProductImageUpdateIdentifier identifier, VariantCache cache) {
		String variantId = identifier.getVariantId();
		String externalId = identifier.getExternalId();
		String barcode = identifier.getBarcode();	
		return Stream
				.of(  cache.getIdToVariantMap().get(variantId)
					, cache.getExternalIdToVariantMap().get(externalId)
					, cache.getBarcodeToVariantMap().get(barcode))
				.filter(Objects::nonNull)
				.findFirst()
				.orElseThrow(() -> new RuntimeBusinessException(
										format(ERR_NO_VARIANT_FOUND, variantId, externalId, barcode)
										, "INVALID PARAM: csv"
										, HttpStatus.NOT_ACCEPTABLE));
	}
	
	


	private Map<String, ProductVariantsEntity> getBarcodeToVariantMap(
			Map<String, List<ProductImageUpdateIdentifier>> fileIdentifiersMap) {
		return 
			Flux
			.fromStream(fileIdentifiersMap.values().stream().flatMap(List::stream))	
			.filter(identifier -> isNotBlankOrNull(identifier.getBarcode()))
			.map(ProductImageUpdateIdentifier::getBarcode)
			.window(1000)
			.flatMap(this::getVariantsByBarcode)
			.collectMap(ProductVariantsEntity::getBarcode, variant -> variant)
			.block();
	}






	private Map<String, ProductVariantsEntity> getExternalIdToVariantMap(
			Map<String, List<ProductImageUpdateIdentifier>> fileIdentifiersMap) {
		return 
			Flux
			.fromStream(fileIdentifiersMap.values().stream().flatMap(List::stream))	
			.filter(identifier -> isNotBlankOrNull(identifier.getExternalId()))
			.map(ProductImageUpdateIdentifier::getExternalId)
			.window(1000)
			.flatMap(this::getVariantsByExternalId)
			.collectMap(variant-> variant.externalId, variant -> variant.variant)
			.block();
	}






	private Map<String, ProductVariantsEntity> getIdToVariantMap(
			Map<String, List<ProductImageUpdateIdentifier>> fileIdentifiersMap) {
		return 
			Flux
			.fromStream(fileIdentifiersMap.values().stream().flatMap(List::stream))	
			.filter(identifier -> isNotBlankOrNull(identifier.getVariantId()))
			.map(ProductImageUpdateIdentifier::getVariantId)
			.window(1000)
			.flatMap(this::getVariantsById)
			.collectMap(this::getIdAsString, variant -> variant)
			.block();
	}

	
	
	
	
	
	private Flux<ProductVariantsEntity> getVariantsById(Flux<String> idList){
		return 
			idList
			.map(Long::valueOf)
			.buffer()
			.flatMapIterable(productVariantsRepository::findByIdIn);
	}
	
	
	
	
	
	private Flux<ProductVariantsEntityWithExternalId> getVariantsByExternalId(Flux<String> idList){		
		return 
			idList
			.buffer()
			.flatMapIterable(this::getProductVariantFromExternalIdIn);
	}
	
	
	
	
	
	private Flux<ProductVariantsEntity> getVariantsByBarcode(Flux<String> barcodeList){
		Long orgId = securityService.getCurrentUserOrganizationId();
		return 
			barcodeList
			.buffer()
			.flatMapIterable(barcodes -> productVariantsRepository.findByOrganizationIdAndBarcodeIn(orgId, barcodes));
	}
	
	
	
	
	private List<ProductVariantsEntityWithExternalId> getProductVariantFromExternalIdIn(List<String> extIdList){
		Long orgId = securityService.getCurrentUserOrganizationId();
		Map<String,String> mapping = integrationService.getLocalMappedValues(orgId, PRODUCT_VARIANT, extIdList);
		Map<String,String> localToExtIdMapping = 
				mapping
				.entrySet()
				.stream()
				.collect(toMap(Map.Entry::getValue, Map.Entry::getKey));
		
		List<Long> variantIds = 
				localToExtIdMapping
				.keySet()
				.stream()
				.map(Long::valueOf)
				.collect(toList());
		
		return 
			productVariantsRepository
				.findByIdIn(variantIds)
				.stream()
				.map(variant -> toProductVariantEntityWithExtId(localToExtIdMapping, variant))
				.collect(toList());
	}






	private ProductVariantsEntityWithExternalId toProductVariantEntityWithExtId(Map<String, String> localToExtIdMapping,
			ProductVariantsEntity variant) {
		String extId = localToExtIdMapping.get(String.valueOf(variant.getId()));
		return new ProductVariantsEntityWithExternalId(variant , extId);
	}
	
	
	
	
	private String getIdAsString(ProductVariantsEntity variant) {
		return String.valueOf(variant.getId());
	}





	private Map<String, List<ImportedImage>> groupImagesByPath( List<ImportedImage> allImportedImgs) throws BusinessException {
		return allImportedImgs
				.stream()
				.collect(groupingBy(ImportedImage::getPath));
	}






	private void saveSingleImgToAllItsVariants(String fileName, Map<String, List<ImportedImage>> fileImgMetadataMap,
			List<String> errors, List<ProductImageUpdateResponse> responses) {
		MultipartFile file = getMulitpartFile(fileImgMetadataMap, fileName);			
		List<ProductImageUpdateDTO> imgMetaDataList = getImgsMetaDataList(fileImgMetadataMap, fileName);
		
		try {
			responses.addAll( saveNewProductImgsUsingSameUrl(file, imgMetaDataList) );
		}catch(Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);						
			fileImgMetadataMap.get(fileName)
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
			deleteImage( res.getImageId() );
		}
		
		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
	}
	
	



	private void throwExceptionWithErrorList(List<String> errors) throws BusinessException {
		JSONArray json = new JSONArray(errors);
		throw new BusinessException(
				ERR_PRODUCT_IMG_BULK_IMPORT
				, json.toString()
				, HttpStatus.INTERNAL_SERVER_ERROR);
	}




	private String createImgImportErrMsg(ImportedImage img, Exception e) {
		StringBuilder msg = new StringBuilder();
		msg.append( String.format("Error importing image file with name[%s]", img.getImage().getOriginalFilename()) );
		msg.append( System.getProperty("line.separator") );
		msg.append("Error Message: " + e.getMessage());
		String msgString = msg.toString();
		return msgString;
	}



	
	
	

	private List<ImportedImage> extractImgsToImport(@Valid MultipartFile zip, @Valid MultipartFile csv,
			@Valid ProductImageBulkUpdateDTO metaData) throws BusinessException {
		List<ImportedImage> imgs = new ArrayList<>();		
		List<String> errors = new ArrayList<>();		
		Map<String,List<ProductImageUpdateIdentifier>> fileIdentifiersMap = createFileToVariantIdsMap(csv);
		
		try(ZipInputStream stream = new ZipInputStream(zip.getInputStream())){	
			
			imgs = readZipStream(stream, metaData, fileIdentifiersMap, errors);
			
		}catch(Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new BusinessException(ERR_READ_ZIP, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
		if(!errors.isEmpty()) {
			String errorsJson = getErrorMsgAsJson(errors);
			throw new BusinessException(ERR_IMPORTING_IMGS, errorsJson , HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return imgs;
	}



	
	

	private List<ImportedImage> readZipStream(ZipInputStream stream, ProductImageBulkUpdateDTO metaData,
			Map<String, List<ProductImageUpdateIdentifier>> fileIdentifiersMap, List<String> errors) throws IOException {
		
		List<ImportedImage> imgs = new ArrayList<>();
		
		ZipEntry zipEntry = stream.getNextEntry();
		
		while (zipEntry != null) {						
			readImgsFromZipEntry(zipEntry, stream, metaData, fileIdentifiersMap, errors)
					.forEach(imgs::add);
			
		    zipEntry = stream.getNextEntry();
		}
		
		stream.closeEntry();
		
		return imgs;
	}


	
	
	

	
	/**
	 * read a single image in the zip file, and return one or more "ImportedImage" based on the barcode,
	 * as a single barcode can belong to both a product and a product variant.
	 * */
	private List<ImportedImage> readImgsFromZipEntry(ZipEntry zipEntry, ZipInputStream stream,
			ProductImageBulkUpdateDTO metaData, Map<String, List<ProductImageUpdateIdentifier>> fileIdentifiersMap, List<String> errors) {
		List<ImportedImage> imgsFromEntry = new ArrayList<>();
		
		if(zipEntry.isDirectory() ) {
			return new ArrayList<>();
		} 
		
		try {
			MultipartFile imgMultipartFile = readZipEntryAsMultipartFile(stream, zipEntry);				
			List<ProductImageUpdateDTO> imgsMetaData = createImportedImagesMetaData(zipEntry, fileIdentifiersMap, metaData);
			
			imgsFromEntry = imgsMetaData
								.stream()
								.map( meta -> new ImportedImage(imgMultipartFile, meta, zipEntry.getName()))
								.collect(toList());
									
		}catch(Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			errors.add( createZipEntryErrorMsg(zipEntry, e) );
		}
		return imgsFromEntry;
	}
	
	
	
	
	
	
	private String createZipEntryErrorMsg(ZipEntry zipEntry, Exception e) {
		return String.format(ERR_IMPORTING_IMG_FILE, zipEntry.getName(), e.getMessage());
	}


	
	
	
	private String getErrorMsgAsJson(List<String> errors) {
		JSONArray errorsJson = new JSONArray(errors);
		
		JSONObject main = new JSONObject();
		main.put("msg", ERR_CSV_PARSE_FAILURE);
		main.put("errors", errorsJson);
		
		return errorsJson.toString();
	}
	
	

	
	
	

	/**
	 * a single imported image is identified by barcode, which can be for a product record ,or a
	 * product variant record.
	 * So, we return metadata considering all cases:
	 * - barcode exists for product only
	 * - barcode exists for variant only
	 * - barcode exists for both a product and a variant
	 * */
	private List<ProductImageUpdateDTO> createImportedImagesMetaData(ZipEntry zipEntry,
																	 Map<String,List<ProductImageUpdateIdentifier>> fileIdentifiersMap,
																	 ProductImageBulkUpdateDTO metaData) throws BusinessException{
		
		List<ProductImageUpdateIdentifier> identifiers = getVariantIdentifiersForCompressedFile(zipEntry, fileIdentifiersMap);
		
		List<List<ProductImageUpdateDTO>> metaDataLists = new ArrayList<>();
		for(ProductImageUpdateIdentifier identifier: identifiers) {
			metaDataLists.add(createImportedImagesMetaData(metaData, identifier));
		}
		
		return metaDataLists
				.stream()
				.flatMap(List::stream)
				.collect(toList());
	}

	
	
	


	private List<ProductImageUpdateDTO> createImportedImagesMetaData(ProductImageBulkUpdateDTO metaData, ProductImageUpdateIdentifier identifier)
			throws BusinessException {
		Long orgId = securityService.getCurrentUserOrganizationId();

		Optional<ProductVariantsEntity> variant = empty();

		if (identifier.getVariantId() != null) {
			variant = productVariantsRepository.findById(Long.parseLong(identifier.getVariantId()));
			validateVariantExistance(variant, identifier.getVariantId());
		}


		if ( !variant.isPresent() && identifier.getExternalId() != null) {
			String localMappedValue = integrationService.getLocalMappedValue(orgId, PRODUCT_VARIANT, identifier.getExternalId());
			if (localMappedValue != null && StringUtils.validateUrl(localMappedValue, "[0-9]+")) {
				variant = productVariantsRepository.findById(Long.parseLong(localMappedValue));
				validateVariantExistance(variant, localMappedValue);
			}
			else {
				throw new BusinessException("Provided external_id("+identifier.getExternalId()+") doesn't match any mapped value!",
						"INVALID_PARAM: external_id", HttpStatus.NOT_ACCEPTABLE);
			}
				
		}

		if ( !variant.isPresent() && identifier.getBarcode() != null) {
			variant = productVariantsRepository.findByBarcodeAndProductEntity_OrganizationId(identifier.getBarcode(), orgId);
		}
		
		ProductImageUpdateDTO variantMetaData = 
				variant
					.map(var -> createImgMetaData(var, metaData))
					.orElse(null);

		if(variantMetaData == null) {
			throw new BusinessException(
					String.format(ERR_NO_PRODUCT_EXISTS_WITH_BARCODE, identifier.getBarcode(), orgId)
					, "INVALID PARAM:imgs_zip"
					, HttpStatus.NOT_ACCEPTABLE);
		}
		
		return
			asList( variantMetaData )
			  .stream()
			  .filter(java.util.Objects::nonNull)
			  .collect(toList());
	}

	
	
	
	void validateVariantExistance(Optional<ProductVariantsEntity> variantsEntity, String variantId) throws BusinessException {
		if (!variantsEntity.isPresent())
			throw new BusinessException("Provided variant_id("+variantId+") doesn't match any existing variant!", "INVALID_PARAM: variant_id", HttpStatus.NOT_ACCEPTABLE);
	}
	

	
	

	private List<ProductImageUpdateIdentifier> getVariantIdentifiersForCompressedFile(ZipEntry zipEntry, Map<String,
																List<ProductImageUpdateIdentifier>> fileToIdentifiersMap) {
		String fileName = zipEntry.getName();
		List<ProductImageUpdateIdentifier> identifiers = fileToIdentifiersMap.get(fileName);
		
		if(identifiers == null) {
			String barcode =  getBarcodeFromImgName(fileName);
			identifiers = new ArrayList<>();
			identifiers.add(new ProductImageUpdateIdentifier(barcode));
		}
		return identifiers;
	}
	
	
	
	
	
	private MultipartFile readZipEntryAsMultipartFile(ZipInputStream stream, ZipEntry zipEntry) throws Exception {
		String fileName = zipEntry.getName();
		FileItem fileItem = createFileItem(fileName);		
		readIntoFileItem(stream, fileItem);
		
		return new CommonsMultipartFile(fileItem);
	}
	
	
	
	
	
	private MultipartFile readUrlAsMultipartFile(String httpUrl, List<Byte> bytes){
		try {
			String fileName = Paths.get(new URI(httpUrl)).getFileName().toString();
			FileItem fileItem = createFileItem(fileName);
			readIntoFileItem(bytes, fileItem);			
			return new CommonsMultipartFile(fileItem);
		} catch (IOException | URISyntaxException e) {
			logger.logException(e, Level.SEVERE);
			throw new RuntimeException(e);
		}		
	}




	private void readIntoFileItem(ZipInputStream stream, FileItem fileItem) throws IOException {
		byte[] buffer = new byte[1024*100];
		OutputStream fos = fileItem.getOutputStream();
		int len;
		while ((len = stream.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
	}
	
	
	
	
	private void readIntoFileItem(List<Byte> bytes, FileItem fileItem) throws IOException {		
		OutputStream fos = fileItem.getOutputStream();
		Byte[] bytesArr = bytes.stream().toArray(Byte[]::new);
		fos.write(toPrimitive(bytesArr));
	}




	private FileItem createFileItem(String fileName) {				
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(1024*1024);
		FileItem fileItem = factory.createItem("image", "image/jpeg", false, fileName);
		return fileItem;
	}


	
	
	
	private ProductImageUpdateDTO createImgMetaData(ProductVariantsEntity variant, ProductImageBulkUpdateDTO metaData) {
		ProductImageUpdateDTO imgMetaData = new  ProductImageUpdateDTO();
		imgMetaData.setOperation(Operation.CREATE);
		imgMetaData.setPriority( metaData.getPriority() );
		imgMetaData.setType( metaData.getType() );
		imgMetaData.setProductId(variant.getProductEntity().getId());
		imgMetaData.setVariantId(variant.getId());
		
		return imgMetaData;
	}
	
	


	private String getBarcodeFromImgName(String fileName) {
		return com.google.common.io.Files.getNameWithoutExtension(fileName);
	}



	

	private Map<String, List<ProductImageUpdateIdentifier>> createFileToVariantIdsMap(MultipartFile csv) throws BusinessException {
		if(csv == null ||csv.isEmpty())
			return new HashMap<>();		

		List<Record> csvRecords = getCsvRecords(csv);
		Map<String, List<ProductImageUpdateIdentifier>> identifiersMap = new HashMap<>();
		String path;
		ProductImageUpdateIdentifier identifier;
		for(Record record:csvRecords) {
			path = normalizeZipPath(record.getString(3));
			identifier = new ProductImageUpdateIdentifier(record.getString(0), record.getString(1), record.getString(2));

			if (identifiersMap.get(path) == null)
				identifiersMap.put(path, new ArrayList<>());

			identifiersMap.get(path).add(identifier);
		}
		return identifiersMap;
	}




	private String normalizeZipPath(String path) {
		String normalized = path ;
		if(path.startsWith("/")) {
			normalized = path.replaceFirst("/", "");
		}
			
		return normalized;
	}



	private List<Record> getCsvRecords(MultipartFile csv) throws BusinessException {
		List<Record> allRecords = new ArrayList<>();
		
		CsvParserSettings settings = new CsvParserSettings();
		settings.setHeaderExtractionEnabled(true);
		CsvParser parser = new CsvParser(settings );		
		
		try{
			allRecords = parser.parseAllRecords(csv.getInputStream());
		}catch(Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);			
			throw new BusinessException(ERR_CSV_PARSE_FAILURE, "INTERNAL SERVER ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return allRecords;
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




	private void validateImgBulkZip(MultipartFile zip) throws BusinessException {
		if(zip.isEmpty()) {
			throw new BusinessException(
					"Provided Zip file has no data!"
					, "INVALID PARAM:imgs_zip"
					, HttpStatus.NOT_ACCEPTABLE);
		}
		
		String ext = com.google.common.io.Files.getFileExtension(zip.getOriginalFilename());
		if(!ext.equalsIgnoreCase("zip")) {
			throw new BusinessException(
					"Provided images archive is not ZIP file!"
					, "INVALID PARAM:imgs_zip"
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}

	
	
	
	
	@Override
	public String getProductCoverImage(Long productId) {
		return productImagesRepository
							.findByProductEntity_IdOrderByPriority(productId)
							.stream()
							.filter(Objects::nonNull)
							.filter(this::isProductCoverImage)
							.sorted( comparing(ProductImagesEntity::getId))
							.findFirst()
							.map(img-> img.getUri())
							.orElse(NO_IMG_FOUND_URL);
	}
	
	
	
	
	@Override
	public Map<Long,String> getProductsCoverImages(List<Long> productIds) {
		if(productIds == null || productIds.isEmpty()) {
			return new HashMap<>();
		}
		
		Map<Long,String> productImgs = productImagesRepository
											.findByProductEntity_IdInOrderByPriority(productIds)
											.stream()
											.filter(Objects::nonNull)
											.filter(this::isProductCoverImage)
											.collect( groupingBy(img -> img.getProductEntity().getId()))
											.entrySet()
											.stream()
											.map(this::getProductCoverImageUrlMapEntry)
											.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));	
		productIds.stream()
				.filter(id -> !productImgs.keySet().contains(id))
				.forEach(id -> productImgs.put(id, NO_IMG_FOUND_URL));
		
		return productImgs;
	}
	

	
	
	
	private Map.Entry<Long, String> getProductCoverImageUrlMapEntry(Map.Entry<Long, List<ProductImagesEntity>> mapEntry){
		String uri = Optional.ofNullable(mapEntry.getValue())
							.map(List::stream)
							.map(s -> s.sorted( comparing(ProductImagesEntity::getId)))
							.flatMap(s -> s.findFirst())
							.map(ProductImagesEntity::getUri)
							.orElse(NO_IMG_FOUND_URL);
				
		return new AbstractMap.SimpleEntry<>(mapEntry.getKey(), uri);
	}
	
	
	
	
	
	private Boolean isProductCoverImage(ProductImagesEntity img) {
		return Objects.equals(img.getPriority(),0);
	}
	
	





	@Override
	public List<ProductImgDetailsDTO> getProductImgs(Long productId) throws BusinessException {
		validateProductToFetchItsImgs(productId);
		
		return getProductAndVariantsImageEntities(productId)
				.stream()
				.map(this::toProductImgDetailsDTO)
				.collect(Collectors.toList());
	}






	private Set<ProductImagesEntity> getProductAndVariantsImageEntities(Long productId) {
		
		Set<Long> variandIds = 
				productRepository
					.findById(productId)
					.map(ProductEntity::getProductVariants)
					.orElse(emptySet()).stream()
					.map(ProductVariantsEntity::getId)
					.collect(toSet());
		
		Set<ProductImagesEntity> imgs = new HashSet<>();
		imgs.addAll(productImagesRepository.findByProductEntity_Id(productId));
		imgs.addAll(productImagesRepository.findByProductVariantsEntity_IdInOrderByPriority(variandIds));
		return imgs;
	}
	
	
	
	
	
	private void validateProductToFetchItsImgs(Long productId) throws BusinessException{
		if(!productRepository.existsById(productId)) {
			throw new BusinessException("INVALID PARAM: product_id", ERR_NO_PRODUCT_EXISTS_WITH_ID, HttpStatus.NOT_ACCEPTABLE);
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
		List<ImportedImage> importedImgs = fetchImgsToImportFromUrls(csv, metaData);;
		return saveImgsBulk(importedImgs);
	}
	
	
	
	
	
	private WebClient buildWebClient() {
		return WebClient
        		.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().wiretap(true)
                ))
                .build();
	}
}




@AllArgsConstructor
class ProductVariantsEntityWithExternalId {
	public ProductVariantsEntity variant;
	public String externalId;
}



@AllArgsConstructor
@Data
class VariantCache{    
	private Map<String, ProductVariantsEntity> idToVariantMap;
	private Map<String, ProductVariantsEntity> externalIdToVariantMap;
	private Map<String, ProductVariantsEntity> barcodeToVariantMap; 
}




@AllArgsConstructor
@Data
class VariantIdentifierAndUrlPair{
	private String url;
	private List<ProductImageUpdateIdentifier> identifier;
}
