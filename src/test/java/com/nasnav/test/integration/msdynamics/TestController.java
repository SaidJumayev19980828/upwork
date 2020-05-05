package com.nasnav.test.integration.msdynamics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.exceptions.InvalidIntegrationEventException;


/**
 * just a work around for wrapping methods that needs authentication to happen first.
 * */
@RestController
@RequestMapping("/test/integration")
public class TestController {

	@Autowired
	IntegrationService integrationService;
	
	
	
	@PostMapping("/get_stock")
	public Integer getExternalStock(@RequestParam("variant_id") Long variantId
			, @RequestParam("shop_id") Long shopId) throws InvalidIntegrationEventException, BusinessException {
		return integrationService.getExternalStock(variantId, shopId).orElse(-1);
	}
	
}
