package com.nasnav.controller;

import static java.util.Collections.emptyList;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.exceptions.BusinessException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/shipping")
@Api(description = "Methods for accessing and controlling shipments")
public class ShippingController {
	
	
	@ApiOperation(value = "get shipping offers", nickname = "getShippingOffers")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid parameters")
	})
	@GetMapping(path = "/offers", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<ShippingOfferDTO> getShippingOffers(
			@RequestHeader(name = "User-Token", required = false) String userToken
			, @RequestParam("customer_address") Long customer_address) throws BusinessException {
		return emptyList();
	}
}
