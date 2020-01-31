package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_CSV_PARSE_FAILURE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_INVALID_ENCODING;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_NO_FILE_UPLOADED;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_PRODUCT_IMPORT_MISSING_PARAM;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_SHOP_ID_NOT_EXIST;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_USER_CANNOT_CHANGE_OTHER_ORG_SHOP;
import static java.util.stream.Collectors.toList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.jboss.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.utils.MapBuilder;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.response.ProductListImportResponse;
import com.nasnav.service.helpers.ProductCsvRowProcessor;
import com.univocity.parsers.common.DataProcessingException;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.RowProcessorErrorHandler;
import com.univocity.parsers.common.fields.ColumnMapping;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import lombok.Data;

@Service
public class CsvDataImportServiceImpl implements CsvDataImportService {

	
	@Autowired
	private ShopsRepository shopRepo;
	
	
	@Autowired
	private EmployeeUserRepository empRepo;
	
	@Autowired
	private SecurityService security;
	
	
	@Autowired
	private ProductFeaturesRepository featureRepo;

	@Autowired
	private ProductFeaturesRepository prodcutFeaturesRepo;

	@Autowired
	private DataImportService dataImportService;
	
	
	private Logger logger = Logger.getLogger(getClass());

	
	
	
	private final Map<String,String> fieldToColumnHeaderMapping = 
			MapBuilder.<String, String>map()
				.put("name", "product_name")
				.put("barcode", "barcode")
				.put("tags", "tags")
				.put("brand", "brand")
				.put("price", "price")
				.put("quantity", "quantity")
				.put("description", "description")
				.put("variantId", "variant_id")
				.put("externalId", "external_id")
				.getMap();
	
	
	
	private final Set<String> csvBaseHeaders = new HashSet<String>(fieldToColumnHeaderMapping.values());
	
	

	@Transactional(rollbackFor = Throwable.class)
	public ProductListImportResponse importProductListFromCSV(@Valid MultipartFile file,
			@Valid ProductListImportDTO csvImportMetaData) throws BusinessException {

		validateProductImportMetaData(csvImportMetaData);
		validateProductImportCsvFile(file);

		List<CsvRow> rows = parseCsvFile(file, csvImportMetaData);
		ProductImportMetadata importMetadata = getImportMetaData(csvImportMetaData);
		
		List<ProductImportDTO> productsData = 
				rows
				.stream()
				.map(CsvRow::toProductImportDto)
				.collect(toList());
		return dataImportService.importProducts(productsData, importMetadata);

	}
	
	
	

	private ProductImportMetadata getImportMetaData(ProductListImportDTO csvImportMetaData) {
		ProductImportMetadata importMetadata = new ProductImportMetadata();

		importMetadata.setDryrun(csvImportMetaData.isDryrun());
		importMetadata.setUpdateProduct(csvImportMetaData.isUpdateProduct());
		importMetadata.setUpdateStocks(csvImportMetaData.isUpdateStocks());
		importMetadata.setShopId(csvImportMetaData.getShopId());
		importMetadata.setCurrency(csvImportMetaData.getCurrency());
		importMetadata.setEncoding(csvImportMetaData.getEncoding());

		return importMetadata;
	}
	
	
	
	

	private List<CsvRow> parseCsvFile(MultipartFile file, ProductListImportDTO metaData) throws BusinessException {
		List<ProductFeaturesEntity> orgFeatures = featureRepo.findByShopId( metaData.getShopId() );
		
		List<CsvRow> rows = new ArrayList<>();
		
		ByteArrayInputStream in = readCsvFile(file);		
		BeanListProcessor<CsvRow> rowProcessor = createRowProcessor(metaData, orgFeatures);
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





	private CsvParserSettings createParsingSettings(BeanListProcessor<CsvRow> rowProcessor,
			RowParseErrorHandler rowParsingErrHandler) {
		CsvParserSettings settings = new CsvParserSettings();
		settings.setHeaderExtractionEnabled(true);
		settings.setProcessor(rowProcessor);		
		settings.setProcessorErrorHandler(rowParsingErrHandler);
		return settings;
	}



	private BeanListProcessor<CsvRow> createRowProcessor(ProductListImportDTO metaData, List<ProductFeaturesEntity> orgFeatures) {
		List<String> defaultTemplateHeaders = getProductImportTemplateHeaders();
		
		ColumnMapping mapper = createAttrToColMapping(metaData);		
		
		BeanListProcessor<CsvRow> rowProcessor =
				new ProductCsvRowProcessor<CsvRow>(CsvRow.class, orgFeatures, defaultTemplateHeaders);
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
		ColumnMapping mapping = new ColumnMapping();
		mapping.attributesToColumnNames(fieldToColumnHeaderMapping);
		
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
		
		if( anyIsNull(shopId, encoding, currency)) {
			throw new BusinessException(
					ERR_PRODUCT_IMPORT_MISSING_PARAM
					, "MISSING PARAM"
					, HttpStatus.NOT_ACCEPTABLE);
		}		
		
		validateShopId(shopId);		
		
		validateEncodingCharset(encoding);		
		
		validateStockCurrency(currency);		
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
		List<String> baseHeaders = getProductImportTemplateHeaders();

		return writeCsvHeaders(baseHeaders);
	}




	@Override
	public List<String> getProductImportTemplateHeaders() {
		Long orgId = security.getCurrentUserOrganizationId();
		List<String> features = 
				prodcutFeaturesRepo
					.findByOrganizationId(orgId)
					.stream()
					.map(ProductFeaturesEntity::getName)
					.sorted()
					.collect(Collectors.toList());
		
		List<String> baseHeaders = new ArrayList<>(csvBaseHeaders);
		baseHeaders.addAll(features);
		return baseHeaders;
	}
	
	
	

	@Override
	public ByteArrayOutputStream generateImagesCsvTemplate() throws IOException{
		List<String> headers = Arrays.asList(new String[]{"variant_id","external_id","barcode","image_file"});

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
