package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.request.GetIntegrationDictParam;
import com.nasnav.request.GetIntegrationErrorParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/integration")
@CrossOrigin("*") // allow all origins
public class IntegrationController {

	@Autowired
	IntegrationService integrationSrv;


    @PostMapping(value = "module", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public void registerIntegrationModule(@RequestHeader (name = "User-Token", required = false) String userToken,
                                             @RequestBody OrganizationIntegrationInfoDTO integrationInfo)  throws BusinessException {
		integrationSrv.registerIntegrationModule(integrationInfo);
    }


    @DeleteMapping(value = "module", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public void removeIntegrationModule(@RequestHeader (name = "User-Token", required = false) String userToken,
    										@RequestParam("organization_id") Long organizationId) {
		integrationSrv.removeIntegrationModule(organizationId);
    }


    @GetMapping(value = "module/all", produces = APPLICATION_JSON_VALUE)
    public List<OrganizationIntegrationInfoDTO> getAllIntegrationModules(@RequestHeader (name = "User-Token", required = false) String userToken) {
		return integrationSrv.getAllIntegrationModules();
    }


    @PostMapping(value = "module/disable")
    public void disableIntegrationModule(@RequestHeader (name = "User-Token", required = false) String userToken,
                                         @RequestParam("organization_id") Long organizationId)  throws BusinessException {
		integrationSrv.disableIntegrationModule(organizationId);
    }


    @PostMapping(value = "module/enable")
    public void enableIntegrationModule(@RequestHeader (name = "User-Token", required = false) String userToken,
                                             @RequestParam("organization_id") Long organizationId)  throws BusinessException {
		integrationSrv.enableIntegrationModule(organizationId);
    }


    @PostMapping(value = "param", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public void saveIntegrationParamModule(@RequestHeader (name = "User-Token", required = false) String userToken,
                                             @RequestBody IntegrationParamDTO param)  throws BusinessException {
		integrationSrv.addIntegrationParam(param);
    }

    @DeleteMapping(value = "param", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public void deleteIntegrationParam(@RequestHeader (name = "User-Token", required = false) String userToken,
                                             @RequestBody IntegrationParamDeleteDTO param)  throws BusinessException {
		integrationSrv.deleteIntegrationParam(param);
    }

    @GetMapping(value = "/import/shops", produces = APPLICATION_JSON_VALUE)
    public List<Long> importShops(@RequestHeader (name = "User-Token", required = false) String userToken)  throws Throwable {
		return integrationSrv.importShops();
    }

    @PostMapping(value = "/import/products", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public Integer importProducts(@RequestHeader (name = "User-Token", required = false) String userToken,
                                  @RequestBody IntegrationProductImportDTO metadata)  throws Throwable {
		return integrationSrv.importOrganizationProducts(metadata);
    }

    @GetMapping(value = "/dictionary", produces = APPLICATION_JSON_VALUE)
    public ResponsePage<IntegrationDictionaryDTO> getDictionary(@RequestHeader (name = "User-Token", required = false) String userToken,
                                                                GetIntegrationDictParam param )  throws Throwable {
		return integrationSrv.getIntegrationDictionary(param);
    }

    @GetMapping(value = "/errors", produces = APPLICATION_JSON_VALUE)
    public ResponsePage<IntegrationErrorDTO> getErrors(@RequestHeader (name = "User-Token", required = false) String userToken,
                                                       GetIntegrationErrorParam param )  throws Throwable {
		return integrationSrv.getIntegrationErrors(param);
    }

    @PostMapping(value = "/import/product_images", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponsePage<Void> importProductImages(@RequestHeader (name = "User-Token", required = false) String userToken,
                                                  @RequestBody IntegrationImageImportDTO metadata)  throws Throwable {
		return integrationSrv.importProductImages(metadata);
    }
}
