package com.nasnav.service;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.model.cart.ShopFulfillingCart;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    Cart getCart();
    Cart getUserCart(Long userId);
    Cart addCartItem(CartItem item);
    Cart deleteCartItem(Long itemId);
    Order checkoutCart(CartCheckoutDTO dto);
    BigDecimal calculateCartTotal();
    List<ShopFulfillingCart> getShopsThatCanProvideCartItems();
    List<ShopFulfillingCart> getShopsThatCanProvideWholeCart();
}
