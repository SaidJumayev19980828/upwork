package com.nasnav.controller;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.CartOptimizeResponseDTO;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.CartOptimizationService;
import com.nasnav.service.CartService;
import com.nasnav.service.PromotionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/cart")
@Tag(name = "Methods for accessing cart")
public class CartController {
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private PromotionsService promoService;
	
	@Autowired
	private CartOptimizationService cartOptimizeService;

	@Operation(description =  "get user cart", summary = "getCart")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 406" ,description = "invalid search parameter")
	})
	@GetMapping(produces=APPLICATION_JSON_VALUE)
	public Cart getCart(@RequestHeader(name = "User-Token", required = false) String userToken) throws BusinessException {
		return cartService.getCart();
	}



	@Operation(description =  "add an item to the cart", summary = "addCartItem")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 403" ,description = "employee user can't have cart"),
			@ApiResponse(responseCode = " 406" ,description = "stock not found")
	})
	@PostMapping(value = "/item", consumes = APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public Cart addCartItem(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestBody CartItem item) {
		return cartService.addCartItem(item);
	}



	@Operation(description =  "delete an item from the cart", summary = "deleteCartItem")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 403" ,description = "employee user can't delete cart item"),
			@ApiResponse(responseCode = " 406" ,description = "item not found")
	})
	@DeleteMapping(value = "/item", produces=APPLICATION_JSON_VALUE)
	public Cart deleteCartItem(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestParam("item_id") Long itemId) {
		return cartService.deleteCartItem(itemId);
	}


	@Operation(description =  "checkout the cart", summary = "cartCheckout")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 403" ,description = "employee user can't have cart"),
			@ApiResponse(responseCode = " 406" ,description = "stock not found")
	})
	@PostMapping(value = "/checkout", consumes = APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public Order checkoutCart(@RequestHeader(name = "User-Token", required = false) String userToken,
							  @RequestBody CartCheckoutDTO dto) {
		return cartService.checkoutCart(dto);
	}
	
	
	
	
	@Operation(description =  "optimize the cart", summary = "cartOptimize")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 403" ,description = "Not a customer"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid parameters")
	})
	@PostMapping(value = "/optimize", consumes = APPLICATION_JSON_VALUE, produces=APPLICATION_JSON_VALUE)
	public CartOptimizeResponseDTO optimizeCart(@RequestHeader(name = "User-Token", required = false) String userToken,
								@RequestBody CartCheckoutDTO dto) {
		return cartOptimizeService.optimizeCart(dto);
	}
	
	
	
	
	@Operation(description =  "calculate promo for the cart", summary = "cartPromoCalc")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 403" ,description = "Not a customer"),
			@ApiResponse(responseCode = " 406" ,description = "Invalid parameters")
	})
	@GetMapping(value = "/promo/discount", produces=APPLICATION_JSON_VALUE)
	public BigDecimal calcPromoDiscount(@RequestHeader(name = "User-Token", required = false) String userToken,
							  @RequestParam("promo") String promoCode) {
		return promoService.calcPromoDiscountForCart(promoCode);
	}
}
