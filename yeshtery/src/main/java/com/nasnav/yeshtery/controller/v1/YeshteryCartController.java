package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.CartCheckoutService;
import com.nasnav.service.CartOptimizationService;
import com.nasnav.service.CartService;

import lombok.RequiredArgsConstructor;

import com.nasnav.commons.YeshteryConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(YeshteryCartController.API_PATH)
@CrossOrigin("*")
@EnableJpaRepositories
@RequiredArgsConstructor
public class YeshteryCartController {
	private final CartCheckoutService cartCheckoutService;

    static final String API_PATH = YeshteryConstants.API_PATH +"/cart";

    @Autowired
    private CartService cartService;
    @Autowired
    private CartOptimizationService cartOptimizationService;

    @Deprecated
    @GetMapping(produces= APPLICATION_JSON_VALUE)
    public Cart getYeshteryCart(@RequestHeader(name = "User-Token", required = false) String token,
                                @RequestParam(value = "promo", required = false, defaultValue = "") String promoCode,
                                @RequestParam(required = false) Set<Long> points) {
        return cartService.getCart(promoCode, points, true);
    }

    @GetMapping(value = "/v2",produces= APPLICATION_JSON_VALUE)
    public Cart getYeshteryCartVersionTwo(@RequestHeader(name = "User-Token", required = false) String token,
                                @RequestParam(value = "promo", required = false, defaultValue = "") String promoCode,
                                @RequestParam(required = false) BigDecimal points) {
        return cartService.getCart(promoCode, points, true);
    }

    @GetMapping(value = "/{userId}",produces= APPLICATION_JSON_VALUE)
    public Cart getYeshteryCartWithUserId(@PathVariable Long userId) {
        return cartService.getUserCart(userId, true);
    }

    @PostMapping(value = "/item", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
    public Cart addCartItem(@RequestHeader(name = "User-Token", required = false) String token,
                            @RequestBody CartItem item,
                            @RequestParam(value = "promo", required = false, defaultValue = "") String promoCode,
                            @RequestParam(required = false) Set<Long> points) {
        return cartService.addCartItem(item, promoCode, points, true);
    }

    @PostMapping(value = "/items", consumes = APPLICATION_JSON_VALUE, produces=APPLICATION_JSON_VALUE)
    public Cart addCartItems(@RequestHeader(name = "User-Token", required = false) String userToken,
                             @RequestBody List<CartItem> items,
                             @RequestParam(value = "promo", required = false, defaultValue = "") String promoCode,
                             @RequestParam(required = false) Set<Long> points) {
        return cartService.addYeshteryCartItems(items, promoCode, points, true);
    }

    @DeleteMapping(value = "/item", produces=APPLICATION_JSON_VALUE)
    public Cart deleteCartItem(@RequestHeader(name = "User-Token", required = false) String token,
                               @RequestParam("item_id") Long itemId,
                               @RequestParam(value = "promo", required = false, defaultValue = "") String promoCode,
                               @RequestParam(required = false) Set<Long> points,
                               @RequestParam(value = "user_id", required = false) Long userId

    ) {
        return cartService.deleteYeshteryCartItem(itemId, promoCode, points, true , userId);
    }

    @PostMapping(value = "/checkout", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
    public Order checkoutCart(@RequestHeader(name = "User-Token", required = false) String token,
                              @RequestBody CartCheckoutDTO dto) {
        return cartCheckoutService.checkoutYeshteryCart(dto);
    }

    @PostMapping(value = "/optimize", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CartOptimizeResponseDTO optimizeCart(@RequestHeader(name = "User-Token", required = false) String userToken,
                                                @RequestBody CartCheckoutDTO dto) {
        return cartOptimizationService.validateAndOptimizeCart(dto, true);
    }

    @PostMapping(value = "/store-checkout/initiate/{userId}", produces= APPLICATION_JSON_VALUE)
    public void initiateCheckout(@PathVariable Long userId) {
        cartCheckoutService.initiateCheckout(userId);
    }

    @PostMapping(value = "/store-checkout/complete", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
    public Order checkoutComplete(@RequestBody CartCheckoutDTO dto) throws BusinessException {
        return cartCheckoutService.completeYeshteryCheckout(dto);  //// this i need modify
    }
}

//1. store-checkout/complete must accept MVR coins   using blockchain method payment with MVR
//2.change user balance according this