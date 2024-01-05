package com.nasnav.service.impl;

import com.nasnav.commons.converters.Converters;
import com.nasnav.commons.converters.DtoToCsvRowMapper;
import com.nasnav.dto.ProductImageDTO;
import com.nasnav.dto.VariantWithNoImagesDTO;
import com.nasnav.service.AbstractCsvExcelDataExportService;
import com.nasnav.service.helpers.ExcelDataFormatter;
import com.nasnav.service.helpers.ExcelDataValidator;
import com.nasnav.service.model.importproduct.csv.CsvRow;
import com.nasnav.service.model.importproduct.csv.OrderRow;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.nasnav.service.CsvExcelDataImportService.IMG_DATA_TO_COLUMN_MAPPING;
import static com.nasnav.service.CsvExcelDataImportService.ORDER_DATA_TO_COLUMN_MAPPING;
import static com.nasnav.service.CsvExcelDataImportService.PRODUCT_DATA_TO_COLUMN_MAPPING;
import static java.util.Optional.ofNullable;

@Service("excel")
public class ExcelDataExportServiceImpl extends AbstractCsvExcelDataExportService{

	private final Logger logger = Logger.getLogger(getClass());

	@Autowired
	private ExcelDataValidator excelDataValidator;
	@Autowired
	private ExcelDataFormatter excelDataFormatter;

	@Override
	protected ByteArrayOutputStream buildOrdersFile(List<String> headers, List<OrderRow> orders) throws IOException {
		return writeOrderFileResult(headers, orders);
	}

	protected ByteArrayOutputStream buildProductsFile(List<String> headers, List<CsvRow> products) throws IOException {

		return writeFileResult(headers, products);
	}

	protected ByteArrayOutputStream buildImagesFile(List<String> headers,
													List<ProductImageDTO> images) throws IOException {
		headers = headers
				.stream()
				.collect(Collectors.toCollection(ArrayList::new));
		return writeFileResult(headers, images);
	}


	protected ByteArrayOutputStream writeOrderFileResult(List<String> headers, List<?> data) throws IOException {
		DtoToCsvRowMapper dtoToCsvRowMapper = data.stream()
				.findAny()
				.map(Converters::getDtoToCsvRowConverterForBean)
				.orElse(null);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Orders");

		writeFileHeaders(headers, sheet);

		Map<Integer, String> indexToHeader = new HashMap<>();
		BidiMap map = ofNullable(dtoToCsvRowMapper)
				.map(DtoToCsvRowMapper::getColumnMapping)
				.map(TreeBidiMap::new)
				.orElse(new TreeBidiMap(ORDER_DATA_TO_COLUMN_MAPPING));

		for(int i =0; i< headers.size(); i++) {
			if (map.containsValue(headers.get(i))) {
				indexToHeader.put(i, map.getKey((headers.get(i))) +"");
			}
			else {
				indexToHeader.put(i, headers.get(i));
			}
		}

		if(dtoToCsvRowMapper == null)
			data.forEach(line ->  createNewRow(sheet, indexToHeader, (CsvRow) line));
		else
			data.forEach(line ->  createNewRow(sheet, indexToHeader, dtoToCsvRowMapper.map(line)));
		workbook.write(bos);
		workbook.close();

		return bos;
	}


	protected ByteArrayOutputStream writeFileResult(List<String> headers, List<?> data) throws IOException {
		DtoToCsvRowMapper dtoToCsvRowMapper = data.stream()
				.findAny()
				.map(Converters::getDtoToCsvRowConverterForBean)
				.orElse(null);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("NasNavProducts");

		writeFileHeaders(headers, sheet);

		Map<Integer, String> indexToHeader = new HashMap<>();
		BidiMap map = ofNullable(dtoToCsvRowMapper)
				.map(DtoToCsvRowMapper::getColumnMapping)
				.map(TreeBidiMap::new)
				.orElse(new TreeBidiMap(PRODUCT_DATA_TO_COLUMN_MAPPING));

		for(int i =0; i< headers.size(); i++) {
			if (map.containsValue(headers.get(i))) {
				indexToHeader.put(i, map.getKey((headers.get(i))) +"");
			}
			else {
				indexToHeader.put(i, headers.get(i));
			}
		}

		if(dtoToCsvRowMapper == null)
			data.forEach(line ->  createNewRow(sheet, indexToHeader, (CsvRow) line));
		else
			data.forEach(line ->  createNewRow(sheet, indexToHeader, dtoToCsvRowMapper.map(line)));

		if(addExcelDataValidation != null && addExcelDataValidation){
			excelDataValidator.addDataValidationsToSheet(sheet);
			excelDataFormatter.addConditionalFormattingToSheet(sheet);
			excelDataFormatter.addStyleFormattingToSheet(sheet);
		}
		workbook.write(bos);
		workbook.close();

		return bos;
	}

	private void writeFileHeaders(List<String> headers, XSSFSheet sheet) {
		AtomicInteger column = new AtomicInteger();
		Row row = sheet.createRow(0);

		headers.forEach(header ->  row.createCell(column.getAndIncrement()).setCellValue(header));
	}

	private void replaceSpecialColumns(List<String> headers, Map<String, String> specialColumns, String key) {
		headers.remove(key);
		headers.add(specialColumns.get(key));
	}

	private void createNewRow(XSSFSheet sheet, Map<Integer, String> indexToHeader, CsvRow line) {
		Row newRow = sheet.createRow(sheet.getLastRowNum()+1);
		for (Integer key: indexToHeader.keySet()) {
			String value = null;
			String columnName = indexToHeader.get(key);
			try {
				if (line.getFeatures().get(columnName) != null) {
					value = line.getFeatures().get(columnName);
				} else if (line.getExtraAttributes().get(columnName) != null) {
					value = line.getExtraAttributes().get(columnName);
				} else {
					value = BeanUtils.getProperty(line, columnName);
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			newRow.createCell(key).setCellValue(value);
		}
	}

	@Override
	protected ByteArrayOutputStream buildProductWithNoImgsFile(List<String> headers,
															   List<VariantWithNoImagesDTO> variants) throws IOException {

		Map<String, String> imgDataToColumnMapping = new HashMap<>(IMG_DATA_TO_COLUMN_MAPPING);

		removeSpecialColumns(imgDataToColumnMapping);

		return writeFileResult(headers, variants);
	}

	@Override
	protected ByteArrayOutputStream writeFileHeaders(List<String> headers) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("NasNavProducts");

		writeFileHeaders(headers, sheet);

		workbook.write(bos);
		workbook.close();

		return bos;
	}


}
