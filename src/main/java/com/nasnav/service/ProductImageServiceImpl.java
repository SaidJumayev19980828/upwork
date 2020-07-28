package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.firstExistingValueOf;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.commons.utils.StringUtils.startsWithAnyOfAndIgnoreCase;
import static com.nasnav.constatnts.EntityConstants.Operation.CREATE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_CSV_PARSE_FAILURE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_IMPORTING_IMGS;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_IMPORTING_IMG_FILE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_NO_IMG_DATA_PROVIDED;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_NO_IMG_IMPORT_RESPONSE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_NO_PRODUCT_EXISTS_WITH_ID;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_NO_VARIANT_FOUND;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_READ_ZIP;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_USER_CANNOT_MODIFY_PRODUCT;
import static com.nasnav.exceptions.ErrorCodes.P$PRO$0001;
import static com.nasnav.service.CsvDataImportService.IMG_CSV_HEADER_BARCODE;
import static com.nasnav.service.CsvDataImportService.IMG_CSV_HEADER_EXTERNAL_ID;
import static com.nasnav.service.CsvDataImportService.IMG_CSV_HEADER_IMAGE_FILE;
import static com.nasnav.service.CsvDataImportService.IMG_CSV_HEADER_VARIANT_ID;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.logging.Level.SEVERE;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.validation.Valid;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dto.ProductImageBulkUpdateDTO;
import com.nasnav.dto.ProductImageDTO;
import com.nasnav.dto.ProductImageUpdateDTO;
import com.nasnav.dto.ProductImgDetailsDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportImageBulkRuntimeException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductImagesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.response.ProductImageDeleteResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.service.helpers.CachingHelper;
import com.nasnav.service.model.ImportedImage;
import com.nasnav.service.model.VariantBasicData;
import com.nasnav.service.model.VariantCache;
import com.nasnav.service.model.VariantIdentifier;
import com.nasnav.service.model.VariantIdentifierAndUrlPair;
import com.sun.istack.logging.Logger;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.netty.http.client.HttpClient;



@Service
public class ProductImageServiceImpl implements ProductImageService {

