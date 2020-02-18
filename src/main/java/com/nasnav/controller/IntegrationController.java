package com.nasnav.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.dto.IntegrationDictionaryDTO;
import com.nasnav.dto.IntegrationErrorDTO;
import com.nasnav.dto.IntegrationParamDTO;
import com.nasnav.dto.IntegrationParamDeleteDTO;
import com.nasnav.dto.IntegrationProductImportDTO;
import com.nasnav.dto.OrganizationIntegrationInfoDTO;
import com.nasnav.dto.ResponsePage;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.request.GetIntegrationDictParam;
import com.nasnav.request.GetIntegrationErrorParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/integration")
@Api(description = "Set of endpoints for adding, updating and deleting Dashboard data.")
@CrossOrigin("*") // allow all origins
public class IntegrationController {
	
	
	@Autowired
	IntegrationService integrationSrv;
	
	
	@ApiOperation(value = "Register a new integration module for an organization", nickname = "IntegrationModuleRegister", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "module", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void registerIntegrationModule(@RequestHeader (value = "User-Token") String userToken,
                                             @RequestBody OrganizationIntegrationInfoDTO integrationInfo)  throws BusinessException {
		integrationSrv.registerIntegrationModule(integrationInfo);
    }
	
	
	
	
	
	
	@ApiOperation(value = "Remove integration module for an organization", nickname = "IntegrationModuleRegister", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "module", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void removeIntegrationModule(@RequestHeader (value = "User-Token") String userToken,
    										@RequestParam("organization_id") Long organizationId)  throws BusinessException {
		integrationSrv.removeIntegrationModule(organizationId);
    }
	
	
	
	
	
	
	
	@ApiOperation(value = "list integration modules for all organizations", nickname = "IntegrationModuleAllGet", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "module/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<OrganizationIntegrationInfoDTO> getAllIntegrationModules(@RequestHeader (value = "User-Token") String userToken)  throws BusinessException {
		return integrationSrv.getAllIntegrationModules();
    }
	
	
	
	
	
	
	@ApiOperation(value = "Disable integration module for an organization", nickname = "IntegrationModuleDisable", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "module/disable")
    public void disableIntegrationModule(@RequestHeader (value = "User-Token") String userToken,
                                             @RequestParam("organization_id") Long organizationId)  throws BusinessException {
		integrationSrv.disableIntegrationModule(organizationId);
    }
	
	
	
	
	
	@ApiOperation(value = "Enable an already disabled integration module for an organization", nickname = "IntegrationModuleEnable", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "module/enable")
    public void enableIntegrationModule(@RequestHeader (value = "User-Token") String userToken,
                                             @RequestParam("organization_id") Long organizationId)  throws BusinessException {
		integrationSrv.enableIntegrationModule(organizationId);
    }
	
	
	
	
	
	
	@ApiOperation(value = "Add/update an integration parameter for an organization", nickname = "IntegrationParamAdd", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "param", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void saveIntegrationParamModule(@RequestHeader (value = "User-Token") String userToken,
                                             @RequestBody IntegrationParamDTO param)  throws BusinessException {
		integrationSrv.addIntegrationParam(param);
    }
	
	
	
	
	
	
	@ApiOperation(value = "Delete an integration parameter for an organization", nickname = "IntegrationParamAdd", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "param", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void deleteIntegrationParam(@RequestHeader (value = "User-Token") String userToken,
                                             @RequestBody IntegrationParamDeleteDTO param)  throws BusinessException {
		integrationSrv.deleteIntegrationParam(param);
    }
	
	
	
	
	@ApiOperation(value = "import the organization shops from external system", nickname = "ShopsImport", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "/import/shops", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Long> importShops(@RequestHeader (value = "User-Token") String userToken)  throws Throwable {
		return integrationSrv.importShops();
    }
	
	
	
	
	@ApiOperation(value = "import the products from external system", nickname = "ProductsImport", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "/import/products", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Integer importProdcuts(@RequestHeader (value = "User-Token") String userToken,@RequestBody IntegrationProductImportDTO metadata)  throws Throwable {
		return integrationSrv.importOrganizationProducts(metadata);
    }
	
	
	
	
	
	@ApiOperation(value = "get the integration dictionary - mapping between values nasnav and external systems"
			, nickname = "GetIntegrationDictionary", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "/dictionary", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponsePage<IntegrationDictionaryDTO> getDictionary(@RequestHeader (value = "User-Token") String userToken, GetIntegrationDictParam param )  throws Throwable {
		return integrationSrv.getIntegrationDictionary(param);
    }
	
	
	
	
	@ApiOperation(value = "get the integration errors"
			, nickname = "GetIntegrationErrors", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "/errors", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponsePage<IntegrationErrorDTO> getErrors(@RequestHeader (value = "User-Token") String userToken, GetIntegrationErrorParam param )  throws Throwable {
		return integrationSrv.getIntegrationErrors(param);
    }
}
