package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.utils.FilesUtils;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.CsvExcelDataImportService;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import com.nasnav.yeshtery.YeshteryConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping(DataImportController.API_PATH)
public class DataImportController {
	static final String API_PATH = YeshteryConstants.API_PATH +"/upload";

	@Autowired
	@Qualifier("csv")
	private CsvExcelDataImportService csvImportService;

	@Autowired
	@Qualifier("excel")
	private CsvExcelDataImportService excelDataImportService;

	@PostMapping(value = "productlist", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportProductContext> importProductList(
    		@RequestHeader(name = "User-Token", required = false) String token,
            @RequestPart("csv") @Valid MultipartFile file,
            @RequestPart("properties") @Valid ProductListImportDTO importMetaData)
            		throws BusinessException, ImportProductException {
		ImportProductContext importResult = null;
		if(FilesUtils.isExcel(file)){
			importResult = excelDataImportService.importProductList(file, importMetaData);
		} else if(FilesUtils.isCsv(file)){
			importResult = csvImportService.importProductList(file, importMetaData);
		}
		if(importResult != null && importResult.isSuccess()) {
			return ResponseEntity.ok(importResult);
		}else {
			return new ResponseEntity<>(importResult, NOT_ACCEPTABLE);
		}			
    }

	@PostMapping(value = "productlist/xlsx", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ImportProductContext> importProductListXLSX(
			@RequestHeader(name = "User-Token", required = false) String token,
			@RequestPart("xlsx") @Valid MultipartFile file,
			@RequestPart("properties") @Valid ProductListImportDTO importMetaData)
			throws BusinessException, ImportProductException {
		ImportProductContext importResult = null;
		if(FilesUtils.isExcel(file)){
			importResult = excelDataImportService.importProductList(file, importMetaData);
		}
		
		if(importResult != null && importResult.isSuccess()) {
			return ResponseEntity.ok(importResult);
		}else {
			return new ResponseEntity<>(importResult, NOT_ACCEPTABLE);
		}
	}

	@PostMapping(value = "productlist/csv", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ImportProductContext> importProductListCSV(@RequestHeader(TOKEN_HEADER) String token,
																	 @RequestPart("csv") @Valid MultipartFile file,
																	 @RequestPart("properties") @Valid ProductListImportDTO importMetaData)
			throws BusinessException, ImportProductException {
		ImportProductContext importResult = null;
		if(FilesUtils.isCsv(file)){
			importResult = csvImportService.importProductList(file, importMetaData);
		}
		if(importResult != null && importResult.isSuccess()) {
			return ResponseEntity.ok(importResult);
		}else {
			return new ResponseEntity<>(importResult, NOT_ACCEPTABLE);
		}
	}

	@GetMapping(value = {"/productlist/csv/template", "/productlist/template"})
	@ResponseBody
	public ResponseEntity<String> generateCsvTemplate(@RequestHeader(TOKEN_HEADER) String token) throws IOException {
		ByteArrayOutputStream s = csvImportService.generateProductsTemplate();
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("text/csv"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Csv_Template.csv")
				.body(s.toString());
	}

	@GetMapping(value = "/productlist/xls/template")
	@ResponseBody
	public ResponseEntity<byte[]> generateXlsTemplate(@RequestHeader(TOKEN_HEADER) String token) throws IOException {
		ByteArrayOutputStream s = excelDataImportService.generateProductsTemplate();
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Product_Template.xlsx")
				.body(s.toByteArray());
	}
}