	private static final int IMG_DOWNLOAD_TIMEOUT_SEC = 90;

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
	private CachingHelper cachingHelper;
	

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
		Set<ImportedImage> importedImgs = new HashSet<>(extractImgsToImport(zip, csv, metaData));
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
			deleteOrgProductmages();
		}
		return saveImgsBulk(importedImgs);
	}






	private void deleteOrgProductmages() {
		Long orgId = securityService.getCurrentUserOrganizationId();
		productImagesRepository.deleteByProductEntity_organizationId(orgId);
	}
	
	
	
	
	





	private Set<ImportedImage> fetchImgsToImportFromUrls(@Valid MultipartFile csv,
			@Valid ProductImageBulkUpdateDTO metaData) throws BusinessException {				
				
		Map<String,List<VariantIdentifier>> fileIdentifiersMap = createFileToVariantIdsMap(csv);
		
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
					.map(identifier -> getProductVariant(identifier, variantCache, metaData))
					.filter(Optional::isPresent)
					.map(Optional::get)
					.map(variant -> createImgMetaData(metaData, variant));
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
	
	
	
	
	
	private ProductImageUpdateDTO createImgMetaData(ProductImageBulkUpdateDTO metaData, VariantBasicData variant) {
		Long variantId = variant.getVariantId();
		Long productId = variant.getProductId();
		
		ProductImageUpdateDTO imgMetaData = new ProductImageUpdateDTO();
		imgMetaData.setOperation(CREATE);
		imgMetaData.setPriority(metaData.getPriority());
		imgMetaData.setType(metaData.getType());	
		imgMetaData.setProductId(productId);
		
		if(!Objects.equals(metaData.getType(), PRODUCT_IMAGE)) {
			imgMetaData.setVariantId(variantId);
		}
		
		return imgMetaData;		
	}
	
	
	
	
	private Optional<VariantBasicData> getProductVariant(VariantIdentifier identifier, VariantCache cache, ProductImageBulkUpdateDTO metaData) {
		Optional<VariantBasicData> variant = getVariantFromCache(identifier, cache);
		
		Boolean isIgnoreErrors = ofNullable(metaData.isIgnoreErrors()).orElse(false);
		if(!isIgnoreErrors && !variant.isPresent()) {
			throw new RuntimeBusinessException(
					format(ERR_NO_VARIANT_FOUND, identifier.getVariantId(), identifier.getExternalId(), identifier.getBarcode())
					, "INVALID PARAM: csv"
					, NOT_ACCEPTABLE);
		}
		return variant;
	}






	private Optional<VariantBasicData> getVariantFromCache(VariantIdentifier identifier, VariantCache cache) {
		String variantId = identifier.getVariantId();
		String externalId = identifier.getExternalId();
		String barcode = identifier.getBarcode();
		return firstExistingValueOf(
				cache.getIdToVariantMap().get(variantId)
				, cache.getExternalIdToVariantMap().get(externalId)
				, cache.getBarcodeToVariantMap().get(barcode));
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
			logger.log(Level.SEVERE, e.getMessage(), e);						
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
			deleteImage( res.getImageId() );
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



	
	
	

	private List<ImportedImage> extractImgsToImport(@Valid MultipartFile zip, @Valid MultipartFile csv,
			@Valid ProductImageBulkUpdateDTO metaData) throws BusinessException {
		List<ImportedImage> imgs = new ArrayList<>();		
		List<String> errors = new ArrayList<>();		
		Map<String,List<VariantIdentifier>> fileIdentifiersMap = createFileToVariantIdsMap(csv);
		
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
			Map<String, List<VariantIdentifier>> fileIdentifiersMap, List<String> errors) throws IOException {
		
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
			ProductImageBulkUpdateDTO metaData, Map<String, List<VariantIdentifier>> fileIdentifiersMap, List<String> errors) {
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
																	 Map<String,List<VariantIdentifier>> fileIdentifiersMap,
																	 ProductImageBulkUpdateDTO metaData) throws BusinessException{
		
		List<VariantIdentifier> identifiers = getVariantIdentifiersForCompressedFile(zipEntry, fileIdentifiersMap);
		VariantCache cache = cachingHelper.createVariantCache(identifiers);
		
		List<List<ProductImageUpdateDTO>> metaDataLists = new ArrayList<>();
		for(VariantIdentifier identifier: identifiers) {
			List<ProductImageUpdateDTO> imgsMetaData = createImportedImagesMetaData(metaData, identifier, cache); 
			metaDataLists.add(imgsMetaData);
		}
		
		return metaDataLists
				.stream()
				.flatMap(List::stream)
				.collect(toList());
	}
	
	
	

	private List<ProductImageUpdateDTO> createImportedImagesMetaData(ProductImageBulkUpdateDTO metaData
			, VariantIdentifier identifier
			, VariantCache cache)
			throws BusinessException {
		Optional<VariantBasicData> variant = getProductVariant(identifier, cache, metaData);
		
		return variant
				.map(var -> createImgMetaData(metaData, var))
				.map(Arrays::asList)
				.orElse(emptyList());
	}

	
	
	
	void validateVariantExistance(Optional<ProductVariantsEntity> variantsEntity, String variantId) throws BusinessException {
		if (!variantsEntity.isPresent())
			throw new BusinessException("Provided variant_id("+variantId+") doesn't match any existing variant!", "INVALID_PARAM: variant_id", HttpStatus.NOT_ACCEPTABLE);
	}
	

	
	

	private List<VariantIdentifier> getVariantIdentifiersForCompressedFile(ZipEntry zipEntry, Map<String,
																List<VariantIdentifier>> fileToIdentifiersMap) {
		String fileName = zipEntry.getName();
		List<VariantIdentifier> identifiers = fileToIdentifiersMap.get(fileName);
		
		if(identifiers == null) {
			String barcode =  getBarcodeFromImgName(fileName);
			identifiers = new ArrayList<>();
			identifiers.add(new VariantIdentifier(barcode));
		}
		return identifiers;
	}
	
	
	
	
	
	private MultipartFile readZipEntryAsMultipartFile(ZipInputStream stream, ZipEntry zipEntry) throws Exception {
		String fileName = zipEntry.getName();
		FileItem fileItem = createFileItem(fileName);		
		readIntoFileItem(stream, fileItem);
		
		return new CommonsMultipartFile(fileItem);
	}
	
	
	
	
	
	private MultipartFile readUrlAsMultipartFile(String httpUrl, byte[] bytes){
		try {
			String extension = getFileExtension(bytes);
			String fileName = createImageFileNameFromUrl(httpUrl, extension);
			FileItem fileItem = createFileItem(fileName);
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




	private void readIntoFileItem(ZipInputStream stream, FileItem fileItem) throws IOException {
		byte[] buffer = new byte[1024*100];
		OutputStream fos = fileItem.getOutputStream();
		int len;
		while ((len = stream.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
	}
	
	
	
	
	private void readIntoFileItem(byte[] bytes, FileItem fileItem) throws IOException {		
		OutputStream fos = fileItem.getOutputStream();
		fos.write(bytes);
	}




	private FileItem createFileItem(String fileName) {				
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(1024*1024);
		FileItem fileItem = factory.createItem("image", "image/jpeg", false, fileName);
		return fileItem;
	}


	
	


	private String getBarcodeFromImgName(String fileName) {
		return com.google.common.io.Files.getNameWithoutExtension(fileName);
	}



	

	private Map<String, List<VariantIdentifier>> createFileToVariantIdsMap(MultipartFile csv) throws BusinessException {
		if(csv == null ||csv.isEmpty())
			return new HashMap<>();		

		List<Record> csvRecords = getCsvRecords(csv);
		Map<String, List<VariantIdentifier>> identifiersMap = new HashMap<>();
		String path;
		VariantIdentifier identifier;
		for(Record record:csvRecords) {
			path = normalizeZipPath(record.getString(IMG_CSV_HEADER_IMAGE_FILE));
			identifier = new VariantIdentifier(
								record.getString(IMG_CSV_HEADER_VARIANT_ID)
								, record.getString(IMG_CSV_HEADER_EXTERNAL_ID)
								, record.getString(IMG_CSV_HEADER_BARCODE));

			if (identifiersMap.get(path) == null)
				identifiersMap.put(path, new ArrayList<>());

			identifiersMap.get(path).add(identifier);
		}
		return identifiersMap;
	}




	private String normalizeZipPath(String path) {
		return ofNullable(path)
				.map(p -> p.startsWith("/")? p.replaceFirst("/", "") : p)
				.orElse("") ;
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
				.orElse(ProductImageService.NO_IMG_FOUND_URL);
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
		
		deleteOrgProductmages();
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


	private Map.Entry<Long, String> getProductCoverImageUrlMapEntry(Map.Entry<Long, List<ProductImageDTO>> mapEntry){
		String uri = ofNullable(mapEntry.getValue())
				.map(List::stream)
				.flatMap(s -> s.findFirst())
				.map(ProductImageDTO::getImagePath)
				.orElse(null);

		return new AbstractMap.SimpleEntry<>(mapEntry.getKey(), uri);
	}






	@Override
	public Map<Long, Optional<String>> getVariantsCoverImages(List<Long> variantsIds) {
		List<VariantBasicData> variantData = productVariantsRepository.findVariantBasicDataByIdIn(variantsIds);
		List<Long> productsIds = 
				variantData
				.stream()
				.map(VariantBasicData::getProductId)
				.collect(toList());
		List<ProductImageDTO> allImgs = productImagesRepository.getProductsAndVariantsImages(productsIds, variantsIds);
		
		return variantData
				.stream()
				.map(variant -> getVariantImgs(variant, allImgs))
				.map(this::electVariantCoverImage)
				.collect(toMap(VariantCoverImg::getVariantId, VariantCoverImg::getImgUrl));
	}






	private VariantBasicDataWithImgs getVariantImgs(VariantBasicData variant, List<ProductImageDTO> allImgs) {
		return allImgs
				.stream()
				.filter(img -> imgBelongToVariantOrItsProduct(variant, img))
				.collect(
						collectingAndThen(
								toList()
								, imgs -> new VariantBasicDataWithImgs(variant, imgs)));
	}






	private boolean imgBelongToVariantOrItsProduct(VariantBasicData variant, ProductImageDTO img) {
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
}








@Data
@AllArgsConstructor
class VariantBasicDataWithImgs{
	private VariantBasicData variant;
	private List<ProductImageDTO> imgs;
}



@Data
@AllArgsConstructor
class VariantCoverImg{
	private Long variantId;
	private Optional<String> imgUrl;
}


