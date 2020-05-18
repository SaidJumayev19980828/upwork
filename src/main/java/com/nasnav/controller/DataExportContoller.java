package com.nasnav.controller;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.CsvDataExportService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponses;


@RestController
@RequestMapping("/export")
@Api(description = "Data export api")
public class DataExportContoller {
	
	
	@Autowired
	private CsvDataExportService exportService;

	


    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Products data exported"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
	@GetMapping(value = "/products")
	@ResponseBody
	public ResponseEntity<String> generateProductsCsv(
			@RequestHeader(name = "User-Token", required = false) String token
			, @RequestParam(name = "shop_id", required = true)Long shopId) throws SQLException, BusinessException, IllegalAccessException, InvocationTargetException {
		ByteArrayOutputStream s = exportService.generateProductsCsv(shopId);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("text/csv"))
				.header(CONTENT_DISPOSITION, "attachment; filename=Products_Csv.csv")
				.body(s.toString());
	}
}
