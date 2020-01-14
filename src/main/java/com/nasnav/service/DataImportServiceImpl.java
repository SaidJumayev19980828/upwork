package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_BRAND_NAME_NOT_EXIST;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_CATEGORY_NAME_NOT_EXIST;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_CONVERT_TO_JSON;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_CSV_PARSE_FAILURE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_INVALID_ENCODING;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_NO_FILE_UPLOADED;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_PRODUCT_CSV_ROW_SAVE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_PRODUCT_IMPORT_MISSING_HEADER_NAME;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_PRODUCT_IMPORT_MISSING_PARAM;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_SHOP_ID_NOT_EXIST;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_USER_CANNOT_CHANGE_OTHER_ORG_SHOP;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_PREPARE_PRODUCT_DTO_DATA;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.univocity.parsers.csv.*;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtils;
import org.jboss.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.model.dataimport.ProductImportCsvRowData;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.CategoriesRepository;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.CsvHeaderNamesDTO;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.dto.ProductUpdateDTO;
import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.dto.VariantUpdateDTO;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.response.ProductListImportResponse;
import com.nasnav.response.ProductUpdateResponse;
import com.nasnav.response.VariantUpdateResponse;
import com.nasnav.service.helpers.ProductCsvRowProcessor;
import com.univocity.parsers.common.DataProcessingException;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.RowProcessorErrorHandler;
import com.univocity.parsers.common.fields.ColumnMapping;
import com.univocity.parsers.common.processor.BeanListProcessor;

import lombok.Data;

@Service
public class DataImportServiceImpl implements DataImportService{

	
	@Autowired
	private ShopsRepository shopRepo;
	
	
	@Autowired
	private EmployeeUserRepository empRepo;
	
	
	@Autowired
	private CategoriesRepository categoriesRepo;
	
	@Autowired
	private BrandsRepository brandRepo;
	
	
	@Autowired
	private ProductVariantsRepository variantRepo;
	
	@Autowired
	private SecurityService security;
	
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private StockService stockService;
	
	
	@Autowired
	private ProductFeaturesRepository featureRepo;
	
	
	@Autowired
	private ProductRepository prodcutRepo;

	@Autowired
	private ProductFeaturesRepository prodcutFeaturesRepo;
	
	
	private Logger logger = Logger.getLogger(getClass());

	private final List<String> csvBaseHeaders = Arrays.asList(new String[]{"product_name","barcode","brand","price","quantity","description"});

	@Transactional(rollbackFor = Throwable.class)
	public ProductListImportResponse importProductListFromCSV(@Valid MultipartFile file,
			@Valid ProductListImportDTO importMetaData) throws BusinessException {
		
		validateProductImportMetaData(importMetaData);
		validateProductImportCsvFile(file);
		
		List<ProductImportCsvRowData> rows = parseCsvFile(file, importMetaData);
		
		List<ProductCsvImportDTO> importedDtos = toProductImportDto(rows, importMetaData);
		
		saveToDB(importedDtos, importMetaData);
		
		
		if(importMetaData.isDryrun()) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}
		
