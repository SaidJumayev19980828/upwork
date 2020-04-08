package com.nasnav.controller;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.CsvDataImportService;
import com.nasnav.service.model.importproduct.context.ImportProductContext;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponses;


@RestController
@RequestMapping("/upload")
@Api(description = "Data Import api")
public class DataImportContoller {
	
	
	@Autowired
	private CsvDataImportService importService;


	@ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Products data verified/imported"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
	@ResponseStatus(HttpStatus.OK)
	@PostMapping(value = "productlist",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportProductContext> importProductList(
    		@RequestHeader("User-Token") String token,
            @RequestPart("csv") @Valid MultipartFile file,
            @RequestPart("properties") @Valid ProductListImportDTO importMetaData)
            		throws BusinessException, ImportProductException {
		ImportProductContext importResult = importService.importProductListFromCSV(file, importMetaData);
		if(importResult.isSuccess()) {
			return ResponseEntity.ok(importResult);
		}else {
			return new ResponseEntity<>(importResult, NOT_ACCEPTABLE);
		}			
    }
	
	
	

    @GetMapping(value = "/productlist/template")
	@ResponseBody
	public ResponseEntity<String> generateCsvTemplate(@RequestHeader("User-Token") String token) throws IOException {
		ByteArrayOutputStream s = importService.generateProductsCsvTemplate();
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("text/csv"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Csv_Template.csv")
				.body(s.toString());
	}

}
