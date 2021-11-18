package com.nasnav.service;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.utils.FunctionalUtils;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.ExtraAttributesEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import com.nasnav.service.model.importproduct.csv.CsvRow;

@Service
@Qualifier("excel")
public class ExcelDataImportServiceImpl extends AbstractCsvExcelDataImportService {

	private final Logger logger = Logger.getLogger(getClass());
	private static final Map<String,String> HEADER_NAME_TO_BEAN_PROPERTY_MAPPING =
			getHeaderNameToBeanPropertyMapping();

	@Override
	@Transactional
	public ImportProductContext importProductList(@Valid MultipartFile file, @Valid ProductListImportDTO importMetaData) throws RuntimeBusinessException, ImportProductException {
		validateProductImportMetaData(importMetaData);
		validateProductImporFile(file);

		ProductImportMetadata importMetadata = getImportMetaData(importMetaData);
		ImportProductContext initialContext = new ImportProductContext(emptyList(), importMetadata);

		List<CsvRow> rows = parseExcelFile(file, initialContext);

		List<ProductImportDTO> productsData =
				rows
					.stream()
					.map(CsvRow::toProductImportDto)
					.collect(toList());
		try {
			return dataImportService.importProducts(productsData, importMetadata);
		} catch (BusinessException e) {
			throw new RuntimeBusinessException(e);
		}

	}

	private List<CsvRow> parseExcelFile(MultipartFile file, ImportProductContext initialContext) throws ImportProductException {
		List<CsvRow> lines;
		try {
			Workbook wb = WorkbookFactory.create(file.getInputStream());
			Sheet sheet = wb.getSheetAt(0);
			validateFileHeader(sheet.getRow(0));
			lines = readImpDataLines(sheet);

			wb.close();
		} catch (Exception e) {
			logger.error(e);
			throw  new ImportProductException(e, initialContext);
		}

		return lines;
	}

	private void validateFileHeader(Row row) throws Exception {
		List<String > headers = new ArrayList<>();
		for (Cell cell: row) {
			headers.add( cell.getStringCellValue());
		}
		List<String> originalHeaders = getProductImportTemplateHeaders();
		String headerNotFound = originalHeaders.stream().filter(header -> !headers.contains(header)).map(Object::toString).collect(Collectors.joining(","));
		if(!headerNotFound.isEmpty()){
			throw new BusinessException(" Could not find fields : ["+headerNotFound+"]", "", HttpStatus.NOT_ACCEPTABLE);
		}

	}

	public List<CsvRow> readImpDataLines(Sheet sheet) throws InvocationTargetException, IllegalAccessException {
		List<CsvRow> lines = new ArrayList<>();
		List<String> featuresNames = featureRepo.findByOrganizationId(security.getCurrentUserOrganizationId())
				.stream().map(ProductFeaturesEntity::getName)
				.collect(toList());
		List<String> extraAttributesNames = extraAttrRepo.findByOrganizationId(security.getCurrentUserOrganizationId())
				.stream().map(ExtraAttributesEntity::getName)
				.collect(toList());

		for (Row row: sheet) {
			CsvRow line = new CsvRow();
			Map<String, String> features = new HashMap<>();
			Map<String, String> extraAttributes = new HashMap<>();
			if(row.getRowNum() ==0){
				continue; // skip header row
			}
			for (Cell cell : row) {
				String headerName = sheet.getRow(0).getCell(cell.getColumnIndex()).getStringCellValue();
				var propertyName = getColumnHeaderMapping(headerName);
				Object value = getCellValue(cell);
				if (value != null) {
					BeanUtils.setProperty(line, propertyName, value);
					if (featuresNames.contains(propertyName)) {
						features.put(propertyName, value.toString());
					}
					if (extraAttributesNames.contains(propertyName)) {
						extraAttributes.put(propertyName, value.toString());
					}
				}
			}
			line.setFeatures(features);
			line.setExtraAttributes(extraAttributes);
			lines.add(line);
		}
		return lines;
	}



	private static Map<String,String> getHeaderNameToBeanPropertyMapping() {
		return PRODUCT_DATA_TO_COLUMN_MAPPING
				.entrySet()
				.stream()
				.collect(toUnmodifiableMap(Map.Entry::getValue, Map.Entry::getKey, FunctionalUtils::getFirst));
	}



	private String getColumnHeaderMapping(String headerName) {
		return 	HEADER_NAME_TO_BEAN_PROPERTY_MAPPING.getOrDefault(headerName, headerName);
	}

	private static Object getCellValue(Cell cell ){
		switch (cell.getCellType())
		{
			case NUMERIC: {
				double number = cell.getNumericCellValue();
				if (number == (int) number) {
					return (int) number;
				}
				return BigDecimal.valueOf(cell.getNumericCellValue());
			}
			case STRING:
				return cell.getStringCellValue();
			case BOOLEAN:
				return Boolean.toString(cell.getBooleanCellValue());
		}
		return null;
	}

	@Override
	ByteArrayOutputStream writeFileHeaders(List<String> headers) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("NasNavProducts");

		AtomicInteger column = new AtomicInteger();
		Row row = sheet.createRow(0);

		headers.forEach(header ->  row.createCell(column.getAndIncrement()).setCellValue(header));

 		workbook.write(bos);
		workbook.close();
		return bos;
	}
}




