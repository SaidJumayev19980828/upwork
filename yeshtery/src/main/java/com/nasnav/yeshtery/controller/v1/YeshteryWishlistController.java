package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.Wishlist;
import com.nasnav.dto.response.navbox.WishlistItem;
import com.nasnav.dto.response.navbox.WishlistItemQuantity;
import com.nasnav.service.WishlistService;
import com.nasnav.commons.YeshteryConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(YeshteryWishlistController.API_PATH)
@CrossOrigin("*")
@EnableJpaRepositories
public class YeshteryWishlistController {

    static final String API_PATH = YeshteryConstants.API_PATH +"/wishlist";

    @Autowired
    private WishlistService wishlistService;

    @GetMapping(produces= APPLICATION_JSON_VALUE)
    public Wishlist getWishlist(@RequestHeader(name = "User-Token", required = false) String token) {
        return wishlistService.getWishlist();
    }

    @PostMapping(value = "/item", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
    public Wishlist addWishlistItem(@RequestHeader(name = "User-Token", required = false) String token,
                                    @RequestBody WishlistItem item) {
        return wishlistService.addWishlistItem(item);
    }

    @DeleteMapping(value = "/item", produces=APPLICATION_JSON_VALUE)
    public Wishlist deleteWishlistItem(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestParam("item_id") Long itemId) {
        return wishlistService.deleteWishlistItem(itemId);
    }

    @PostMapping(value = "/item/into_cart", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
    public Cart moveWishlistItemIntoCart(@RequestHeader(name = "User-Token", required = false) String token,
                                         @RequestBody WishlistItemQuantity items) {
        return wishlistService.moveWishlistItemsToCart(items);
    }
}
