package com.nasnav.controller;

import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.CsvDataExportService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;


@RestController
@RequestMapping("/export")
@Tag(name = "Data export api")
public class DataExportContoller {
	
	
	@Autowired
	private CsvDataExportService exportService;

	


    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Products data exported"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
	@GetMapping(value = "/products")
	@ResponseBody
	public ResponseEntity<String> generateProductsCsv(
			@RequestHeader(name = "User-Token", required = false) String token
			, @RequestParam(name = "shop_id", required = false)Long shopId) throws SQLException, BusinessException, IllegalAccessException, InvocationTargetException {
		ByteArrayOutputStream s = exportService.generateProductsCsv(shopId);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("text/csv"))
				.header(CONTENT_DISPOSITION, "attachment; filename=Products_Csv.csv")
				.body(s.toString());
	}



	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "Products images data exported"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
			@ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid data"),
	})
	@GetMapping(value = "/products/images")
	@ResponseBody
	public ResponseEntity<String> generateProductsImagesCsv(
			@RequestHeader(name = "User-Token", required = false) String token) {
		ByteArrayOutputStream s = exportService.generateProductsImagesCsv();
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("text/csv"))
				.header(CONTENT_DISPOSITION, "attachment; filename=Products_With_Images_Csv.csv")
				.body(s.toString());
	}
}
