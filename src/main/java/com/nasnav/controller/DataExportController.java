package com.nasnav.controller;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import com.nasnav.enumerations.FileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.CsvExcelDataExportService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponses;


@RestController
@RequestMapping("/export")
@Api(description = "Data export api")
public class DataExportController {

	@Autowired
	@Qualifier("csv")
	private CsvExcelDataExportService csvExportService;

	@Autowired
	@Qualifier("excel")
	private CsvExcelDataExportService excelExportService;


	@ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Products data exported"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insufficient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
	@GetMapping(value = "/products")
	@ResponseBody
	public ResponseEntity<byte[]> generateProductsCsv(
			@RequestHeader(name = "User-Token", required = false) String token
			, @RequestParam(name = "shop_id", required = false)Long shopId
			, @RequestParam(name = "type") FileType type) throws Exception {
		if(FileType.CSV == type){
			ByteArrayOutputStream s = csvExportService.generateProductsFile(shopId);
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType("text/csv"))
					.header(CONTENT_DISPOSITION, "attachment; filename=Products_Csv.csv")
					.body(s.toByteArray());
		} else if(FileType.XLSX == type) {

			ByteArrayOutputStream s = excelExportService.generateProductsFile(shopId);
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.header(CONTENT_DISPOSITION, "attachment; filename=Products_Xlsx.xlsx")
					.body(s.toByteArray());
		}
		throw  new UnsupportedOperationException();

	}

	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "Products data exported XLXS"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "Insufficient Rights"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
	})
	@GetMapping(value = "/products/xlsx")
	@ResponseBody
	public ResponseEntity<byte[]> generateProductsXLSX(
			@RequestHeader(name = "User-Token", required = false) String token
			, @RequestParam(name = "shop_id", required = false)Long shopId) throws Exception {
			ByteArrayOutputStream s = excelExportService.generateProductsFile(shopId);
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.header(CONTENT_DISPOSITION, "attachment; filename=Products_Xlsx.xlsx")
					.body(s.toByteArray());
	}

	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "Products data exported CSV"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "Insufficient Rights"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
	})
	@GetMapping(value = "/products/csv")
	@ResponseBody
	public ResponseEntity<byte[]> generateProductsCSV(
			@RequestHeader(name = "User-Token", required = false) String token
			, @RequestParam(name = "shop_id", required = false)Long shopId) throws Exception {
		ByteArrayOutputStream s = csvExportService.generateProductsFile(shopId);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("text/csv"))
				.header(CONTENT_DISPOSITION, "attachment; filename=Products_Csv.csv")
				.body(s.toByteArray());
	}

	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "Products images data exported"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
	})
	@GetMapping(value = "/products/images")
	@ResponseBody
	public ResponseEntity<byte[]> generateProductsImagesCsv(
			@RequestHeader(name = "User-Token", required = false) String token
			, @RequestParam(name = "type") FileType type) throws IOException {
		if(FileType.CSV == type) {
			ByteArrayOutputStream s = csvExportService.generateProductsImagesFile();
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType("text/csv"))
					.header(CONTENT_DISPOSITION, "attachment; filename=Products_With_Images_Csv.csv")
					.body(s.toByteArray());
		}else if(FileType.XLSX == type) {

			ByteArrayOutputStream s = excelExportService.generateProductsImagesFile();
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType("text/csv"))
					.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.header(CONTENT_DISPOSITION, "attachment; filename=Products_Xlsx.xlsx")
					.body(s.toByteArray());
		}
		throw  new UnsupportedOperationException();

	}

	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "Products images data exported csv"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
	})
	@GetMapping(value = "/products/images/csv")
	@ResponseBody
	public ResponseEntity<byte[]> generateProductsImagesCSV(
			@RequestHeader(name = "User-Token", required = false) String token) throws IOException {
		ByteArrayOutputStream s = csvExportService.generateProductsImagesFile();
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType("text/csv"))
					.header(CONTENT_DISPOSITION, "attachment; filename=Products_With_Images_Csv.csv")
					.body(s.toByteArray());
	}

	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "Products images data exported csv"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
	})
	@GetMapping(value = "/products/images/xlsx")
	@ResponseBody
	public ResponseEntity<byte[]> generateProductsImagesXLSX(
			@RequestHeader(name = "User-Token", required = false) String token) throws IOException {
		ByteArrayOutputStream s = excelExportService.generateProductsImagesFile();
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("text/csv"))
				.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.header(CONTENT_DISPOSITION, "attachment; filename=Products_Xlsx.xlsx")
				.body(s.toByteArray());
	}
}
