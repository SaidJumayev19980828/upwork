package com.nasnav.controller;

import com.nasnav.dto.AppliedPromotionsResponse;
import com.nasnav.dto.EstimateTokensUsdResponse;
import com.nasnav.dto.TokenValueRequest;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.TokenPaymentResponse;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.CartOptimizeResponseDTO;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.CartCheckoutService;
import com.nasnav.service.CartOptimizationService;
import com.nasnav.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
	private final CartCheckoutService cartCheckoutService;

	@Autowired
	private CartService cartService;
	@Autowired
	private CartOptimizationService cartOptimizeService;


	@Deprecated
	@GetMapping(produces=APPLICATION_JSON_VALUE)
	public Cart getCart(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
						@RequestParam(value = "promo", required = false, defaultValue = "") String promoCode,
						@RequestParam(required = false) Set<Long> points) {
		return cartService.getCart(promoCode, points, false);
	}


	@GetMapping(value = "/v2",produces=APPLICATION_JSON_VALUE)
	public Cart getCartVersionTwo(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
						@RequestParam(value = "promo", required = false, defaultValue = "") String promoCode,
						@RequestParam(required = false) BigDecimal points) {
		return cartService.getCart(promoCode, points, false);
	}
	@GetMapping(value = "/{userId}",produces=APPLICATION_JSON_VALUE)
	public Cart getCartWithUserId(@PathVariable Long userId) {
		return cartService.getUserCart(userId,false);
	}

	@PostMapping(value = "/item", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public Cart addCartItem(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
							@RequestBody CartItem item,
							@RequestParam(value = "promo", required = false, defaultValue = "") String promoCode,
							@RequestParam(required = false) Set<Long> points) {
		return cartService.addCartItem(item, promoCode, points, false);
	}

	@PostMapping(value = "/items", consumes = APPLICATION_JSON_VALUE, produces=APPLICATION_JSON_VALUE)
	public Cart addCartItems(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
							 @RequestBody List<CartItem> items,
							 @RequestParam(value = "promo", required = false, defaultValue = "") String promoCode,
							 @RequestParam(required = false) Set<Long> points) {
		return cartService.addNasnavCartItems(items, promoCode, points, false);
	}


	@DeleteMapping(value = "/item", produces=APPLICATION_JSON_VALUE)
	public Cart deleteCartItem(@RequestHeader(name = "User-Token", required = false) String token,
							   @RequestParam("item_id") Long itemId,
							   @RequestParam(value = "promo", required = false, defaultValue = "") String promoCode,
							   @RequestParam(required = false) Set<Long> points,
							   @RequestParam(value = "user_id", required = false) Long userId

	) {
		return cartService.deleteCartItem(itemId, promoCode, points, false, userId);
	}


	@PostMapping(value = "/checkout", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
	public Order checkoutCart(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @RequestBody CartCheckoutDTO dto) {
		return cartCheckoutService.checkoutCart(dto);
	}

	@PostMapping(value = "/store-checkout/initiate", produces= APPLICATION_JSON_VALUE)
	public void initiateCheckout(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
								 @RequestParam("user_id") Long userId) {
		 cartCheckoutService.initiateCheckout(userId);   ///here i need modify
	}

	@PostMapping(value = "/store-checkout/complete", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
	public Order checkoutComplete(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
								  @RequestBody CartCheckoutDTO dto) throws BusinessException {
		return cartCheckoutService.completeCheckout(dto);  ///here also
	}

	@PostMapping(value = "/optimize", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public CartOptimizeResponseDTO optimizeCart(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @RequestBody CartCheckoutDTO dto) {
		return cartOptimizeService.validateAndOptimizeCart(dto, false);
	}

	@GetMapping(value = "/promo/discount", produces = APPLICATION_JSON_VALUE)
	public AppliedPromotionsResponse calcPromoDiscount(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
													   @RequestParam(value = "promo", required = false) String promoCode) {
		return cartService.getCartPromotions(promoCode);
	}

	@PostMapping("/estimate-tokens-to-usdc")
	public EstimateTokensUsdResponse estimateTokensToUsdc(@RequestBody TokenValueRequest request) {
		return cartService.estimateTokensToUsd(request);   /// endpoint for converting usd to MVR    move this to Yeshtery Cart
	}
	@PostMapping("/token-paymet/{brandId}")
	public TokenPaymentResponse tokenPayment(
			@PathVariable Long brandId,
			@RequestBody TokenValueRequest request) {
		 return cartService.tokenPayment(brandId,request);
	}

}
