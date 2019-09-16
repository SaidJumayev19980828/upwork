package com.nasnav.service;

import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_INVALID_ENCODING;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_NO_FILE_UPLOADED;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_PRODUCT_IMPORT_MISSING_HEADER_NAME;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_PRODUCT_IMPORT_MISSING_PARAM;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_SHOP_ID_NOT_EXIST;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_USER_CANNOT_CHANGE_OTHER_ORG_SHOP;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_CSV_PARSE_FAILURE;
import static com.nasnav.persistence.EntityUtils.anyIsNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.beanutils.BeanMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.commons.model.dataimport.ProductImportCsvRowData;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.CsvHeaderNamesDTO;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.response.ProductListImportResponse;
import com.univocity.parsers.common.DataProcessingException;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.RowProcessorErrorHandler;
import com.univocity.parsers.common.fields.ColumnMapping;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import lombok.Data;

@Service
public class DataImportService {

	
	@Autowired
	private ShopsRepository shopRepo;
	
	
	@Autowired
	private EmployeeUserRepository empRepo;
	
	
	
	public ProductListImportResponse importProductListFromCSV(@Valid MultipartFile file,
			@Valid ProductListImportDTO importMetaData) throws BusinessException {
		
		validateProductImportMetaData(importMetaData);
		validateProductImportCsvFile(file);
		
		List<ProductImportCsvRowData> rows = parseCsvFile(file, importMetaData);
		
		return new ProductListImportResponse(Collections.emptyList());
	}
	
	
	
	

	private List<ProductImportCsvRowData> parseCsvFile(MultipartFile file, ProductListImportDTO metaData) throws BusinessException {
		
		List<ProductImportCsvRowData> rows = new ArrayList<>();
		
		ByteArrayInputStream in = readCsvFile(file);
		
		BeanListProcessor<ProductImportCsvRowData> rowProcessor = createRowProcessor(metaData);		
		RowParseErrorHandler rowParsingErrHandler = new RowParseErrorHandler();
		CsvParserSettings settings = createParsingSettings(rowProcessor, rowParsingErrHandler);
		
		CsvParser parser = new CsvParser(settings);
		
		try {
			parser.parse(in);
		}catch(Exception e) {
			throw new BusinessException(
					ERR_CSV_PARSE_FAILURE + " cause:\n" + e
					, "INVALID PARAM:csv"
					, HttpStatus.NOT_ACCEPTABLE); 
		}	
		
		if( !rowParsingErrHandler.getErrors().isEmpty() ) {
			throw new BusinessException(
					rowParsingErrHandler.getErrorMsgAsJson() 
					, "INVALID PARAM:csv"
					, HttpStatus.NOT_ACCEPTABLE); 
		}
		
		rows = rowProcessor.getBeans();
		
		return rows;
	}





	private CsvParserSettings createParsingSettings(BeanListProcessor<ProductImportCsvRowData> rowProcessor,
			RowParseErrorHandler rowParsingErrHandler) {
		CsvParserSettings settings = new CsvParserSettings();
		settings.setHeaderExtractionEnabled(true);
		settings.setProcessor(rowProcessor);		
		settings.setProcessorErrorHandler(rowParsingErrHandler);
		return settings;
	}





	private BeanListProcessor<ProductImportCsvRowData> createRowProcessor(ProductListImportDTO metaData) {
		ColumnMapping mapper = createAttrToColMapping(metaData);
		
		BeanListProcessor<ProductImportCsvRowData> rowProcessor = 
				new BeanListProcessor<ProductImportCsvRowData>(ProductImportCsvRowData.class);
		rowProcessor.setColumnMapper(mapper);
		rowProcessor.setStrictHeaderValidationEnabled(true);
		return rowProcessor;
	}





	private ByteArrayInputStream readCsvFile(MultipartFile file) throws BusinessException {
		byte[] bytes = new byte[0];
		try {
			bytes = file.getBytes();
		} catch (IOException e) {
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
		if(anyIsNull(headers.getBarcode(), headers.getName(), headers.getCategory(), headers.getPrice(), headers.getQuantity())) {
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

}




@Data
class RowParseErrorHandler implements RowProcessorErrorHandler {
	private List<String> errors;
	
	
	public RowParseErrorHandler() {
		errors = new ArrayList<>();
	}
	
	
	
	@Override
	public void handleError(DataProcessingException error, Object[] inputRow, ParsingContext context) {
			StringBuilder errMsg = new StringBuilder();
			String line1 = String.format("Error processing row[%d]: %s", context.currentLine(), Arrays.toString(inputRow));
			String line2 = String.format("Error details: column '%s' (index %d) has value '%s'", error.getColumnName(), error.getColumnIndex(), inputRow[error.getColumnIndex()]);
			String line3 = String.format("which caused error: %s", error.getMessage());
			errMsg.append(line1).append("\n")
				.append(line2).append("\n")
				.append(line3);
			errors.add( errMsg.toString());	
	}
	
	
	
	
	public String getErrorMsgAsJson() {
		JSONArray errorsJson = new JSONArray(errors);
		
		JSONObject main = new JSONObject();
		main.put("msg", ERR_CSV_PARSE_FAILURE);
		main.put("errors", errorsJson);
		
		return main.toString();
	}
}




