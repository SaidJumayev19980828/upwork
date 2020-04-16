package com.nasnav.controller;

import com.nasnav.dto.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.CategoryService;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.service.OrganizationService;
import com.nasnav.service.ThemeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Api(description = "Set of endpoints for adding, updating and deleting Dashboard data.")
@CrossOrigin("*") // allow all origins
public class AdminController {

    @Autowired
    private CategoryService categoryService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private ThemeService themeService;

    @ApiOperation(value = "Create an Organization", nickname = "OrganizationCreation", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "organization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity createOrganization(@RequestHeader (value = "User-Token") String userToken,
                                             @RequestBody OrganizationDTO.OrganizationCreationDTO json)  throws BusinessException {
	    OrganizationResponse response = organizationService.createOrganization(json);
	    return new ResponseEntity(response, response.getHttpStatus());
    }

	@ApiOperation(value = "Create or update a Category", nickname = "categoryModification", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
	})
	@PostMapping(value = "category", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity createCategory(@RequestHeader (value = "User-Token") String userToken,
	                                     @RequestBody CategoryDTO.CategoryModificationObject categoryJson) throws BusinessException {
    	if (categoryJson.getOperation() != null)
			if (categoryJson.getOperation().equals("update"))
				return categoryService.updateCategory(categoryJson);
			else if (categoryJson.getOperation().equals("create"))
				return categoryService.createCategory(categoryJson);
		throw new BusinessException("INVAILD_PARAM: operation","No correct operation provided", HttpStatus.NOT_ACCEPTABLE);
	}


	@ApiOperation(value = "delete a Category", nickname = "categoryDeletion", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
			@io.swagger.annotations.ApiResponse(code = 409, message = "Category is used by other entities"),
	})
	@DeleteMapping(value = "category", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity deleteCategory(@RequestHeader (value = "User-Token") String userToken,
	                                     @RequestParam (value = "category_id") Long categoryId ) throws BusinessException {
		if (categoryId == null ){
			throw new BusinessException("MISSING_PRARM: Category_id", "",HttpStatus.NOT_ACCEPTABLE);
		}
		return categoryService.deleteCategory(categoryId);
	}

	@ApiOperation(value = "list all organizations", nickname = "orgList", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 404, message = "no organizations found"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "user not allowed to list organizations"),
	})
	@GetMapping(value = "list_organizations", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public List<OrganizationRepresentationObject> listOrganizations(@RequestHeader (value = "User-Token") String userToken) throws BusinessException {
		return organizationService.listOrganizations();
	}



	@ApiOperation(value = "list all theme classes", nickname = "themesList", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "no theme classes found"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "user not allowed to list theme classes"),
    })
    @GetMapping(value = "themes/class", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ThemeClassDTO> listThemeClasses(@RequestHeader (value = "User-Token") String userToken) {
        return themeService.listThemeClasses();
    }
    

    @ApiOperation(value = "list all themes", nickname = "themeClassesList", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "no theme found"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "user not allowed to list theme"),
    })
    @GetMapping(value = "themes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ThemeDTO> listThemes(@RequestHeader (value = "User-Token") String userToken) {
        return themeService.listThemes();
    }
}
