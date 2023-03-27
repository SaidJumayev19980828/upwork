package com.nasnav.controller;

import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.service.SecurityService;
import com.nasnav.service.ShippingManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shipping")
public class ShippingController {

	@Autowired
	private ShippingManagementService shippingService;
	@Autowired
	private SecurityService securityService;

	@GetMapping(path = "/offers", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<ShippingOfferDTO> getShippingOffers(@RequestHeader(name = "User-Token", required = false) String userToken,
													@RequestParam(value = "customer_address", required = false, defaultValue = "-1") Long customerAddress,
													@RequestParam(value = "payment_method_id", required = false, defaultValue = "") String paymentMethodId,
													@RequestParam(value = "shipping_service_id", required = false, defaultValue = "") String shippingServiceId) {
		return shippingService.getShippingOffers(customerAddress, paymentMethodId, shippingServiceId);
	}
}
