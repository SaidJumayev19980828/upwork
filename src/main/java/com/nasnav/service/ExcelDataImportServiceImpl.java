package com.nasnav.service;

import static java.util.Collections.emptyList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

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

		return initialContext;

	}

	private List<CsvRow> parseExcelFile(MultipartFile file, ProductListImportDTO importMetaData, ImportProductContext initialContext) throws ImportProductException {
		List<ProductFeaturesEntity> orgFeatures = featureRepo.findByShopId( importMetaData.getShopId() );
		try {
			Workbook wb = WorkbookFactory.create(file.getInputStream());
			Sheet sheet = wb.getSheetAt(0);
			validateFileHeader(sheet.getRow(0));

		} catch (Exception e) {
			e.printStackTrace();
			throw  new ImportProductException(e, initialContext);
		}

		return null;
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
			for (Cell cell : row) {
				
			}
			lines.add(line);
			rowIterator++;
		}
		return lines;
	}
	@Override
	public ByteArrayOutputStream generateProductsCsvTemplate(){
		List<String> baseHeaders = getProductImportTemplateHeaders();

		return null;
	}

	
}




