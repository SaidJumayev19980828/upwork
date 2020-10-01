package com.nasnav.controller;

import com.nasnav.dto.response.navbox.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.WishlistService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/wishlist")
@Api(description = "Methods for accessing wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;


    @ApiOperation(value = "add an item to the wishlist", nickname = "addWishlistItem")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "employee user can't have wishlist"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "stock not found")
    })
    @PostMapping(value = "/item", consumes = APPLICATION_JSON_VALUE, produces= MediaType.APPLICATION_JSON_VALUE)
    public Wishlist addWishlistItem(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestBody WishlistItem item) {
        return wishlistService.addWishlistItem(item);
    }




    @ApiOperation(value = "delete an item from the wishlist", nickname = "deleteWishlistItem")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "employee user can't delete wishlist item"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "item not found")
    })
    @DeleteMapping(value = "/item", produces=APPLICATION_JSON_VALUE)
    public Wishlist deleteWishlistItem(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestParam("item_id") Long itemId) {
        return wishlistService.deleteWishlistItem(itemId);
    }



    @ApiOperation(value = "get user wishlist", nickname = "getWishlist")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "invalid search parameter")
    })
    @GetMapping(produces=APPLICATION_JSON_VALUE)
    public Wishlist getWishlist(@RequestHeader(name = "User-Token", required = false) String userToken) {
        return wishlistService.getWishlist();
    }




    @ApiOperation(value = "move a wishlist item to the cart", nickname = "moveIntoCart")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "employee user can't access a wishlist"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "stock not found")
    })
    @PostMapping(value = "/item/into_cart", consumes = APPLICATION_JSON_VALUE, produces= MediaType.APPLICATION_JSON_VALUE)
    public Cart moveWishlistItemIntoCart(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestBody WishlistItemQuantity items) {
        return wishlistService.moveWishlistItemsToCart(items);
    }
}
