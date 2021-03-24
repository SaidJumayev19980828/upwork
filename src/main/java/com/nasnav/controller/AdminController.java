package com.nasnav.controller;

import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.Map;

import com.nasnav.dto.*;
import com.nasnav.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.dto.request.DomainUpdateDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.CategoryResponse;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.ThemeClassResponse;
import com.nasnav.response.ThemeResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import reactor.core.publisher.Mono;

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
	
	@Autowired
	private DomainService domainService;

	@Autowired
	private AdminService adminService;

	@Autowired
	private SearchService searchService;

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


	@ApiOperation(value = "change category for tags list")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
			@io.swagger.annotations.ApiResponse(code = 409, message = "Category is used by other entities"),
	})
	@PostMapping(value = "tag/category")
	public void setTagsListCategory(@RequestHeader (name = "User-Token", required = false) String userToken,
									@RequestParam (value = "category_id") Long categoryId,
									@RequestParam(value = "tags") List<Long> tagsIds) {
		categoryService.setTagsListCategory(categoryId, tagsIds);
	}


	@ApiOperation(value = "change category for product list")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
			@io.swagger.annotations.ApiResponse(code = 409, message = "Category is used by other entities"),
	})
	@PostMapping(value = "product/category")
	public void setProductListCategory(@RequestHeader (name = "User-Token", required = false) String userToken,
									@RequestParam (value = "category_id") Long categoryId,
									@RequestParam(value = "products") List<Long> productsIds) {
		categoryService.setProductsListCategory(categoryId, productsIds);
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


	@ApiOperation(value = "Add all areas, cities, countries to be used for shipping", nickname = "addCountries", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid Parameter"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "user not allowed to delete theme"),
	})
	@ResponseStatus(OK)
	@PostMapping(value = "country/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void addCountries(@RequestHeader (name = "User-Token", required = false) String userToken,
						     @RequestBody List<CountryDTO> dto) {
		addressService.addCountries(dto);
	}


	@ApiOperation(value = "remove area, city, country", nickname = "removeCountry", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid Parameter"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "user not allowed to delete theme"),
	})
	@ResponseStatus(OK)
	@DeleteMapping(value = "country")
	public void removeCountry(@RequestHeader (name = "User-Token", required = false) String userToken,
						      @RequestParam Long id,
							  @RequestParam String type) {
		addressService.removeCountry(id, type);
	}
	
	
	
	@ApiOperation(value = "add/update organizatoin domain", nickname = "updateDomain", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid Parameter"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "user not allowed to delete theme"),
	})
	@ResponseStatus(OK)
	@PostMapping(value = "organization/domain", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void updateDomain(@RequestHeader (name = "User-Token", required = false) String userToken,
						   @RequestBody DomainUpdateDTO dto) {
		domainService.updateDomain(dto);
	}


	@ApiOperation(value = "invalidate all caches", nickname = "invalidateCache", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid Parameter"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "user not allowed to delete theme"),
	})
	@ResponseStatus(OK)
	@PostMapping(value = "cache/invalidate")
	public void invalidateAllCaches(@RequestHeader (name = "User-Token", required = false) String userToken) {
		adminService.invalidateCaches();
	}




	@ApiOperation(value = "Get organization domain", nickname = "getOrgDomain")
	@ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)")})
	@GetMapping(value = "organization/domain", produces = MediaType.TEXT_PLAIN_VALUE )
	public String getOrgDomain(@RequestHeader(name = "User-Token", required = false) String userToken,
							   @RequestParam Long id) {
		return domainService.getOrganizationDomainAndSubDir(id);
	}

	@ApiOperation(value = "Get organization domains", nickname = "getOrgDomains")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)")})
	@GetMapping(value = "organization/domains", produces = MediaType.APPLICATION_JSON_VALUE )
	public List<DomainUpdateDTO> getOrgDomains(@RequestHeader(name = "User-Token", required = false) String userToken,
										   @RequestParam("org_id") Long id) {
		return domainService.getOrganizationDomains(id);
	}

	@ApiOperation(value = "delete organization domain", nickname = "deleteOrgDomain")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)")})
	@DeleteMapping(value = "organization/domain")
	public void deleteOrgDomain(@RequestHeader(name = "User-Token", required = false) String userToken,
											@RequestParam Long id,
										    @RequestParam("org_id") Long orgId) {
		domainService.deleteOrgDomain(id, orgId);
	}

	@ApiOperation(value = "delete all indices on elastic search", nickname = "deleteElasticSearch", code = 200)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid Parameter"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "user not allowed to delete theme"),
	})
	@ResponseStatus(OK)
	@DeleteMapping(value = "search/indices")
	public Mono<Void> deleteSearchIndices(@RequestHeader (name = "User-Token", required = false) String userToken) {
		return searchService.deleteAllIndices();
	}
}
