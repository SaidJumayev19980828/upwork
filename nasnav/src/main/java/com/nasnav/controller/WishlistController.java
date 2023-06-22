package com.nasnav.controller;

import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.Wishlist;
import com.nasnav.dto.response.navbox.WishlistItem;
import com.nasnav.dto.response.navbox.WishlistItemQuantity;
import com.nasnav.service.WishlistService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @PostMapping(value = "/item", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
    public Wishlist addWishlistItem(@RequestHeader(name = "User-Token", required = false) String userToken,
                                    @RequestBody WishlistItem item) {
        return wishlistService.addWishlistItem(item);
    }

    @DeleteMapping(value = "/item", produces=APPLICATION_JSON_VALUE)
    public Wishlist deleteWishlistItem(@RequestHeader(name = "User-Token", required = false) String userToken,
                                       @RequestParam("item_id") Long itemId) {
        return wishlistService.deleteWishlistItem(itemId);
    }

    @GetMapping(produces=APPLICATION_JSON_VALUE)
    public Wishlist getWishlist(@RequestHeader(name = "User-Token", required = false) String userToken) {
        return wishlistService.getWishlist();
    }
    @GetMapping( value = "/{userId}",produces = APPLICATION_JSON_VALUE)
    public Wishlist getWishlistWithUserId(@PathVariable Long userId) {
        return wishlistService.getWishlist(userId, false);
    }

    @PostMapping(value = "/item/into_cart", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
    public Cart moveWishlistItemIntoCart(@RequestHeader(name = "User-Token", required = false) String userToken,
                                         @RequestBody WishlistItemQuantity items) {
        return wishlistService.moveWishlistItemsToCart(items);
    }
}
