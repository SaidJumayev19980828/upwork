package com.nasnav.controller;

import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.Wishlist;
import com.nasnav.dto.response.navbox.WishlistItem;
import com.nasnav.dto.response.navbox.WishlistItemQuantity;
import com.nasnav.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/wishlist")
@Tag(name = "Methods for accessing wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;


    @Operation(description =  "add an item to the wishlist", summary = "addWishlistItem")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK"),
            @ApiResponse(responseCode = " 403" ,description = "employee user can't have wishlist"),
            @ApiResponse(responseCode = " 406" ,description = "stock not found")
    })
    @PostMapping(value = "/item", consumes = APPLICATION_JSON_VALUE, produces= MediaType.APPLICATION_JSON_VALUE)
    public Wishlist addWishlistItem(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestBody WishlistItem item) {
        return wishlistService.addWishlistItem(item);
    }




    @Operation(description =  "delete an item from the wishlist", summary = "deleteWishlistItem")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK"),
            @ApiResponse(responseCode = " 403" ,description = "employee user can't delete wishlist item"),
            @ApiResponse(responseCode = " 406" ,description = "item not found")
    })
    @DeleteMapping(value = "/item", produces=APPLICATION_JSON_VALUE)
    public Wishlist deleteWishlistItem(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestParam("item_id") Long itemId) {
        return wishlistService.deleteWishlistItem(itemId);
    }



    @Operation(description =  "get user wishlist", summary = "getWishlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK"),
            @ApiResponse(responseCode = " 406" ,description = "invalid search parameter")
    })
    @GetMapping(produces=APPLICATION_JSON_VALUE)
    public Wishlist getWishlist(@RequestHeader(name = "User-Token", required = false) String userToken) {
        return wishlistService.getWishlist();
    }




    @Operation(description =  "move a wishlist item to the cart", summary = "moveIntoCart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK"),
            @ApiResponse(responseCode = " 403" ,description = "employee user can't access a wishlist"),
            @ApiResponse(responseCode = " 406" ,description = "stock not found")
    })
    @PostMapping(value = "/item/into_cart", consumes = APPLICATION_JSON_VALUE, produces= MediaType.APPLICATION_JSON_VALUE)
    public Cart moveWishlistItemIntoCart(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestBody WishlistItemQuantity items) {
        return wishlistService.moveWishlistItemsToCart(items);
    }
}
