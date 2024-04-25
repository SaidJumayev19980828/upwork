package com.nasnav.service.cart.optimizers;

import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.persistence.dto.query.result.CartItemStock;
import com.nasnav.service.CartService;
import com.nasnav.service.cart.optimizers.parameters.EmptyParams;
import com.nasnav.service.cart.optimizers.parameters.PickupParameters;
import com.nasnav.service.model.cart.ShopFulfillingCart;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.SHOP_PICKUP;
import static com.nasnav.shipping.services.PickupFromShop.SHOP_ID;
import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;

@Service(SHOP_PICKUP)
public class PickupCartOptimizer implements CartOptimizer<PickupParameters, EmptyParams> {

    private final Logger logger = LogManager.getLogger();
    @Autowired
    private CartService cartService;

    @Override
    public Optional<OptimizedCart> createOptimizedCart(Optional<PickupParameters> parameters, EmptyParams config, Cart cart ) {

        Long givenShopId = parameters.map(PickupParameters::getShopId).get();
        List<ShopFulfillingCart> shops;
        if(Objects.nonNull(cart.getCustomerId()) && cart.getCustomerId() > 0) {
            shops = cartService.getSelectedShopsThatCanProvideCartItems(cart.getCustomerId(), List.of(givenShopId));
        } else {
           shops = getShopFulfillingCart(givenShopId);
        }

        return cart
                .getItems()
                .stream()
                .map(item -> createOptimizedCartItem(item, shops))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(collectingAndThen(
                        toList()
                        , items -> Optional.of(new OptimizedCart(items))));
    }

    private List<ShopFulfillingCart> getShopFulfillingCart(Long givenShopId) {
        return cartService.getSelectedShopsThatCanProvideCartItems(List.of(givenShopId));
    }

    private Optional<OptimizedCartItem> createOptimizedCartItem(CartItem item, List<ShopFulfillingCart> shopsOrderedByPriority) {
        return getCartItemStockShop(item, shopsOrderedByPriority)
                .map(itemStk -> createOptimizedCartItem(itemStk, item));
    }

    private OptimizedCartItem createOptimizedCartItem(CartItemStock itemStk, CartItem item) {
        CartItem optimized = new CartItem();
        try {
            BeanUtils.copyProperties(optimized, item);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e,e);
            return new OptimizedCartItem(item, false, false);
        }
        BigDecimal itemPrice = ofNullable(item.getPrice()).orElse(ZERO);
        BigDecimal itemDiscount = ofNullable(item.getDiscount()).orElse(ZERO);
        BigDecimal stkPrice = ofNullable(itemStk.getStockPrice()).orElse(ZERO);
        BigDecimal stkDiscount = ofNullable(itemStk.getDiscount()).orElse(ZERO);
        BigDecimal weight = ofNullable(item.getWeight()).orElse(ZERO);
        boolean priceChanged = itemPrice.compareTo(stkPrice) != 0 || itemDiscount.compareTo(stkDiscount) != 0;
        boolean itemChanged = !item.getStockId().equals(itemStk.getStockId());
        optimized.setPrice(itemStk.getStockPrice());
        optimized.setStockId(itemStk.getStockId());
        optimized.setDiscount(itemStk.getDiscount());
        optimized.setWeight(weight);
        return new OptimizedCartItem(optimized, priceChanged, itemChanged);
    }

    private Optional<CartItemStock> getCartItemStockShop(CartItem item, List<ShopFulfillingCart> shops) {
        return shops
                .stream()
                .map(shop -> getCartItemStockInShop(shop, item))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
    private Optional<CartItemStock> getCartItemStockInShop(ShopFulfillingCart shop, CartItem item) {
        return shop
                .getCartItems()
                .stream()
                .filter(itemInShop -> Objects.equals(itemInShop.getVariantId(), item.getVariantId()))
                .findFirst();
    }

    @Override
    public Class<? extends PickupParameters> getCartParametersClass() {
        return PickupParameters.class;
    }

    @Override
    public Optional<PickupParameters> createCartOptimizationParameters(CartCheckoutDTO checkoutDto) {
        PickupParameters params = new PickupParameters();

        ofNullable(checkoutDto)
                .map(CartCheckoutDTO::getAdditionalData)
                .map(data -> data.get(SHOP_ID))
                .flatMap(EntityUtils::parseLongSafely)
                .ifPresent(params::setShopId);

        return Optional.of(params);
    }

    @Override
    public Boolean areCartParametersValid(PickupParameters parameters) {
        return noneIsNull(parameters, parameters.getShopId());
    }

    @Override
    public String getOptimizerName() {
        return SHOP_PICKUP;
    }

    @Override
    public Class<? extends EmptyParams> getConfigurationClass() {
        return EmptyParams.class;
    }

    @Override
    public Boolean isConfigValid(EmptyParams parameters) {
        return true;
    }
}



