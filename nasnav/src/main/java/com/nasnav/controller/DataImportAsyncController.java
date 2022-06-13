package com.nasnav.controller;

import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import com.nasnav.commons.utils.FilesUtils;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.CsvExcelDataImportService;
import com.nasnav.service.ExcelDataImportAsyncServiceImpl;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/upload/async")
@RequiredArgsConstructor
public class DataImportAsyncController {

	private final ExcelDataImportAsyncServiceImpl excelDataImportAsyncService;

	@PostMapping(value = "productlist/xlsx", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<HandlerChainProcessStatus> importProductListXLSX(
			@RequestHeader(name = "User-Token") String token,
			@RequestPart("xlsx") @Valid MultipartFile file,
			@RequestPart("properties") @Valid ProductListImportDTO importMetaData)
			throws Exception {

		HandlerChainProcessStatus importResult = null;
		if (FilesUtils.isExcel(file)) {
			importResult = excelDataImportAsyncService.importProductList(file, importMetaData);
		}

		if (importResult != null && ( importResult.isInProgress() || importResult.isSuccess())) {
			return ResponseEntity.ok(importResult);
		} else {
			return new ResponseEntity<>(importResult, NOT_ACCEPTABLE);
		}
	}




}
