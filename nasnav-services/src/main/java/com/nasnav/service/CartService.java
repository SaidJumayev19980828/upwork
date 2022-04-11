package com.nasnav.service;

import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.mail.AbandonedCartsMail;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.persistence.CartItemEntity;
import com.nasnav.service.model.cart.ShopFulfillingCart;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    Cart getCart(String promoCode);
    Cart getUserCart(Long userId);
    Cart getUserCart(Long userId, String promoCode);
    Cart addCartItem(CartItem item, String promoCode);
    Cart addNasnavCartItems(List<CartItem> items, String promoCode);
    Cart addYeshteryCartItems(List<CartItem> items, String promoCode);
    Cart deleteCartItem(Long itemId, String promoCode);
    Cart deleteYeshteryCartItem(Long itemId, String promoCode);
    Order checkoutCart(CartCheckoutDTO dto);
    Order checkoutYeshteryCart(CartCheckoutDTO dto);
    BigDecimal calculateCartTotal(Cart cart);
    List<ShopFulfillingCart> getSelectedShopsThatCanProvideCartItems(List<Long> shops);
    List<ShopFulfillingCart> getShopsThatCanProvideCartItems();
    List<ShopFulfillingCart> getShopsThatCanProvideWholeCart();
    List<CartItem> toCartItemsDto(List<CartItemEntity> cartItems);
    void sendAbandonedCartEmails(AbandonedCartsMail dto);
    List<ShopRepresentationObject> getShopsThatCanProvideEachItem();
}
