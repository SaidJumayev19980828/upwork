package com.nasnav.service;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.exceptions.RuntimeBusinessException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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

	@Override
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
		int rowIterator =0;
		for (Row row: sheet) {
			CsvRow line = new CsvRow();
			if(rowIterator ==0){
				rowIterator++;
				continue; // skip header row
			}
			int cellIterator = 0;
			for (Cell cell : row) {
				String headerName = sheet.getRow(0).getCell(cellIterator).getStringCellValue();
				String headerMapped = getColumnHeaderMapping(headerName);
				headerName = StringUtils.isEmpty(headerMapped) ? headerName: headerMapped;
				Object value = getCellValue(cell);
				BeanUtils.setProperty(line, headerName, value);
				cellIterator++;
			}
			lines.add(line);
			rowIterator++;
		}
		return lines;
	}

	private String getColumnHeaderMapping(String headerName) {
		return 	PRODUCT_DATA_SPECIAL_MAPPING.get(headerName);
	}

	private static Object getCellValue(Cell cell ){
		switch (cell.getCellType())
		{
			case Cell.CELL_TYPE_NUMERIC:
				return cell.getNumericCellValue();
			case Cell.CELL_TYPE_STRING:
				return cell.getStringCellValue();
			case Cell.CELL_TYPE_BOOLEAN:
				return cell.getBooleanCellValue();
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




