package com.nasnav.controller;

import com.nasnav.dto.*;
import com.nasnav.dto.request.BrandIdAndPriority;
import com.nasnav.dto.request.DomainUpdateDTO;
import com.nasnav.dto.request.organization.OrganizationCreationDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.CategoryResponse;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.ThemeClassResponse;
import com.nasnav.response.ThemeResponse;
import com.nasnav.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
@RequestMapping("/admin")
@CrossOrigin("*")
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
	@Autowired
	private BrandService brandService;

    @PostMapping(value = "organization", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public OrganizationResponse createOrganization(@RequestHeader (name = "User-Token", required = false) String userToken,
												   @RequestBody OrganizationCreationDTO json)  throws BusinessException {
	    return organizationService.createOrganization(json);
    }

	@PostMapping(value = "category", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	public CategoryResponse createCategory(@RequestHeader (name = "User-Token", required = false) String userToken,
										   @RequestBody CategoryDTO categoryJson) {
    	return categoryService.addOrUpdateCategory(categoryJson);
	}
	
	@PostMapping(value = "tag/category", consumes = APPLICATION_JSON_VALUE)
	public void setTagsListCategory(@RequestHeader (name = "User-Token", required = false) String userToken,
									@RequestBody UpdateTagsCategoryDTO updateDto) {
		categoryService.setTagsListCategory(updateDto);
	}

	@PostMapping(value = "product/category", consumes = APPLICATION_JSON_VALUE)
	public void setProductListCategory(@RequestHeader (name = "User-Token", required = false) String userToken,
									@RequestBody UpdateProductsCategoryDTO updateDto) {
		categoryService.setProductsListCategory(updateDto);
	}
	
	@DeleteMapping(value = "category", produces = APPLICATION_JSON_VALUE)
	public CategoryResponse deleteCategory(@RequestHeader (name = "User-Token", required = false) String userToken,
	                                       @RequestParam (value = "category_id") Long categoryId ) throws BusinessException {
		return categoryService.deleteCategory(categoryId);
	}

	@GetMapping(value = "list_organizations", produces = APPLICATION_JSON_VALUE)
	public List<OrganizationRepresentationObject> listOrganizations(@RequestHeader (name = "User-Token", required = false) String userToken) {
		return organizationService.listOrganizations();
	}

    @GetMapping(value = "themes/class", produces = APPLICATION_JSON_VALUE)
    public List<ThemeClassDTO> listThemeClasses(@RequestHeader (name = "User-Token", required = false) String userToken) {
        return themeService.listThemeClasses();
    }

    @GetMapping(value = "themes", produces = APPLICATION_JSON_VALUE)
    public List<ThemeDTO> listThemes(@RequestHeader (name = "User-Token", required = false) String userToken,
									 @RequestParam(value = "class_id", required = false) Integer classId) {
        return themeService.listThemes(classId);
    }

	@PostMapping(value = "themes/class", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ThemeClassResponse updateThemeClass(@RequestHeader (name = "User-Token", required = false) String userToken,
											   @RequestBody ThemeClassDTO jsonDTO) throws BusinessException {
		return themeService.updateThemeClass(jsonDTO);
	}

	@PostMapping(value = "themes", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ThemeResponse updateTheme(@RequestHeader (name = "User-Token", required = false) String userToken,
									 @RequestBody ThemeDTO jsonDTO) throws BusinessException {
		return themeService.updateTheme(jsonDTO);
	}

	@DeleteMapping(value = "themes/class")
	public void deleteThemeClass(@RequestHeader (name = "User-Token", required = false) String userToken,
								 @RequestParam Integer id) throws BusinessException {
		themeService.deleteThemeClass(id);
	}

	@DeleteMapping(value = "themes")
	public void deleteTheme(@RequestHeader (name = "User-Token", required = false) String userToken,
							@RequestParam String id) throws BusinessException {
		themeService.deleteTheme(id);
	}

	@PostMapping(value = "country", consumes = APPLICATION_JSON_VALUE)
	public void addCountry(@RequestHeader (name = "User-Token", required = false) String userToken,
						   @RequestBody CountryInfoDTO dto) {
		addressService.addCountry(dto);
	}

	@PostMapping(value = "country/bulk", consumes = APPLICATION_JSON_VALUE)
	public void addCountries(@RequestHeader (name = "User-Token", required = false) String userToken,
						     @RequestBody List<CountryDTO> dto) {
		addressService.addCountries(dto);
	}

	@DeleteMapping(value = "country")
	public void removeCountry(@RequestHeader (name = "User-Token", required = false) String userToken,
						      @RequestParam Long id,
							  @RequestParam String type) {
		addressService.removeCountry(id, type);
	}

	@PostMapping(value = "organization/domain", consumes = APPLICATION_JSON_VALUE)
	public void updateDomain(@RequestHeader (name = "User-Token", required = false) String userToken,
						     @RequestBody DomainUpdateDTO dto) {
		domainService.updateDomain(dto);
	}

	@PostMapping(value = "cache/invalidate")
	public void invalidateAllCaches(@RequestHeader (name = "User-Token", required = false) String userToken) {
		adminService.invalidateCaches();
	}

	@GetMapping(value = "organization/domain", produces = TEXT_PLAIN_VALUE )
	public String getOrgDomain(@RequestHeader(name = "User-Token", required = false) String userToken,
							   @RequestParam Long id) {
		return domainService.getOrganizationDomainAndSubDir(id);
	}

	@GetMapping(value = "organization/domains", produces = APPLICATION_JSON_VALUE )
	public List<DomainUpdateDTO> getOrgDomains(@RequestHeader(name = "User-Token", required = false) String userToken,
										   	   @RequestParam("org_id") Long id) {
		return domainService.getOrganizationDomains(id);
	}

	@DeleteMapping(value = "organization/domain")
	public void deleteOrgDomain(@RequestHeader(name = "User-Token", required = false) String userToken,
								@RequestParam Long id,
								@RequestParam("org_id") Long orgId) {
		domainService.deleteOrgDomain(id, orgId);
	}

	@DeleteMapping(value = "search/indices")
	public Mono<Void> deleteSearchIndices(@RequestHeader (name = "User-Token", required = false) String userToken) {
		return searchService.deleteAllIndices();
	}

	@PostMapping(value = "priority/brands", consumes = APPLICATION_JSON_VALUE)
	public void bulkUpdateBrandsPriority(@RequestHeader(name = "User-Token", required = false) String userToken,
										 @RequestBody List<BrandIdAndPriority> brandIdAndPriorityList) {
		brandService.changeBrandsPriority(brandIdAndPriorityList);
	}
}
