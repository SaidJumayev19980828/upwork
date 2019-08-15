package com.nasnav.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.FileService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/files")
public class FilesController {
	
	@Autowired
	private FileService fileService;
	
	
	@ApiOperation(value = "upload a file to the server", nickname = "fileUpload")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "INSUFFICIENT RIGHTS or UNAUTHENTICATED"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.OK)
    public String updateShop(
    		@RequestParam("org_id") @Nullable Long orgId,
    		@RequestParam("file") MultipartFile file
    		) throws BusinessException {  
		
        return fileService.saveFile(file, orgId);
    }
	
}
