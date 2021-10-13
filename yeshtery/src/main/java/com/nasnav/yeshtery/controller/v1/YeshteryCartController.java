package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.*;
import com.nasnav.service.CartService;
import com.nasnav.yeshtery.YeshteryConstants;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(YeshteryCartController.API_PATH)
@Tag(name = "Yeshtery Cart Controller")
@CrossOrigin("*")
@EnableJpaRepositories
public class YeshteryCartController {

    static final String API_PATH = YeshteryConstants.API_PATH +"/cart";

    @Autowired
    private CartService cartService;

    @GetMapping(produces= APPLICATION_JSON_VALUE)
    public Cart getYeshteryCart(@RequestHeader(name = "User-Token", required = false) String token,
                                @RequestParam(value = "promo", required = false, defaultValue = "") String promoCode) {
        return cartService.getCart(promoCode);
    }

    @PostMapping(value = "/item", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
    public Cart addCartItem(@RequestHeader(name = "User-Token", required = false) String token,
                            @RequestBody CartItem item,
                            @RequestParam(value = "promo", required = false, defaultValue = "") String promoCode) {
        return cartService.addCartItem(item, promoCode);
    }

    @DeleteMapping(value = "/item", produces=APPLICATION_JSON_VALUE)
    public Cart deleteCartItem(@RequestHeader(name = "User-Token", required = false) String token,
                               @RequestParam("item_id") Long itemId,
                               @RequestParam(value = "promo", required = false, defaultValue = "") String promoCode) {
        return cartService.deleteYeshteryCartItem(itemId, promoCode);
    }

    @PostMapping(value = "/checkout", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
    public Order checkoutCart(@RequestHeader(name = "User-Token", required = false) String token,
                              @RequestBody CartCheckoutDTO dto) {
        return cartService.checkoutYeshteryCart(dto);
    }
}
