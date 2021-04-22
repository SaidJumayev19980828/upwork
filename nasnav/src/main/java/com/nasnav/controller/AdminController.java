package com.nasnav.controller;

import com.nasnav.dto.*;
import com.nasnav.dto.request.DomainUpdateDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.CategoryResponse;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.ThemeClassResponse;
import com.nasnav.response.ThemeResponse;
import com.nasnav.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/admin")
@Tag(name = "Set of endpoints for adding, updating and deleting Dashboard data.")
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

    @Operation(description =  "Create/update an Organization", summary = "OrganizationCreation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "organization",
				produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
				consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public OrganizationResponse createOrganization(@RequestHeader (name = "User-Token", required = false) String userToken,
												   @RequestBody OrganizationDTO.OrganizationCreationDTO json)  throws BusinessException {
	    return organizationService.createOrganization(json);
    }


	@Operation(description =  "Create or update a Category", summary = "categoryModification")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
			@ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
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


	@Operation(description =  "change category for tags list")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
			@ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
			@ApiResponse(responseCode = " 409" ,description = "Category is used by other entities"),
	})
	@PostMapping(value = "tag/category")
	public void setTagsListCategory(@RequestHeader (name = "User-Token", required = false) String userToken,
									@RequestBody UpdateTagsCategoryDTO updateDto) {
		categoryService.setTagsListCategory(updateDto);
	}


	@Operation(description =  "change category for product list")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
			@ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
			@ApiResponse(responseCode = " 409" ,description = "Category is used by other entities"),
	})
	@PostMapping(value = "product/category")
	public void setProductListCategory(@RequestHeader (name = "User-Token", required = false) String userToken,
									@RequestBody UpdateProductsCategoryDTO updateDto) {
		categoryService.setProductsListCategory(updateDto);
	}


	@Operation(description =  "delete a Category", summary = "categoryDeletion")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
			@ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
			@ApiResponse(responseCode = " 409" ,description = "Category is used by other entities"),
	})
	@DeleteMapping(value = "category", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public CategoryResponse deleteCategory(@RequestHeader (name = "User-Token", required = false) String userToken,
	                                       @RequestParam (value = "category_id") Long categoryId ) throws BusinessException {
		return categoryService.deleteCategory(categoryId);
	}


	@Operation(description =  "list all organizations", summary = "orgList")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
			@ApiResponse(responseCode = " 404" ,description = "no organizations found"),
			@ApiResponse(responseCode = " 401" ,description = "user not allowed to list organizations"),
	})
	@GetMapping(value = "list_organizations", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public List<OrganizationRepresentationObject> listOrganizations(@RequestHeader (name = "User-Token", required = false) String userToken) throws BusinessException {
		return organizationService.listOrganizations();
	}



	@Operation(description =  "list all theme classes", summary = "themesList")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 404" ,description = "no theme classes found"),
            @ApiResponse(responseCode = " 401" ,description = "user not allowed to list theme classes"),
    })
    @GetMapping(value = "themes/class", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ThemeClassDTO> listThemeClasses(@RequestHeader (name = "User-Token", required = false) String userToken) {
        return themeService.listThemeClasses();
    }
    

    @Operation(description =  "list all themes", summary = "themeClassesList")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 404" ,description = "no theme found"),
            @ApiResponse(responseCode = " 401" ,description = "user not allowed to list theme"),
    })
    @GetMapping(value = "themes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ThemeDTO> listThemes(@RequestHeader (name = "User-Token", required = false) String userToken,
									 @RequestParam(value = "class_id", required = false) Integer classId) {
        return themeService.listThemes(classId);
    }


	@Operation(description =  "Create/update theme class", summary = "themeClassUpdate")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid Parameter"),
			@ApiResponse(responseCode = " 401" ,description = "user not allowed to create theme class"),
	})
	@PostMapping(value = "themes/class", consumes = MediaType.APPLICATION_JSON_VALUE,
										 produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ThemeClassResponse updateThemeClass(@RequestHeader (name = "User-Token", required = false) String userToken,
											   @RequestBody ThemeClassDTO jsonDTO) throws BusinessException {
		return themeService.updateThemeClass(jsonDTO);
	}


	@Operation(description =  "Create/update theme", summary = "themeUpdate")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid Parameter"),
			@ApiResponse(responseCode = " 401" ,description = "user not allowed to create theme"),
	})
	@PostMapping(value = "themes", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ThemeResponse updateTheme(@RequestHeader (name = "User-Token", required = false) String userToken,
									 @RequestBody ThemeDTO jsonDTO) throws BusinessException {
		return themeService.updateTheme(jsonDTO);
	}


	@Operation(description =  "Delete theme class", summary = "themeClassDeletion")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid Parameter"),
			@ApiResponse(responseCode = " 401" ,description = "user not allowed to delete theme class"),
	})
	@DeleteMapping(value = "themes/class")
	public void deleteThemeClass(@RequestHeader (name = "User-Token", required = false) String userToken,
								 @RequestParam Integer id) throws BusinessException {
		themeService.deleteThemeClass(id);
	}


	@Operation(description =  "Delete theme", summary = "themeDeletion")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid Parameter"),
			@ApiResponse(responseCode = " 401" ,description = "user not allowed to delete theme"),
	})
	@DeleteMapping(value = "themes")
	public void deleteTheme(@RequestHeader (name = "User-Token", required = false) String userToken,
							@RequestParam String id) throws BusinessException {
		themeService.deleteTheme(id);
	}


	@Operation(description =  "Add area, city, country to be used for shipping", summary = "addCountry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid Parameter"),
			@ApiResponse(responseCode = " 401" ,description = "user not allowed to delete theme"),
	})
	@ResponseStatus(OK)
	@PostMapping(value = "country", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void addCountry(@RequestHeader (name = "User-Token", required = false) String userToken,
						   @RequestBody CountryInfoDTO dto) {
		addressService.addCountry(dto);
	}


	@Operation(description =  "Add all areas, cities, countries to be used for shipping", summary = "addCountries")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid Parameter"),
			@ApiResponse(responseCode = " 401" ,description = "user not allowed to delete theme"),
	})
	@ResponseStatus(OK)
	@PostMapping(value = "country/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void addCountries(@RequestHeader (name = "User-Token", required = false) String userToken,
						     @RequestBody List<CountryDTO> dto) {
		addressService.addCountries(dto);
	}


	@Operation(description =  "remove area, city, country", summary = "removeCountry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid Parameter"),
			@ApiResponse(responseCode = " 401" ,description = "user not allowed to delete theme"),
	})
	@ResponseStatus(OK)
	@DeleteMapping(value = "country")
	public void removeCountry(@RequestHeader (name = "User-Token", required = false) String userToken,
						      @RequestParam Long id,
							  @RequestParam String type) {
		addressService.removeCountry(id, type);
	}
	
	
	
	@Operation(description =  "add/update organizatoin domain", summary = "updateDomain")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid Parameter"),
			@ApiResponse(responseCode = " 401" ,description = "user not allowed to delete theme"),
	})
	@ResponseStatus(OK)
	@PostMapping(value = "organization/domain", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void updateDomain(@RequestHeader (name = "User-Token", required = false) String userToken,
						   @RequestBody DomainUpdateDTO dto) {
		domainService.updateDomain(dto);
	}


	@Operation(description =  "invalidate all caches", summary = "invalidateCache")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid Parameter"),
			@ApiResponse(responseCode = " 401" ,description = "user not allowed to delete theme"),
	})
	@ResponseStatus(OK)
	@PostMapping(value = "cache/invalidate")
	public void invalidateAllCaches(@RequestHeader (name = "User-Token", required = false) String userToken) {
		adminService.invalidateCaches();
	}




	@Operation(description =  "Get organization domain", summary = "getOrgDomain")
	@ApiResponses(value = {@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)")})
	@GetMapping(value = "organization/domain", produces = MediaType.TEXT_PLAIN_VALUE )
	public String getOrgDomain(@RequestHeader(name = "User-Token", required = false) String userToken,
							   @RequestParam Long id) {
		return domainService.getOrganizationDomainAndSubDir(id);
	}

	@Operation(description =  "Get organization domains", summary = "getOrgDomains")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)")})
	@GetMapping(value = "organization/domains", produces = MediaType.APPLICATION_JSON_VALUE )
	public List<DomainUpdateDTO> getOrgDomains(@RequestHeader(name = "User-Token", required = false) String userToken,
										   @RequestParam("org_id") Long id) {
		return domainService.getOrganizationDomains(id);
	}

	@Operation(description =  "delete organization domain", summary = "deleteOrgDomain")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)")})
	@DeleteMapping(value = "organization/domain")
	public void deleteOrgDomain(@RequestHeader(name = "User-Token", required = false) String userToken,
											@RequestParam Long id,
										    @RequestParam("org_id") Long orgId) {
		domainService.deleteOrgDomain(id, orgId);
	}

	@Operation(description =  "delete all indices on elastic search", summary = "deleteElasticSearch")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid Parameter"),
			@ApiResponse(responseCode = " 401" ,description = "user not allowed to delete theme"),
	})
	@ResponseStatus(OK)
	@DeleteMapping(value = "search/indices")
	public Mono<Void> deleteSearchIndices(@RequestHeader (name = "User-Token", required = false) String userToken) {
		return searchService.deleteAllIndices();
	}
}
