package com.nasnav.service.cart.optimizers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.dto.query.result.CartItemStock;
import com.nasnav.service.CartService;
import com.nasnav.service.cart.optimizers.parameters.MultipleShopsCartOptimizerParameters;
import com.nasnav.service.cart.optimizers.parameters.MultipleShopsOptimizerConfig;
import com.nasnav.service.model.cart.ShopFulfillingCart;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0008;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.MULTIPLE_SHOPS;
import static com.nasnav.shipping.services.PickupFromMultipleShops.ORG_SHOPS;
import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service(MULTIPLE_SHOPS)
public class MultipleShopsCartOptimizer implements CartOptimizer<MultipleShopsCartOptimizerParameters, MultipleShopsOptimizerConfig>{

    private Logger logger = LogManager.getLogger();

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private CartService cartService;

    @Override
    public Optional<MultipleShopsCartOptimizerParameters> createCartOptimizationParameters(CartCheckoutDTO dto) {
        MultipleShopsCartOptimizerParameters params = new MultipleShopsCartOptimizerParameters();
        List<Long> shopIds = ofNullable(dto)
                .map(CartCheckoutDTO::getAdditionalData)
                .map(data -> data.get(ORG_SHOPS))
                .map(JSONArray::new)
                .map(JSONArray::toList)
                .get()
                .stream()
                .map(String::valueOf)
                .map(Long::parseLong)
                .collect(toList());
        params.setOrgShops(shopIds);
        return Optional.of(params);
    }

    @Override
    public Optional<OptimizedCart> createOptimizedCart(Optional<MultipleShopsCartOptimizerParameters> parameters,
                                                       MultipleShopsOptimizerConfig multipleShopsOptimizerConfig,
                                                       Cart cart) {
        if (!areCartParametersValid(parameters.get())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0008, parameters);
        }

        List<Long> allSelectedShops = parameters.map(MultipleShopsCartOptimizerParameters::getOrgShops)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0008, parameters));

        List<Long> configShops = multipleShopsOptimizerConfig
                .getOrgShops()
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .flatMap(Set::stream)
                .collect(toList());
        List<ShopFulfillingCart> shops = cartService.getSelectedShopsThatCanProvideCartItems(allSelectedShops);

        return cart
                .getItems()
                .stream()
                .map(entry -> createOptimizedCartItem(entry, shops))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(collectingAndThen(
                        toList()
                        , items -> Optional.of(new OptimizedCart(items))));
    }

    private Optional<OptimizedCartItem> createOptimizedCartItem(CartItem item, List<ShopFulfillingCart> shops) {
         return shops
                .stream()
                .map(ShopFulfillingCart::getCartItems)
                .flatMap(List::stream)
                .filter(e -> item.getVariantId().equals(e.getVariantId()))
                .findFirst()
                .map(itemStock -> createOptimizedCartItem(itemStock, item));
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
        boolean priceChanged =
                itemPrice.compareTo(stkPrice) != 0
                        || itemDiscount.compareTo(stkDiscount) != 0;
        boolean itemChanged = !item.getStockId().equals(itemStk.getStockId());
        optimized.setPrice(itemStk.getStockPrice());
        optimized.setStockId(itemStk.getStockId());
        optimized.setDiscount(itemStk.getDiscount());
        optimized.setWeight(weight);
        return new OptimizedCartItem(optimized, priceChanged, itemChanged);
    }

    @Override
    public Class<? extends MultipleShopsCartOptimizerParameters> getCartParametersClass() {
        return MultipleShopsCartOptimizerParameters.class;
    }

    @Override
    public Class<? extends MultipleShopsOptimizerConfig> getConfigurationClass() {
        return MultipleShopsOptimizerConfig.class;
    }


    @Override
    public Boolean isConfigValid(MultipleShopsOptimizerConfig parameters) {
        return noneIsNull(parameters, parameters.getOrgShops()) && !parameters.getOrgShops().isEmpty();
    }

    @Override
    public Boolean areCartParametersValid(MultipleShopsCartOptimizerParameters parameters) {
        return noneIsNull(parameters, parameters.getOrgShops()) && !parameters.getOrgShops().isEmpty();
        /*
        if (anyIsNull(parameters, parameters.getOrgShops()) || parameters.getOrgShops().isEmpty())
            return false;
        for(Map.Entry e : parameters.getOrgShops().entrySet()) {

        }
         */
    }

    @Override
    public String getOptimizerName() {
        return MULTIPLE_SHOPS;
    }
}
