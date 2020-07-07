package com.nasnav.controller;

import java.util.List;

import com.nasnav.dto.*;
import com.nasnav.response.CategoryResponse;
import com.nasnav.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.ThemeClassResponse;
import com.nasnav.response.ThemeResponse;
import com.nasnav.service.CategoryService;
import com.nasnav.service.OrganizationService;
import com.nasnav.service.ThemeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;

import static org.springframework.http.HttpStatus.OK;

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

	@Autowired
	private AddressService addressService;

    @ApiOperation(value = "Create/update an Organization", nickname = "OrganizationCreation", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "organization",
				produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
				consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public OrganizationResponse createOrganization(@RequestHeader (name = "User-Token", required = false) String userToken,
												   @RequestBody OrganizationDTO.OrganizationCreationDTO json)  throws BusinessException {
	    return organizationService.createOrganization(json);
    }


	@ApiOperation(value = "Create or update a Category", nickname = "categoryModification", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
	})
	@PostMapping(value = "category", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public CategoryResponse createCategory(@RequestHeader (name = "User-Token", required = false) String userToken,
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
	public CategoryResponse deleteCategory(@RequestHeader (name = "User-Token", required = false) String userToken,
	                                       @RequestParam (value = "category_id") Long categoryId ) throws BusinessException {
		return categoryService.deleteCategory(categoryId);
	}


	@ApiOperation(value = "list all organizations", nickname = "orgList", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 404, message = "no organizations found"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "user not allowed to list organizations"),
	})
	@GetMapping(value = "list_organizations", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public List<OrganizationRepresentationObject> listOrganizations(@RequestHeader (name = "User-Token", required = false) String userToken) throws BusinessException {
		return organizationService.listOrganizations();
	}



	@ApiOperation(value = "list all theme classes", nickname = "themesList", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "no theme classes found"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "user not allowed to list theme classes"),
    })
    @GetMapping(value = "themes/class", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ThemeClassDTO> listThemeClasses(@RequestHeader (name = "User-Token", required = false) String userToken) {
        return themeService.listThemeClasses();
    }
    

    @ApiOperation(value = "list all themes", nickname = "themeClassesList", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "no theme found"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "user not allowed to list theme"),
    })
    @GetMapping(value = "themes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ThemeDTO> listThemes(@RequestHeader (name = "User-Token", required = false) String userToken,
									 @RequestParam(value = "class_id", required = false) Integer classId) {
        return themeService.listThemes(classId);
    }


	@ApiOperation(value = "Create/update theme class", nickname = "themeClassUpdate", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid Parameter"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "user not allowed to create theme class"),
	})
	@PostMapping(value = "themes/class", consumes = MediaType.APPLICATION_JSON_VALUE,
										 produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ThemeClassResponse updateThemeClass(@RequestHeader (name = "User-Token", required = false) String userToken,
											   @RequestBody ThemeClassDTO jsonDTO) throws BusinessException {
		return themeService.updateThemeClass(jsonDTO);
	}


	@ApiOperation(value = "Create/update theme", nickname = "themeUpdate", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid Parameter"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "user not allowed to create theme"),
	})
	@PostMapping(value = "themes", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ThemeResponse updateTheme(@RequestHeader (name = "User-Token", required = false) String userToken,
									 @RequestBody ThemeDTO jsonDTO) throws BusinessException {
		return themeService.updateTheme(jsonDTO);
	}


	@ApiOperation(value = "Delete theme class", nickname = "themeClassDeletion", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid Parameter"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "user not allowed to delete theme class"),
	})
	@DeleteMapping(value = "themes/class")
	public void deleteThemeClass(@RequestHeader (name = "User-Token", required = false) String userToken,
								 @RequestParam Integer id) throws BusinessException {
		themeService.deleteThemeClass(id);
	}


	@ApiOperation(value = "Delete theme", nickname = "themeDeletion", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid Parameter"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "user not allowed to delete theme"),
	})
	@DeleteMapping(value = "themes")
	public void deleteTheme(@RequestHeader (name = "User-Token", required = false) String userToken,
							@RequestParam String id) throws BusinessException {
		themeService.deleteTheme(id);
	}


	@ApiOperation(value = "Add area, city, country to be used for shipping", nickname = "addCountry", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid Parameter"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "user not allowed to delete theme"),
	})
	@ResponseStatus(OK)
	@PostMapping(value = "country", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void addCountry(@RequestHeader (name = "User-Token", required = false) String userToken,
						   @RequestBody CountryInfoDTO dto) {
		addressService.addCountry(dto);
	}
}
