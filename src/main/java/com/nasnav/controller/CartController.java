package com.nasnav.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.OrderService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/cart")
@Api(description = "Methods for accessing public information about shops and products.")
public class CartController {
	
	@Autowired
	private OrderService orderService;

	@ApiOperation(value = "get user cart", nickname = "getCart")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "invalid search parameter")
	})
	@GetMapping(produces=MediaType.APPLICATION_JSON_VALUE)
	public Cart getCart(@RequestHeader(name = "User-Token", required = false) String userToken) throws BusinessException {
		return orderService.getCart();
	}
}
