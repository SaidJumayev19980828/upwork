package com.nasnav.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.websocket.server.PathParam;

import com.univocity.parsers.csv.Csv;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.dto.UserDTOs.UserLoginObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ProductListImportResponse;
import com.nasnav.service.DataImportService;
import com.nasnav.service.SecurityService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponses;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


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
