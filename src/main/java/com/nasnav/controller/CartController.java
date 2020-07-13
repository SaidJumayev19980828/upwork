package com.nasnav.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.cart.CartOptimizeDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.CartOptimizeResponseDTO;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.OrderService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/cart")
@Api(description = "Methods for accessing cart")
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



	@ApiOperation(value = "add an item to the cart", nickname = "addCartItem")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "employee user can't have cart"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "stock not found")
	})
	@PostMapping(value = "/item", consumes = MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public Cart addCartItem(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestBody CartItem item) {
		return orderService.addCartItem(item);
	}



	@ApiOperation(value = "delete an item from the cart", nickname = "deleteCartItem")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "employee user can't delete cart item"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "item not found")
	})
	@DeleteMapping(value = "/item", produces=MediaType.APPLICATION_JSON_VALUE)
	public Cart deleteCartItem(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestParam("item_id") Long itemId) {
		return orderService.deleteCartItem(itemId);
	}



	@ApiOperation(value = "checkout the cart", nickname = "cartCheckout")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "employee user can't have cart"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "stock not found")
	})
	@PostMapping(value = "/checkout", consumes = MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public Order checkoutCart(@RequestHeader(name = "User-Token", required = false) String userToken,
							  @RequestBody CartCheckoutDTO dto) throws BusinessException, IOException {
		return orderService.checkoutCart(dto);
	}
	
	
	
	
	@ApiOperation(value = "checkout the cart", nickname = "cartCheckout")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "employee user can't have cart"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "stock not found")
	})
	@PostMapping(value = "/optimize", consumes = MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public CartOptimizeResponseDTO optimizeCart(@RequestHeader(name = "User-Token", required = false) String userToken,
							  @RequestBody CartOptimizeDTO dto) throws BusinessException, IOException {
		return orderService.optimizeCart(dto);
	}
}