		return new ProductListImportResponse(Collections.emptyList());
	}
	
	
	
	

	private void saveToDB(List<ProductCsvImportDTO> importedDtos, ProductListImportDTO importMetaData) throws BusinessException{
		List<String> errors = new ArrayList<>();
		
		for(int i=0; i < importedDtos.size(); i++) {
			ProductCsvImportDTO dto = importedDtos.get(i);
			try {
				saveSingleProductCsvRowToDB(dto, importMetaData);
			}catch(Exception e) {
				logger.error(e,e);
				
				StringBuilder msg = new StringBuilder();
				msg.append( String.format("Error at Row[%d], with data[%s]", i+1, dto.toString()) );
				msg.append( System.getProperty("line.separator") );
				msg.append("Error Message: " + e.getMessage());
				
				errors.add(msg.toString());
			}
		}
		
		if(!errors.isEmpty()) {
			JSONArray json = new JSONArray(errors);
			throw new BusinessException(
					ERR_PRODUCT_CSV_ROW_SAVE
					, json.toString()
					, HttpStatus.NOT_ACCEPTABLE); 
		}
		
	}
	
	
	
	
	private void saveSingleProductCsvRowToDB(ProductCsvImportDTO dto, ProductListImportDTO importMetaData) throws BusinessException{
		if(dto.isExisting()) {
			if( importMetaData.isUpdateProduct()) {
				Long productId = saveProductDto( dto.getProductDto() );
				productService.updateVariant(dto.getVariantDto());
			}
			
			if(importMetaData.isUpdateStocks()) {
				stockService.updateStock(dto.getStockDto());
			}
		}else {
			saveNewImportedProduct(dto);
		}
		
	}





	private void saveNewImportedProduct(ProductCsvImportDTO dto) throws BusinessException {
		Long productId = saveProductDto(dto.getProductDto());
		dto.getVariantDto().setProductId(productId);
		VariantUpdateResponse variantResponse = productService.updateVariant(dto.getVariantDto());
		Long variantId = variantResponse.getVariantId();
		
		dto.getStockDto().setVariantId(variantId);
		stockService.updateStock(dto.getStockDto());
	}





	private Long saveProductDto(ProductUpdateDTO dto) throws BusinessException {
		String productDtoJson = getProductDtoJson(dto);		
		ProductUpdateResponse productResponse = productService.updateProduct(productDtoJson, false);
		Long productId = productResponse.getProductId();
		return productId;
	}





	private String getProductDtoJson(ProductUpdateDTO dto) throws BusinessException {
		ProductUpdateDTO dtoClone = prepareProductUpdateDto(dto);		
		String productDtoJson = "";
		try {	
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			productDtoJson = mapper.writeValueAsString(dtoClone);
		}catch(Exception e) {
			logger.error(e,e);
			throw new BusinessException(
					String.format(ERR_CONVERT_TO_JSON, dtoClone.getClass().getName())
					, "INTERNAL SERVER ERROR"
					, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return productDtoJson;
	}





	private ProductUpdateDTO prepareProductUpdateDto(ProductUpdateDTO dto) throws BusinessException {
		try {
			ProductUpdateDTO dtoClone = (ProductUpdateDTO)BeanUtils.cloneBean(dto);
			Optional<ProductEntity> product = prodcutRepo.findByName(dto.getName());
			if(product.isPresent()) {
				dtoClone.setId( product.get().getId());
				dtoClone.setOperation(Operation.UPDATE);
			}
			return dtoClone;
		} catch (Exception e) {
			logger.error(e,e);
			throw new BusinessException(
					String.format( ERR_PREPARE_PRODUCT_DTO_DATA, dto.toString())
					, "INTERNAL SERVER ERROR"
					, HttpStatus.INTERNAL_SERVER_ERROR);
		}		
	}




	private List<ProductCsvImportDTO> toProductImportDto(List<ProductImportCsvRowData> rows, ProductListImportDTO importMetaData) throws BusinessException{
		List<ProductCsvImportDTO> dtoList = new ArrayList<>();
		for(ProductImportCsvRowData row: rows) {
			dtoList.add( toProductImportDto(row, importMetaData) );
		}
		
		return dtoList;
	}
	
	
	
	
	
	private ProductCsvImportDTO toProductImportDto(ProductImportCsvRowData row, ProductListImportDTO importMetaData) throws BusinessException{
		ProductCsvImportDTO dto = new ProductCsvImportDTO();
		
		dto.setOriginalRowData( row.toString() );
		dto.setProductDto( createProductDto(row) );
		dto.setVariantDto( createVariantDto(row) );
		dto.setStockDto( createStockDto(row, importMetaData) );
		
		Long orgId = security.getCurrentUserOrganizationId();
		Optional<ProductVariantsEntity> variantEnt = variantRepo.findByBarcodeAndProductEntity_OrganizationId(row.getBarcode(), orgId);
		
		if(variantEnt != null && variantEnt.isPresent()) {
			modifyProductCsvImportDtoForUpdate(dto, row, variantEnt.get() );
		}
		
		return dto;
	}





	private void modifyProductCsvImportDtoForUpdate(ProductCsvImportDTO dto, ProductImportCsvRowData row, ProductVariantsEntity variantEnt)
			throws BusinessException {
		dto.setExisting(true);
		
		ProductUpdateDTO product = dto.getProductDto();
		VariantUpdateDTO variant = dto.getVariantDto();
		StockUpdateDTO stock = dto.getStockDto();
		
		Long productId = variantEnt.getProductEntity().getId();
		variant.setProductId(productId);
		variant.setVariantId(variantEnt.getId());			
		variant.setOperation( Operation.UPDATE );
		
		product.setOperation( Operation.UPDATE );
		product.setId(productId);
		
		stock.setVariantId(variantEnt.getId());
	}





	private StockUpdateDTO createStockDto(ProductImportCsvRowData row, ProductListImportDTO importMetaData) {
		StockUpdateDTO stock = new StockUpdateDTO();
		stock.setCurrency( importMetaData.getCurrency() );
		stock.setShopId( importMetaData.getShopId() );
		stock.setPrice( row.getPrice() );
		stock.setQuantity( row.getQuantity() );		
		return stock;
	}





	private VariantUpdateDTO createVariantDto(ProductImportCsvRowData row) {
		String features = Optional.ofNullable(row.getFeatures())
								.map(JSONObject::new)
								.map(JSONObject::toString)
								.orElse(null);
		
		VariantUpdateDTO variant = new VariantUpdateDTO();
		variant.setBarcode( row.getBarcode() );		
		variant.setFeatures("{}");
		variant.setDescription( row.getDescription() );
		variant.setName(row.getName() );
		variant.setOperation( Operation.CREATE );
		variant.setPname(row.getPname());
		if(features != null) {
			variant.setFeatures( features);
		}
			
		return variant;
	}





	private ProductUpdateDTO createProductDto(ProductImportCsvRowData row) throws BusinessException {
		
		Long brandId = brandRepo.findByName(row.getBrand());
		if(brandId == null) {
			throw new BusinessException(
					String.format(ERR_BRAND_NAME_NOT_EXIST, row.getBrand())
					, "INVALID DATA:brand"
					, HttpStatus.NOT_ACCEPTABLE);
		}
		
		
		
		ProductUpdateDTO product = new ProductUpdateDTO();
		product.setBrandId(brandId);
		product.setDescription( row.getDescription() );
		product.setBarcode(row.getBarcode());
		product.setName(row.getName() );
		product.setOperation(  Operation.CREATE );
		product.setPname(row.getPname());
		return product;
	}





	private List<ProductImportCsvRowData> parseCsvFile(MultipartFile file, ProductListImportDTO metaData) throws BusinessException {
		List<ProductFeaturesEntity> orgFeatures = featureRepo.findByShopId( metaData.getShopId() );
		
		List<ProductImportCsvRowData> rows = new ArrayList<>();
		
		ByteArrayInputStream in = readCsvFile(file);		
		BeanListProcessor<ProductImportCsvRowData> rowProcessor = createRowProcessor(metaData, orgFeatures);		
		RowParseErrorHandler rowParsingErrHandler = new RowParseErrorHandler();
		CsvParserSettings settings = createParsingSettings(rowProcessor, rowParsingErrHandler);
		
		CsvParser parser = new CsvParser(settings);
		
		runCsvParser(in, rowParsingErrHandler, parser);
		
		rows = rowProcessor.getBeans();
		
		return rows;
	}





	private void runCsvParser(ByteArrayInputStream in, RowParseErrorHandler rowParsingErrHandler, CsvParser parser)
			throws BusinessException {
		try {
			parser.parse(in);
		}catch(Exception e) {
			logger.error(e,e);
			throw new BusinessException(
					ERR_CSV_PARSE_FAILURE + " cause:\n" + e
					, "INVALID PARAM:csv"
					, HttpStatus.NOT_ACCEPTABLE); 
		}	
		
		if( !rowParsingErrHandler.getErrors().isEmpty() ) {
			throw new BusinessException(
					ERR_CSV_PARSE_FAILURE 
					, rowParsingErrHandler.getErrorMsgAsJson()
					, HttpStatus.NOT_ACCEPTABLE); 
		}
	}





	private CsvParserSettings createParsingSettings(BeanListProcessor<ProductImportCsvRowData> rowProcessor,
			RowParseErrorHandler rowParsingErrHandler) {
		CsvParserSettings settings = new CsvParserSettings();
		settings.setHeaderExtractionEnabled(true);
		settings.setProcessor(rowProcessor);		
		settings.setProcessorErrorHandler(rowParsingErrHandler);
		return settings;
	}



	private BeanListProcessor<ProductImportCsvRowData> createRowProcessor(ProductListImportDTO metaData, List<ProductFeaturesEntity> features) {
		ColumnMapping mapper = createAttrToColMapping(metaData);
		
		BeanListProcessor<ProductImportCsvRowData> rowProcessor = 
				new ProductCsvRowProcessor<ProductImportCsvRowData>(ProductImportCsvRowData.class, features);
		rowProcessor.setColumnMapper(mapper);
		rowProcessor.setStrictHeaderValidationEnabled(true);
		return rowProcessor;
	}





	private ByteArrayInputStream readCsvFile(MultipartFile file) throws BusinessException {
		byte[] bytes = new byte[0];
		try {
			bytes = file.getBytes();
		} catch (IOException e) {
			logger.error(e,e);
			throw new BusinessException(
					"Failed To read Products CSV file! cause:\n" + e
					, "INTERNAL SERVER ERROR"
					, HttpStatus.INTERNAL_SERVER_ERROR); 
		}
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		return in;
	}





	private ColumnMapping createAttrToColMapping(ProductListImportDTO metaData) {
		Map<Object,Object> beanMap = new BeanMap(metaData.getHeaders());
		Map<String,String> attrToColumnMap = beanMap.entrySet()
													.stream()
													.collect(
														Collectors.toMap(
																e -> e.getKey().toString()
																,e-> e.getValue().toString()
																)
													  );
		
		ColumnMapping mapping = new ColumnMapping();
		mapping.attributesToColumnNames(attrToColumnMap);
		
		return mapping;
	}





	private void validateProductImportCsvFile(@Valid MultipartFile file) throws BusinessException{
		if(file == null || file.isEmpty()) {
			throw new BusinessException(
					ERR_NO_FILE_UPLOADED
					, "INVALID PARAM"
					, HttpStatus.NOT_ACCEPTABLE); 
		}
		
	}





	private void validateProductImportMetaData(@Valid ProductListImportDTO metaData) throws BusinessException{
		Long shopId = metaData.getShopId();
		String encoding = metaData.getEncoding();
		Integer currency = metaData.getCurrency();
		CsvHeaderNamesDTO headerNames = metaData.getHeaders();
		
		if( anyIsNull(shopId, encoding, currency, headerNames)) {
			throw new BusinessException(
					ERR_PRODUCT_IMPORT_MISSING_PARAM
					, "MISSING PARAM"
					, HttpStatus.NOT_ACCEPTABLE);
		}		
		
		validateShopId(shopId);		
		
		validateEncodingCharset(encoding);		
		
		validateStockCurrency(currency);
		
		validateCsvHeaderNames( headerNames);
	}





	private void validateCsvHeaderNames(CsvHeaderNamesDTO headers) throws BusinessException{
		if(anyIsNull(headers.getBarcode(), headers.getName(), headers.getPrice(), headers.getQuantity())) {
			throw new BusinessException(
					ERR_PRODUCT_IMPORT_MISSING_HEADER_NAME 
					, "MISSING PARAM:csv-header(s)"
					, HttpStatus.NOT_ACCEPTABLE);
		}
		
	}





	private void validateEncodingCharset(String encoding) throws BusinessException {
		try {
			if( !Charset.isSupported(encoding)) {
				throw new IllegalStateException();
			}			
		}catch(Exception e) {
			logger.error(e,e);
			throw new BusinessException(
					String.format(ERR_INVALID_ENCODING, encoding)
					, "MISSING PARAM:encoding"
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}





	private void validateShopId(Long shopId) throws BusinessException {
		if( !shopRepo.existsById( shopId ) ) {
			throw new BusinessException(
					String.format(ERR_SHOP_ID_NOT_EXIST, shopId)
					, "MISSING PARAM:shop_id"
					, HttpStatus.NOT_ACCEPTABLE);
		}
		
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		EmployeeUserEntity user =  empRepo.getOneByEmail(auth.getName());
		Long userOrgId = user.getOrganizationId();
		
		ShopsEntity shop = shopRepo.findById(shopId).get();
		Long shopOrgId = shop.getOrganizationEntity().getId();
		
		if(!Objects.equals(shopOrgId, userOrgId)) {
			throw new BusinessException(
					String.format(ERR_USER_CANNOT_CHANGE_OTHER_ORG_SHOP, userOrgId, shopOrgId)
					, "MISSING PARAM:shop_id"
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	
	
	
	
	private void validateStockCurrency(Integer currency) throws BusinessException {
		boolean invalidCurrency = Arrays.asList( TransactionCurrency.values() )
										.stream()
										.map(TransactionCurrency::getValue)
										.map(Integer::valueOf)
										.noneMatch(val -> Objects.equals(currency, val));
		if(invalidCurrency ) {
			throw new BusinessException(
					String.format("Invalid Currency code [%d]!", currency)
					, "INVALID_PARAM:currency" 
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	
	
	

	private CsvWriterSettings createWritingSettings() {
		CsvWriterSettings settings = new CsvWriterSettings();
		return settings;
	}
	
	
	

	private ByteArrayOutputStream writeCsvHeaders(List<String> headers) throws IOException {
		ByteArrayOutputStream csvResult = new ByteArrayOutputStream();
		Writer outputWriter = new OutputStreamWriter(csvResult);

		CsvWriter writer = new CsvWriter(outputWriter, createWritingSettings());

		writer.writeHeaders(headers);
		writer.close();
		csvResult.close();

		return csvResult;
	}

	@Override
	public ByteArrayOutputStream generateProductsCsvTemplate() throws IOException{
		Long orgId = security.getCurrentUserOrganizationId();
		List<String> features = prodcutFeaturesRepo.findByOrganizationId(orgId)
													.stream()
													.map(ProductFeaturesEntity::getName)
													.sorted()
													.collect(Collectors.toList());
		
		List<String> baseHeaders = new ArrayList<>(csvBaseHeaders);
		baseHeaders.addAll(features);

		return writeCsvHeaders(baseHeaders);
	}
	
	
	

	@Override
	public ByteArrayOutputStream generateImagesCsvTemplate() throws IOException{
		List<String> headers = Arrays.asList(new String[]{"barcode","image_file"});

		return writeCsvHeaders(headers);
	}
}




@Data
class RowParseErrorHandler implements RowProcessorErrorHandler {
	private List<String> errors;
	
	
	public RowParseErrorHandler() {
		errors = new ArrayList<>();
	}
	
	
	
	@Override
	public void handleError(DataProcessingException error, Object[] inputRow, ParsingContext context) {
			errors.add( error.getMessage());	
	}
	
	
	
	
	public String getErrorMsgAsJson() {
		JSONArray errorsJson = new JSONArray(errors);
		
		JSONObject main = new JSONObject();
		main.put("msg", ERR_CSV_PARSE_FAILURE);
		main.put("errors", errorsJson);
		
		return errorsJson.toString();
	}
}






@Data
class ProductCsvImportDTO{
	private VariantUpdateDTO variantDto;
	private ProductUpdateDTO productDto;
	private StockUpdateDTO stockDto;
	private boolean existing;
	private String originalRowData;
	
	public ProductCsvImportDTO() {
		variantDto = new VariantUpdateDTO();
		productDto = new ProductUpdateDTO();
		stockDto = new StockUpdateDTO();
		existing = false;
		originalRowData = "[]";
	}
}
