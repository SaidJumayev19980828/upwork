package com.nasnav.service;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.mail.AbandonedCartsMail;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.persistence.CartItemEntity;
import com.nasnav.persistence.dto.query.result.CartItemData;
import com.nasnav.service.model.cart.ShopFulfillingCart;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    Cart getCart();
    Cart getUserCart(Long userId);
    Cart addCartItem(CartItem item);
    Cart addCartItems(List<CartItem> item);
    Cart deleteCartItem(Long itemId);
    Order checkoutCart(CartCheckoutDTO dto);
    BigDecimal calculateCartTotal();
    List<ShopFulfillingCart> getShopsThatCanProvideCartItems();
    List<ShopFulfillingCart> getShopsThatCanProvideWholeCart();
    List<CartItem> toCartItemsDto(List<CartItemEntity> cartItems);
    void sendAbandonedCartEmails(AbandonedCartsMail dto);
}
