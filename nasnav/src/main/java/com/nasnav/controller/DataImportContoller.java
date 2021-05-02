package com.nasnav.controller;

import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.CsvExcelDataImportService;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.validation.Valid;

import com.nasnav.commons.utils.FilesUtils;
import com.nasnav.service.CsvDataImportServiceImpl;
import com.nasnav.service.ExcelDataImportServiceImpl;
import io.swagger.annotations.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.model.importproduct.context.ImportProductContext;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;


@RestController
@RequestMapping("/upload")
@Tag(name = "Data Import api")
public class DataImportContoller {

	@Autowired
	private CsvDataImportServiceImpl csvImportService;

	@Autowired
	private ExcelDataImportServiceImpl excelDataImportService;

	@ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Products data verified/imported"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
	@ResponseStatus(HttpStatus.OK)
	@PostMapping(value = "productlist",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Products data verified/imported xlsx"),
			@ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
			@ApiResponse(code = 403, message = "Insuffucient Rights"),
			@ApiResponse(code = 406, message = "Invalid data"),
	})
	@ResponseStatus(HttpStatus.OK)
	@PostMapping(value = "productlist/xlsx",
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Products data verified/imported csv"),
			@ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
			@ApiResponse(code = 403, message = "Insuffucient Rights"),
			@ApiResponse(code = 406, message = "Invalid data"),
	})
	@ResponseStatus(HttpStatus.OK)
	@PostMapping(value = "productlist/csv",
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ImportProductContext> importProductListCSV(
			@RequestHeader(name = "User-Token", required = false) String token,
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

	@GetMapping(value = "/productlist/csv/template")
	@ResponseBody
	public ResponseEntity<String> generateCsvTemplate(@RequestHeader(name = "User-Token", required = false) String token) throws IOException {
		ByteArrayOutputStream s = csvImportService.generateProductsTemplate();
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("text/csv"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Csv_Template.csv")
				.body(s.toString());
	}


	@GetMapping(value = "/productlist/xls/template")
	@ResponseBody
	public ResponseEntity<byte[]> generateXlsTemplate(@RequestHeader(name = "User-Token", required = false) String token) throws IOException {
		ByteArrayOutputStream s = excelDataImportService.generateProductsTemplate();
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Product_Template.xlsx")
				.body(s.toByteArray());
	}
}
