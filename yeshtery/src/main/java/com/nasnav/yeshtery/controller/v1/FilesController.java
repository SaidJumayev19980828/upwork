package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.enumerations.ConvertedImageTypes;
import com.nasnav.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping(FilesController.API_PATH)
public class FilesController {
    static final String API_PATH = YeshteryConstants.API_PATH +"/files";

	@Autowired
	private FileService fileService;

    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.OK)
    public String updateShop(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
    		                 @RequestParam("org_id") @Nullable Long orgId,
    		                 @RequestParam("file") MultipartFile file) {
        return fileService.saveFile(file, orgId);
    }

    @GetMapping( path="**")
    public void downloadFile(HttpServletRequest request,
                             HttpServletResponse resp,
                             @RequestParam(required = false) Integer height,
                             @RequestParam(required = false) Integer width,
                             @RequestParam(required = false) ConvertedImageTypes type) throws ServletException, IOException {
        String url = request.getRequestURI().replaceFirst(API_PATH, "");
        String resourceInternalUrl = fileService.getResourceInternalUrl(url, width, height, type);
		resp.setStatus(HttpStatus.OK.value());

		RequestDispatcher dispatcher = request.getRequestDispatcher(resourceInternalUrl);
		dispatcher.forward(request, resp);
    }

    @DeleteMapping()
    public void deleteFile(
            @RequestHeader(name = "User-Token", required = false) String userToken,
            @RequestParam(value = "file_name", required = false) String fileName,
            @RequestParam(value = "url", required = false) String fileUrl,
            @RequestParam(value = "model_id", required = false) Long modelId) {
        fileService.delete(fileName, fileUrl, modelId);
    }
}
