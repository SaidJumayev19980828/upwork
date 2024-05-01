package com.nasnav.controller;

import com.nasnav.enumerations.FileType;
import com.nasnav.request.OrderSearchParam;
import com.nasnav.service.CsvExcelDataExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@RestController
@RequestMapping("/export")
public class DataExportController {

	@Autowired
	@Qualifier("csv")
	private CsvExcelDataExportService csvExportService;
	@Autowired
	@Qualifier("excel")
	private CsvExcelDataExportService excelExportService;

	@GetMapping(value = { "/products", "/products/csv" })
	@ResponseBody
	public ResponseEntity<byte[]> generateProductsCsv(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
													  @RequestParam(name = "shop_id", required = false)Long shopId) throws Exception {
			ByteArrayOutputStream s = csvExportService.generateProductsFile(shopId, false);
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType("text/csv"))
					.header(CONTENT_DISPOSITION, "attachment; filename=Products_Csv.csv")
					.body(s.toByteArray());
	}

 	@GetMapping(value = "/products/xlsx")
	@ResponseBody
	public ResponseEntity<byte[]> generateProductsXLSX(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
													   @RequestParam(name = "shop_id", required = false) Long shopId,
													   @RequestParam(name = "validate", required = false) Boolean validate) throws Exception {
			ByteArrayOutputStream s = excelExportService.generateProductsFile(shopId, validate);
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.header(CONTENT_DISPOSITION, "attachment; filename=Products_Xlsx.xlsx")
					.body(s.toByteArray());
	}

	@GetMapping(value = "/products/images", params = { "type=CSV" })
	@ResponseBody
	public ResponseEntity<byte[]> generateProductsImagesCsvByType(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
			@RequestParam(name = "type") FileType type) throws IOException {
		ByteArrayOutputStream s = csvExportService.generateProductsImagesFile();
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("text/csv"))
				.header(CONTENT_DISPOSITION, "attachment; filename=Products_With_Images_Csv.csv")
				.body(s.toByteArray());
	}

	@GetMapping(value = "/products/images", params = { "type=XLSX" })
	@ResponseBody
	public ResponseEntity<byte[]> generateProductsImagesExcelByType(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
			@RequestParam(name = "type") FileType type) throws IOException {
		ByteArrayOutputStream s = excelExportService.generateProductsImagesFile();
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.header(CONTENT_DISPOSITION, "attachment; filename=Products_Xlsx.xlsx")
				.body(s.toByteArray());
	}

	@GetMapping(value = "/products/images/csv")
	@ResponseBody
	public ResponseEntity<byte[]> generateProductsImagesCSV(@RequestHeader(value = TOKEN_HEADER, required = false) String token) throws IOException {
		ByteArrayOutputStream s = csvExportService.generateProductsImagesFile();
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("text/csv"))
				.header(CONTENT_DISPOSITION, "attachment; filename=Products_With_Images_Csv.csv")
				.body(s.toByteArray());
	}

	@GetMapping(value = "/products/images/xlsx")
	@ResponseBody
	public ResponseEntity<byte[]> generateProductsImagesXLSX(@RequestHeader(value = TOKEN_HEADER, required = false) String token) throws IOException {
		ByteArrayOutputStream s = excelExportService.generateProductsImagesFile();
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.header(CONTENT_DISPOSITION, "attachment; filename=Products_Xlsx.xlsx")
				.body(s.toByteArray());
	}


	@GetMapping(value = { "/orders", "/orders/csv" })
	@ResponseBody
	public ResponseEntity<byte[]> generateOrdersCsv(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
													OrderSearchParam params) throws Exception {
		ByteArrayOutputStream s = csvExportService.generateOrdersFile(params);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("text/csv; charset=utf-8"))
				.header(CONTENT_DISPOSITION, "attachment; filename=orders_Csv.csv")
				.body(s.toByteArray());
	}

}
