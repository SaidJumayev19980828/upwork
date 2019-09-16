package com.nasnav.controller;

import java.io.IOException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ProductListImportResponse;
import com.nasnav.service.DataImportService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponses;



@RestController
@RequestMapping("/upload")
@Api(description = "Data Import api")
public class DataImportContoller {
	
	
	@Autowired
	private DataImportService importService;


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
    public ProductListImportResponse importProductList(
            @RequestPart("csv") @Valid MultipartFile file,
            @RequestPart("properties") @Valid ProductListImportDTO importMetaData)
            		throws BusinessException {

		return  importService.importProductListFromCSV(file, importMetaData);
    }
	
}
