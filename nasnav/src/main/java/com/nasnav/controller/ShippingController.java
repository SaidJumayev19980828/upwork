package com.nasnav.controller;

import com.nasnav.dto.request.shipping.ShippingOfferDTO;
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

	@GetMapping(path = "/offers", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<ShippingOfferDTO> getShippingOffers(@RequestHeader(name = "User-Token", required = false) String userToken,
													@RequestParam("customer_address") Long customerAddress) {
		return shippingService.getShippingOffers(customerAddress);
	}
}
