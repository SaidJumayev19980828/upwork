package com.nasnav.controller;

import com.nasnav.dto.AppliedPromotionsResponse;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.CartOptimizeResponseDTO;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.service.CartOptimizationService;
import com.nasnav.service.CartService;
import com.nasnav.service.PromotionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/cart")
public class CartController {

	@Autowired
	private CartService cartService;
	@Autowired
	private PromotionsService promoService;
	@Autowired
	private CartOptimizationService cartOptimizeService;


	@GetMapping(produces=APPLICATION_JSON_VALUE)
	public Cart getCart(@RequestHeader(name = "User-Token", required = false) String userToken,
						@RequestParam(value = "promo", required = false) String promoCode) {
		return cartService.getCart(promoCode);
	}

	@PostMapping(value = "/item", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public Cart addCartItem(@RequestHeader(name = "User-Token", required = false) String userToken,
							@RequestBody CartItem item) {
		return cartService.addCartItem(item);
	}


	@Operation(description =  "add items to the cart", summary = "addCartItems")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 403" ,description = "employee user can't have cart"),
			@ApiResponse(responseCode = " 406" ,description = "stock not found")
	})
	@PostMapping(value = "/items", consumes = APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public Cart addCartItems(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestBody List<CartItem> items) {
		return cartService.addCartItems(items);
	}


	@Operation(description =  "delete an item from the cart", summary = "deleteCartItem")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 403" ,description = "employee user can't delete cart item"),
			@ApiResponse(responseCode = " 406" ,description = "item not found")
	})
	@DeleteMapping(value = "/item", produces=APPLICATION_JSON_VALUE)
	public Cart deleteCartItem(@RequestHeader(name = "User-Token", required = false) String userToken,
							   @RequestParam("item_id") Long itemId) {
		return cartService.deleteCartItem(itemId);
	}

	@PostMapping(value = "/checkout", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
	public Order checkoutCart(@RequestHeader(name = "User-Token", required = false) String userToken,
							  @RequestBody CartCheckoutDTO dto) {
		return cartService.checkoutCart(dto);
	}

	@PostMapping(value = "/optimize", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public CartOptimizeResponseDTO optimizeCart(@RequestHeader(name = "User-Token", required = false) String userToken,
												@RequestBody CartCheckoutDTO dto) {
		return cartOptimizeService.optimizeCart(dto);
	}

	@GetMapping(value = "/promo/discount", produces = APPLICATION_JSON_VALUE)
	public AppliedPromotionsResponse calcPromoDiscount(@RequestHeader(name = "User-Token", required = false) String userToken,
										@RequestParam(value = "promo", required = false) String promoCode) {
		return promoService.calcPromoDiscountForCart(promoCode);
	}
}
