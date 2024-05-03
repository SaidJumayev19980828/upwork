package com.nasnav.service;

import com.nasnav.dto.AppliedPromotionsResponse;
import com.nasnav.dto.EstimateTokensUsdResponse;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.dto.TokenValueRequest;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.mail.AbandonedCartsMail;
import com.nasnav.dto.response.TokenPaymentResponse;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.persistence.CartItemEntity;
import com.nasnav.service.model.cart.ShopFulfillingCart;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface CartService {
    AppliedPromotionsResponse getCartPromotions(String promoCode);
    Cart getCart(String promoCode, Set<Long>points, boolean yeshteryCart);
    Cart getCart(String promoCode, BigDecimal points, boolean yeshteryCart);

    Cart getCart(CartCheckoutDTO dto , String promoCode, Set<Long>points, boolean yeshteryCart);

    Cart getCart(CartCheckoutDTO dto , String promoCode, BigDecimal points, boolean yeshteryCart);

    Cart getUserCart(Long userId, String promoCode, BigDecimal points, boolean yeshteryCart);
    Cart getUserCart(Long userId);
    Cart getUserCart(Long userId,Boolean isYeshtery);
    Cart getUserCart(Long userId, String promoCode, Set<Long>points, boolean yeshteryCart);
    Cart addCartItem(CartItem item, String promoCode, Set<Long>points, boolean yeshteryCart);
    Cart addNasnavCartItems(List<CartItem> items, String promoCode, Set<Long>points, boolean yeshteryCart);
    Cart addYeshteryCartItems(List<CartItem> items, String promoCode, Set<Long>points, boolean yeshteryCart);
    Cart deleteCartItem(Long itemId, String promoCode, Set<Long>points, boolean yeshteryCart,Long userId);
    Cart deleteYeshteryCartItem(Long itemId, String promoCode, Set<Long>points, boolean yeshteryCart , Long userId );
    BigDecimal calculateCartTotal(Cart cart);
    List<ShopFulfillingCart> getSelectedShopsThatCanProvideCartItems(List<Long> shops);

    List<ShopFulfillingCart> getSelectedShopsThatCanProvideCartItems(Long userId, List<Long> shops);

    List<ShopFulfillingCart> getShopsThatCanProvideCartItems();

    List<ShopFulfillingCart> getShopsThatCanProvideWholeCart();
    List<CartItem> toCartItemsDto(List<CartItemEntity> cartItems);
    void sendAbandonedCartEmails(AbandonedCartsMail dto);
    List<ShopRepresentationObject> getShopsThatCanProvideEachItem();
    void moveOutOfStockCartItemsToWishlist();
    void moveCartItemsToWishlist(List<CartItemEntity> movedItems);

    EstimateTokensUsdResponse estimateTokensToUsd (TokenValueRequest request);
    TokenPaymentResponse tokenPayment(Long brandId, TokenValueRequest request);
}
