package com.nasnav.service;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.persistence.dto.query.result.CartItemData;
import com.nasnav.service.model.cart.ShopFulfillingCart;

import java.util.List;

public interface CartService {
    Cart getCart();
    Cart getCart(String promoCode);
    Cart getUserCart(Long userId, String promoCode);

    void addCartPromoData(Cart cart, String promoCode);

    Cart addCartItem(CartItem item);
    Cart addCartItem(CartItem item, String promoCode);
    Cart deleteCartItem(Long itemId, String promoCode);
    Order checkoutCart(CartCheckoutDTO dto);
    List<ShopFulfillingCart> getShopsThatCanProvideCartItems();
    List<ShopFulfillingCart> getShopsThatCanProvideWholeCart();
    List<CartItem> toCartItemsDto(List<CartItemData> cartItems);
}
