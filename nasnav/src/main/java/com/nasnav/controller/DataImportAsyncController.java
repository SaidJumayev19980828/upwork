package com.nasnav.controller;

import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import com.nasnav.commons.utils.FilesUtils;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.response.ImportProcessStatusResponse;
import com.nasnav.service.OrganizationProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/upload/async")
@RequiredArgsConstructor
public class DataImportAsyncController {

	private final OrganizationProcessService organizationProcessService;

	@PostMapping(value = "productlist/xlsx", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ImportProcessStatusResponse> importProductListXLSX(
			@RequestHeader(name = "User-Token") String token,
			@RequestPart("xlsx") @Valid MultipartFile file,
			@RequestPart("properties") @Valid ProductListImportDTO importMetaData)
			throws Exception {

		ImportProcessStatusResponse importResult = null;
		if (FilesUtils.isExcel(file)) {
			importResult = organizationProcessService.importExcelProductList(file, importMetaData);
		}

		if (importResult != null && ( importResult.getProcessStatus().isInProgress() || importResult.getProcessStatus().isSuccess())) {
			return ResponseEntity.ok(importResult);
		} else {
			return new ResponseEntity<>(importResult, NOT_ACCEPTABLE);
		}
	}

	@PostMapping(value = "productlist/csv", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ImportProcessStatusResponse> importProductListCSV(@RequestHeader(TOKEN_HEADER) String token,
																	 @RequestPart("csv") @Valid MultipartFile file,
																	 @RequestPart("properties") @Valid ProductListImportDTO importMetaData)
			throws Exception {
		ImportProcessStatusResponse importResult = null;
		if(FilesUtils.isCsv(file)){
			importResult =organizationProcessService.importCsvProductList(file, importMetaData);
		}
		if(importResult != null && ( importResult.getProcessStatus().isInProgress() || importResult.getProcessStatus().isSuccess())) {
			return ResponseEntity.ok(importResult);
		}else {
			return new ResponseEntity<>(importResult, NOT_ACCEPTABLE);
		}
	}

	@GetMapping("process")
	public List<ImportProcessStatusResponse> getAllProcess(@RequestHeader(name = "User-Token") String token) {

		return organizationProcessService.getProcessesStatus();
	}

	@GetMapping("process/{id}/status")
	public ImportProcessStatusResponse getProcessStatus(@RequestHeader(name = "User-Token") String token,
													  @PathVariable String id) {

		return organizationProcessService.getProcessStatus(id);
	}

	@GetMapping("process/{id}/result")
	public Object getProcessResult(@RequestHeader(name = "User-Token") String token,
								   @PathVariable String id) {

		return organizationProcessService.getProcessResult(id);
	}

	@PutMapping("process/cancel/{id}")
	public ImportProcessStatusResponse cancelProcess(@RequestHeader(name = "User-Token") String token,
												   @PathVariable String id) {

		return organizationProcessService.cancelProcess(id);
	}

	@DeleteMapping("process")
	public void clearAllProcess(@RequestHeader(name = "User-Token") String token){
		organizationProcessService.clearAllProcess();
	}

	@DeleteMapping("process/{id}")
	public void clearProcess(@RequestHeader(name = "User-Token") String token,
							 @PathVariable String id){
		organizationProcessService.clearProcess(id);
	}

}
