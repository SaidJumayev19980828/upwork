package com.nasnav.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.math.BigDecimal;

import com.nasnav.service.CartService;
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
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.CartOptimizeResponseDTO;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.CartOptimizationService;
import com.nasnav.service.PromotionsService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/cart")
@Api(description = "Methods for accessing cart")
public class CartController {
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private PromotionsService promoService;
	
	@Autowired
	private CartOptimizationService cartOptimizeService;

	@ApiOperation(value = "get user cart", nickname = "getCart")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "invalid search parameter")
	})
	@GetMapping(produces=APPLICATION_JSON_VALUE)
	public Cart getCart(@RequestHeader(name = "User-Token", required = false) String userToken,
						@RequestParam(value = "promo", required = false) String promoCode) {
		return cartService.getCart(promoCode);
	}



	@ApiOperation(value = "add an item to the cart", nickname = "addCartItem")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "employee user can't have cart"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "stock not found")
	})
	@PostMapping(value = "/item", consumes = APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public Cart addCartItem(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestBody CartItem item,
							@RequestParam(value = "promo", required = false) String promoCode) {
		return cartService.addCartItem(item, promoCode);
	}



	@ApiOperation(value = "delete an item from the cart", nickname = "deleteCartItem")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "employee user can't delete cart item"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "item not found")
	})
	@DeleteMapping(value = "/item", produces=APPLICATION_JSON_VALUE)
	public Cart deleteCartItem(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestParam("item_id") Long itemId,
							   @RequestParam(value = "promo", required = false) String promoCode) {
		return cartService.deleteCartItem(itemId, promoCode);
	}


	@ApiOperation(value = "checkout the cart", nickname = "cartCheckout")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "employee user can't have cart"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "stock not found")
	})
	@PostMapping(value = "/checkout", consumes = APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public Order checkoutCart(@RequestHeader(name = "User-Token", required = false) String userToken,
							  @RequestBody CartCheckoutDTO dto) {
		return cartService.checkoutCart(dto);
	}
	
	
	
	
	@ApiOperation(value = "optimize the cart", nickname = "cartOptimize")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "Not a customer"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "Invalid parameters")
	})
	@PostMapping(value = "/optimize", consumes = APPLICATION_JSON_VALUE, produces=APPLICATION_JSON_VALUE)
	public CartOptimizeResponseDTO optimizeCart(@RequestHeader(name = "User-Token", required = false) String userToken,
								@RequestBody CartCheckoutDTO dto) {
		return cartOptimizeService.optimizeCart(dto);
	}

}
