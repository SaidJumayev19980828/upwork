package com.nasnav.controller;

import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.CsvExcelDataImportService;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import com.nasnav.service.CsvDataImportServiceImpl;
import com.nasnav.service.ExcelDataImportServiceImpl;
import io.swagger.annotations.ApiResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;


@RestController
@RequestMapping("/upload")
@Tag(name = "Data Import api")
public class DataImportContoller {

	@Autowired
	private CsvDataImportServiceImpl csvImportService;

	@Autowired
	private ExcelDataImportServiceImpl excelImportService;

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
		ImportProductContext importResult = csvImportService.importProductList(file, importMetaData);
		if(importResult.isSuccess()) {
			return ResponseEntity.ok(importResult);
		}else {
			return new ResponseEntity<>(importResult, NOT_ACCEPTABLE);
		}			
    }

    @GetMapping(value = "/productlist/template")
	@ResponseBody
	public ResponseEntity<String> generateCsvTemplate(@RequestHeader(name = "User-Token", required = false) String token) throws IOException {
		ByteArrayOutputStream s = csvImportService.generateProductsCsvTemplate();
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("text/csv"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Csv_Template.csv")
				.body(s.toString());
	}

}
