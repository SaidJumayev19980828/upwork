package com.nasnav.controller;

import com.nasnav.dto.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.request.GetIntegrationDictParam;
import com.nasnav.request.GetIntegrationErrorParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping("/integration")
@Tag(name = "Set of endpoints for adding, updating and deleting Dashboard data.")
@CrossOrigin("*") // allow all origins
public class IntegrationController {
	
	
	@Autowired
	IntegrationService integrationSrv;
	
	
	@Operation(description =  "Register a new integration module for an organization", summary = "IntegrationModuleRegister")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "module", produces = APPLICATION_JSON_UTF8_VALUE, consumes = APPLICATION_JSON_UTF8_VALUE)
    public void registerIntegrationModule(@RequestHeader (name = "User-Token", required = false) String userToken,
                                             @RequestBody OrganizationIntegrationInfoDTO integrationInfo)  throws BusinessException {
		integrationSrv.registerIntegrationModule(integrationInfo);
    }
	
	
	
	
	
	
	@Operation(description =  "Remove integration module for an organization", summary = "IntegrationModuleRegister")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "module", produces = APPLICATION_JSON_UTF8_VALUE, consumes = APPLICATION_JSON_UTF8_VALUE)
    public void removeIntegrationModule(@RequestHeader (name = "User-Token", required = false) String userToken,
    										@RequestParam("organization_id") Long organizationId)  throws BusinessException {
		integrationSrv.removeIntegrationModule(organizationId);
    }
	
	
	
	
	
	
	
	@Operation(description =  "list integration modules for all organizations", summary = "IntegrationModuleAllGet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "module/all", produces = APPLICATION_JSON_UTF8_VALUE)
    public List<OrganizationIntegrationInfoDTO> getAllIntegrationModules(@RequestHeader (name = "User-Token", required = false) String userToken)  throws BusinessException {
		return integrationSrv.getAllIntegrationModules();
    }
	
	
	
	
	
	
	@Operation(description =  "Disable integration module for an organization", summary = "IntegrationModuleDisable")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "module/disable")
    public void disableIntegrationModule(@RequestHeader (name = "User-Token", required = false) String userToken,
                                             @RequestParam("organization_id") Long organizationId)  throws BusinessException {
		integrationSrv.disableIntegrationModule(organizationId);
    }
	
	
	
	
	
	@Operation(description =  "Enable an already disabled integration module for an organization", summary = "IntegrationModuleEnable")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "module/enable")
    public void enableIntegrationModule(@RequestHeader (name = "User-Token", required = false) String userToken,
                                             @RequestParam("organization_id") Long organizationId)  throws BusinessException {
		integrationSrv.enableIntegrationModule(organizationId);
    }
	
	
	
	
	
	
	@Operation(description =  "Add/update an integration parameter for an organization", summary = "IntegrationParamAdd")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "param", produces = APPLICATION_JSON_UTF8_VALUE, consumes = APPLICATION_JSON_UTF8_VALUE)
    public void saveIntegrationParamModule(@RequestHeader (name = "User-Token", required = false) String userToken,
                                             @RequestBody IntegrationParamDTO param)  throws BusinessException {
		integrationSrv.addIntegrationParam(param);
    }
	
	
	
	
	
	
	@Operation(description =  "Delete an integration parameter for an organization", summary = "IntegrationParamAdd")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "param", produces = APPLICATION_JSON_UTF8_VALUE, consumes = APPLICATION_JSON_UTF8_VALUE)
    public void deleteIntegrationParam(@RequestHeader (name = "User-Token", required = false) String userToken,
                                             @RequestBody IntegrationParamDeleteDTO param)  throws BusinessException {
		integrationSrv.deleteIntegrationParam(param);
    }
	
	
	
	
	@Operation(description =  "import the organization shops from external system", summary = "ShopsImport")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "/import/shops", produces = APPLICATION_JSON_UTF8_VALUE)
    public List<Long> importShops(@RequestHeader (name = "User-Token", required = false) String userToken)  throws Throwable {
		return integrationSrv.importShops();
    }
	
	
	
	
	@Operation(description =  "import the products from external system", summary = "ProductsImport")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "/import/products", produces = APPLICATION_JSON_UTF8_VALUE, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Integer importProdcuts(@RequestHeader (name = "User-Token", required = false) String userToken,@RequestBody IntegrationProductImportDTO metadata)  throws Throwable {
		return integrationSrv.importOrganizationProducts(metadata);
    }
	
	
	
	
	
	@Operation(description =  "get the integration dictionary - mapping between values nasnav and external systems"
			, summary = "GetIntegrationDictionary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "/dictionary", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponsePage<IntegrationDictionaryDTO> getDictionary(@RequestHeader (name = "User-Token", required = false) String userToken, GetIntegrationDictParam param )  throws Throwable {
		return integrationSrv.getIntegrationDictionary(param);
    }
	
	
	
	
	@Operation(description =  "get the integration errors"
			, summary = "GetIntegrationErrors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "/errors", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponsePage<IntegrationErrorDTO> getErrors(@RequestHeader (name = "User-Token", required = false) String userToken, GetIntegrationErrorParam param )  throws Throwable {
		return integrationSrv.getIntegrationErrors(param);
    }
	
	
	
	
	@Operation(description =  "import the product image from external system", summary = "ProductImagesImport")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "/import/product_images", produces = APPLICATION_JSON_UTF8_VALUE, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponsePage<Void> importProdcutImages(@RequestHeader (name = "User-Token", required = false) String userToken,@RequestBody IntegrationImageImportDTO metadata)  throws Throwable {
		return integrationSrv.importProductImages(metadata);
    }
}
