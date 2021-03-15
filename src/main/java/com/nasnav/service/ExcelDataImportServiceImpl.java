package com.nasnav.service;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.utils.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.jboss.logging.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import com.nasnav.service.model.importproduct.csv.CsvRow;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.common.base.Optional;
@Service
public class ExcelDataImportServiceImpl extends AbstractCsvExcelDataImportService {

	private Logger logger = Logger.getLogger(getClass());

	@Override
	public ImportProductContext importProductList(@Valid MultipartFile file, @Valid ProductListImportDTO importMetaData) throws BusinessException, ImportProductException {
		validateProductImportMetaData(importMetaData);
		validateProductImportCsvFile(file);

		ProductImportMetadata importMetadata = getImportMetaData(importMetaData);
		ImportProductContext initialContext = new ImportProductContext(emptyList(), importMetadata);

		List<CsvRow> rows = parseExcelFile(file, importMetaData, initialContext);

		List<ProductImportDTO> productsData =
				rows
						.stream()
						.map(CsvRow::toProductImportDto)
						.collect(toList());
		return dataImportService.importProducts(productsData, importMetadata);

	}

	private List<CsvRow> parseExcelFile(MultipartFile file, ProductListImportDTO importMetaData, ImportProductContext initialContext) throws ImportProductException {
		List<CsvRow> lines = null;
		try {
			Workbook wb = WorkbookFactory.create(file.getInputStream());
			Sheet sheet = wb.getSheetAt(0);
			validateFileHeader(sheet.getRow(0));
			lines = readImpDataLines(sheet);

			wb.close();
		} catch (Exception e) {
			e.printStackTrace();
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
		if(headerNotFound != null && !headerNotFound.isEmpty()){
			throw new Exception(" Could not find fields : ["+headerNotFound+"]");
		}

	}

	public List<CsvRow> readImpDataLines(Sheet sheet) {
		List<CsvRow> lines = new ArrayList<>();
		int rowIterator =0;
		for (Row row: sheet) {
			CsvRow line = new CsvRow();
			if(rowIterator ==0){
				rowIterator++;
				continue; // skip header row
			}
			System.out.print(" row -->> "+ rowIterator+"\t");
			int cellIterator = 0;
			for (Cell cell : row) {
				String headerName = sheet.getRow(0).getCell(cellIterator).getStringCellValue();
				String headerMapped = getColumnHeaderMapping(headerName);
				headerName = StringUtils.isEmpty(headerMapped) ? headerName: headerMapped;
				System.out.print(" cell -->> "+ headerName +"\t");
				Object value = getCellValue(cell);
				System.out.print(" value -->> "+ value +"\t");
				ReflectUtils.set(line, headerName, value);
				cellIterator++;
				System.out.println();
			}
			lines.add(line);
			rowIterator++;
		}
		return lines;
	}

	private String getColumnHeaderMapping(String headerName) {
		return 	PRODUCT_DATA_SPECIAL_MAPPING.get(headerName);
	}

	@Override
	public ByteArrayOutputStream generateProductsCsvTemplate(){
		List<String> baseHeaders = getProductImportTemplateHeaders();

		return null;
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
	public static class ReflectUtils {

		public static boolean set(Object object, String fieldName, Object fieldValue) {
			if(fieldName==null)
				return false;
			Class<?> clazz = object.getClass();
			while (clazz != null) {
				try {
					Field field = clazz.getDeclaredField(fieldName);
					field.setAccessible(true);
					Type pt=null;
					try{
						pt = field.getGenericType();
					}catch(Exception e){
						e.printStackTrace();
					}
					if(fieldValue != null ) {
						if (pt != null && pt.getTypeName().equals("java.lang.String"))
							if (fieldValue instanceof Double)
								field.set(object, String.valueOf(((Double) fieldValue).intValue()));
							else
								field.set(object, String.valueOf(fieldValue));
						else if (pt != null && (pt.getTypeName().equals("java.lang.Long") || pt.getTypeName().equals("long")))
							field.set(object, new BigDecimal(String.valueOf(fieldValue)).longValue());
						else if (pt != null && (pt.getTypeName().equals("java.math.BigDecimal")))
							field.set(object, new BigDecimal(String.valueOf(fieldValue)));
						else if (pt != null && (pt.getTypeName().equals("java.lang.Integer") || pt.getTypeName().equals("int")))
							if (fieldValue instanceof Double)
								field.set(object, ((Double) fieldValue).intValue());
							else
								field.set(object, Integer.parseInt(String.valueOf(fieldValue)));
					}
					else
						field.set(object, fieldValue);
					return true;
				} catch (NoSuchFieldException e) {
					clazz = clazz.getSuperclass();
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
			return false;
		}
	}
}




