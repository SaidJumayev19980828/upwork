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

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
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
	public Cart getCart(@RequestHeader(TOKEN_HEADER) String userToken,
						@RequestParam(value = "promo", required = false, defaultValue = "") String promoCode) {
		return cartService.getCart(promoCode);
	}

	@PostMapping(value = "/item", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public Cart addCartItem(@RequestHeader(TOKEN_HEADER) String userToken,
							@RequestBody CartItem item,
							@RequestParam(value = "promo", required = false, defaultValue = "") String promoCode) {
		return cartService.addCartItem(item, promoCode);
	}

	@PostMapping(value = "/items", consumes = APPLICATION_JSON_VALUE, produces=APPLICATION_JSON_VALUE)
	public Cart addCartItems(@RequestHeader(TOKEN_HEADER) String userToken,
							 @RequestBody List<CartItem> items,
							 @RequestParam(value = "promo", required = false, defaultValue = "") String promoCode) {
		return cartService.addNasnavCartItems(items, promoCode);
	}

	@DeleteMapping(value = "/item", produces=APPLICATION_JSON_VALUE)
	public Cart deleteCartItem(@RequestHeader(TOKEN_HEADER) String userToken,
							   @RequestParam("item_id") Long itemId,
							   @RequestParam(value = "promo", required = false, defaultValue = "") String promoCode) {
		return cartService.deleteCartItem(itemId, promoCode);
	}

	@PostMapping(value = "/checkout", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
	public Order checkoutCart(@RequestHeader(TOKEN_HEADER) String userToken, @RequestBody CartCheckoutDTO dto) {
		return cartService.checkoutCart(dto);
	}

	@PostMapping(value = "/optimize", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public CartOptimizeResponseDTO optimizeCart(@RequestHeader(TOKEN_HEADER) String userToken, @RequestBody CartCheckoutDTO dto) {
		return cartOptimizeService.validateAndOptimizeCart(dto);
	}

	@GetMapping(value = "/promo/discount", produces = APPLICATION_JSON_VALUE)
	public AppliedPromotionsResponse calcPromoDiscount(@RequestHeader(TOKEN_HEADER) String userToken,
													   @RequestParam(value = "promo", required = false) String promoCode) {
		return promoService.calcPromoDiscountForCart(promoCode);
	}
}
