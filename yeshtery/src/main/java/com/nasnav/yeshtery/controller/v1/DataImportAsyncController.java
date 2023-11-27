package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.response.ImportProcessStatusResponse;
import com.nasnav.service.OrganizationProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping(DataImportAsyncController.API_PATH)
@RequiredArgsConstructor
public class DataImportAsyncController {
	static final String API_PATH = YeshteryConstants.API_PATH +"/upload/async";


	private final OrganizationProcessService organizationProcessService;

	@PostMapping(value = "productlist/xlsx", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
	public ImportProcessStatusResponse  importProductListXLSX(
			@RequestHeader(name = "User-Token") String token,
			@RequestPart("xlsx") @Valid MultipartFile file,
			@RequestPart("properties") @Valid ProductListImportDTO importMetaData)
			throws Exception {
		return organizationProcessService.importExcelProductList(file, importMetaData);
	}

	@PostMapping(value = "productlist/csv", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
	public ImportProcessStatusResponse importProductListCSV(@RequestHeader(TOKEN_HEADER) String token,
																	 @RequestPart("csv") @Valid MultipartFile file,
																	 @RequestPart("properties") @Valid ProductListImportDTO importMetaData)
			throws Exception {
		return organizationProcessService.importCsvProductList(file, importMetaData);
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
