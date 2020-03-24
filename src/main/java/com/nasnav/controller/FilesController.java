package com.nasnav.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
    		@RequestHeader (value = "User-Token") String userToken,
    		@RequestParam("org_id") @Nullable Long orgId,
    		@RequestParam("file") MultipartFile file
    		) throws BusinessException {  
		
        return fileService.saveFile(file, orgId);
    }
	
	
	
	
	
	
	
	@ApiOperation(value = "download a file to the server", nickname = "fileDownload")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "INSUFFICIENT RIGHTS or UNAUTHENTICATED"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @GetMapping( path="**")
	@ResponseStatus(HttpStatus.OK)
    public void downloadFile(HttpServletRequest request, HttpServletResponse resp) throws BusinessException, ServletException, IOException {
        String url = request.getRequestURI().replaceFirst("/files", "");
		String resourceInternalUrl = fileService.getResourceInternalUrl(url);        
		
		RequestDispatcher dispatcher = request.getRequestDispatcher(resourceInternalUrl);
		dispatcher.forward(request, resp);		
    }
	
}
