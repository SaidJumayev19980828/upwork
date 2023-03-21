package com.nasnav.controller;

import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.AutoDataImportService;
import com.nasnav.service.CsvExcelDataImportService;
import com.nasnav.service.model.importproduct.context.ImportProductContext;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class DataImportController {

	@Autowired
	@Qualifier("csv")
	private CsvExcelDataImportService csvImportService;

	@Autowired
	@Qualifier("excel")
	private CsvExcelDataImportService excelDataImportService;

	@Autowired
	private AutoDataImportService autoDataImportService;

	@PostMapping(value = "productlist", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
	public ImportProductContext importProductList(
			@RequestHeader(name = "User-Token", required = false) String token,
			@RequestPart("csv") @Valid MultipartFile file,
			@RequestPart("properties") @Valid ProductListImportDTO importMetaData)
			throws BusinessException, ImportProductException {
		return autoDataImportService.importProductList(file, importMetaData);
	}

	@PostMapping(value = "productlist/xlsx", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
	public ImportProductContext importProductListXLSX(
			@RequestHeader(name = "User-Token", required = false) String token,
			@RequestPart("xlsx") @Valid MultipartFile file,
			@RequestPart("properties") @Valid ProductListImportDTO importMetaData)
			throws BusinessException, ImportProductException {
		return excelDataImportService.importProductList(file, importMetaData);
	}

	@PostMapping(value = "productlist/csv", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
	public ImportProductContext importProductListCSV(@RequestHeader(TOKEN_HEADER) String token,
																	 @RequestPart("csv") @Valid MultipartFile file,
																	 @RequestPart("properties") @Valid ProductListImportDTO importMetaData)
			throws BusinessException, ImportProductException {
		return csvImportService.importProductList(file, importMetaData);
	}

	@GetMapping(value = {"/productlist/csv/template", "/productlist/template"})
	@ResponseBody
	public ResponseEntity<String> generateCsvTemplate(@RequestHeader(TOKEN_HEADER) String token) throws IOException {
		ByteArrayOutputStream s = csvImportService.generateProductsTemplate(false);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("text/csv"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Csv_Template.csv")
				.body(s.toString());
	}

	@GetMapping(value = "/productlist/xls/template")
	@ResponseBody
	public ResponseEntity<byte[]> generateXlsTemplate(@RequestHeader(TOKEN_HEADER) String token,
													  @RequestParam(name = "validate", required = false) Boolean validate) throws IOException {
		ByteArrayOutputStream s = excelDataImportService.generateProductsTemplate(validate);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Product_Template.xlsx")
				.body(s.toByteArray());
	}
}
