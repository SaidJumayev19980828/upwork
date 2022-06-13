package com.nasnav.controller;

import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import com.nasnav.commons.utils.FilesUtils;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.service.DataImportAsyncServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/upload/async")
@RequiredArgsConstructor
public class DataImportAsyncController {

	private final DataImportAsyncServiceImpl excelDataImportAsyncService;

	@PostMapping(value = "productlist/xlsx", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<HandlerChainProcessStatus> importProductListXLSX(
			@RequestHeader(name = "User-Token") String token,
			@RequestPart("xlsx") @Valid MultipartFile file,
			@RequestPart("properties") @Valid ProductListImportDTO importMetaData)
			throws Exception {

		HandlerChainProcessStatus importResult = null;
		if (FilesUtils.isExcel(file)) {
			importResult = excelDataImportAsyncService.importExcelProductList(file, importMetaData);
		}

		if (importResult != null && ( importResult.isInProgress() || importResult.isSuccess())) {
			return ResponseEntity.ok(importResult);
		} else {
			return new ResponseEntity<>(importResult, NOT_ACCEPTABLE);
		}
	}

	@PostMapping(value = "productlist/csv", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<HandlerChainProcessStatus> importProductListCSV(@RequestHeader(TOKEN_HEADER) String token,
																	 @RequestPart("csv") @Valid MultipartFile file,
																	 @RequestPart("properties") @Valid ProductListImportDTO importMetaData)
			throws Exception {
		HandlerChainProcessStatus importResult = null;
		if(FilesUtils.isCsv(file)){
			importResult =excelDataImportAsyncService.importCsvProductList(file, importMetaData);
		}
		if(importResult != null && ( importResult.isInProgress() || importResult.isSuccess())) {
			return ResponseEntity.ok(importResult);
		}else {
			return new ResponseEntity<>(importResult, NOT_ACCEPTABLE);
		}
	}


}
