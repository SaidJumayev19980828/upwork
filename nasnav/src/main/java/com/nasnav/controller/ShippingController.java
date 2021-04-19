package com.nasnav.controller;

import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.ShippingManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shipping")
@Tag(name = "Methods for accessing and controlling shipments")
public class ShippingController {
	
	
	@Autowired
	private ShippingManagementService shippingService;
	
	
	@Operation(description =  "get shipping offers", summary = "getShippingOffers")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid parameters")
	})
	@GetMapping(path = "/offers", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<ShippingOfferDTO> getShippingOffers(
			@RequestHeader(name = "User-Token", required = false) String userToken
			, @RequestParam("customer_address") Long customerAddress) throws BusinessException {
		return shippingService.getShippingOffers(customerAddress);
	}
}
