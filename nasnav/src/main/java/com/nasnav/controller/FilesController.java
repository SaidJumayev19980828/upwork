package com.nasnav.controller;

import com.nasnav.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FilesController {

	@Autowired
	private FileService fileService;

	private static Logger logger = Logger.getLogger(FilesController.class);

	@Operation(description =  "upload a file to the server", summary = "fileUpload")
    @ApiResponses(value = { @ApiResponse(responseCode = " 200" ,description = "OK"),
            @ApiResponse(responseCode = " 401" ,description = "INSUFFICIENT RIGHTS or UNAUTHENTICATED"),
            @ApiResponse(responseCode = " 406" ,description = "INVALID_PARAM")})
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.OK)
    public String updateShop(
    		@RequestHeader (name = "User-Token", required = false) String userToken,
    		@RequestParam("org_id") @Nullable Long orgId,
    		@RequestParam("file") MultipartFile file) {

        return fileService.saveFile(file, orgId);
    }



	@Operation(description =  "download a file to the server", summary = "fileDownload")
    @ApiResponses(value = { @ApiResponse(responseCode = " 200" ,description = "OK"),
            @ApiResponse(responseCode = " 401" ,description = "INSUFFICIENT RIGHTS or UNAUTHENTICATED"),
            @ApiResponse(responseCode = " 406" ,description = "INVALID_PARAM")})
    @GetMapping( path="**")
    public void downloadFile(HttpServletRequest request, HttpServletResponse resp,
                             @RequestParam(required = false) Integer height,
                             @RequestParam(required = false) Integer width,
                             @RequestParam(required = false) String type) throws ServletException, IOException {
        String url = request.getRequestURI().replaceFirst("/files", "");;
        String resourceInternalUrl;

		logger.debug("Requesting image " + url + ", size: {" + width + "} x {" + height + "}");
		if (height != null || width != null) {
            resourceInternalUrl = fileService.getResizedImageInternalUrl(url, width, height, type);
        } else {
            resourceInternalUrl = fileService.getResourceInternalUrl(url);
        }
		logger.debug("Resultant URL: " + resourceInternalUrl);
		resp.setStatus(HttpStatus.OK.value());

		RequestDispatcher dispatcher = request.getRequestDispatcher(resourceInternalUrl);
		dispatcher.forward(request, resp);
    }

}
