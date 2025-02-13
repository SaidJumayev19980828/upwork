package com.nasnav.service;

import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.Wishlist;
import com.nasnav.dto.response.navbox.WishlistItem;
import com.nasnav.dto.response.navbox.WishlistItemQuantity;

public interface WishlistService {

    Wishlist addWishlistItem(WishlistItem item);

    Wishlist deleteWishlistItem(Long itemId);

    Wishlist getWishlist();

    Wishlist getWishlist(Long userId,Boolean isYeshtery);

    Cart moveWishlistItemsToCart(WishlistItemQuantity items);

    void sendRestockedWishlistEmails();
}
