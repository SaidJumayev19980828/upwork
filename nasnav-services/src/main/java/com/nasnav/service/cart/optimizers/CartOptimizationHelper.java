package com.nasnav.service.cart.optimizers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.OrganizationCartOptimizationRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationCartOptimizationEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.service.SecurityService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.nasnav.commons.utils.EntityUtils.firstExistingValueOf;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.WAREHOUSE;
import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class CartOptimizationHelper {
    private Logger logger = LogManager.getLogger();

    @Autowired
    private OrganizationCartOptimizationRepository orgCartOptimizationRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private StockRepository stockRepo;


    public <T, Config> Config getOptimizerConfig(String configJson, CartOptimizer<T,Config> optimizer) {
        Config config =  parseConfigJson(configJson, optimizer.getConfigurationClass());
        validateConfig(optimizer, config);
        return config;
    }



    public Map<Long, StocksEntity> getCartItemsStockInShop(Cart cart, Long shopId) {
        List<Long> variantIds =
                cart
                .getItems()
                .stream()
                .map(CartItem::getVariantId)
                .collect(toList());
        return stockRepo
                .findByProductVariantsEntity_IdInAndShopsEntity_Id(variantIds, shopId)
                .stream()
                .collect(
                        toMap(stock -> stock.getProductVariantsEntity().getId()
                                , stock -> stock));
    }



    public Optional<OptimizedCart> createOptimizedCart(Cart cart, Map<Long, StocksEntity> stocks) {
        return cart
                .getItems()
                .stream()
                .map(item -> createOptimizedCartItem(item, stocks))
                .collect(
                        collectingAndThen(toList()
                                , items -> Optional.of(new OptimizedCart(items))));
    }



    public OptimizedCartItem createOptimizedCartItem(CartItem item, Map<Long,StocksEntity> stocks) {
        CartItem optimized = new CartItem();
        try {
            BeanUtils.copyProperties(optimized, item);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e,e);
            return new OptimizedCartItem(item, false, false);
        }
        StocksEntity stock =
                ofNullable(item.getVariantId())
                        .map(stocks::get)
                        .orElseThrow(
                                () -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0011, item.getId(), item.getStockId()));

        reduceItemQuantityIfNeeded(stock, item, optimized);

        boolean priceChanged = isPriceChanged(item, stock);
        boolean itemChanged = isItemChanged(item, stock);
        optimized.setPrice(stock.getPrice());
        optimized.setStockId(stock.getId());
        optimized.setDiscount(stock.getDiscount());
        return new OptimizedCartItem(optimized, priceChanged, itemChanged);
    }

    private void reduceItemQuantityIfNeeded(StocksEntity stock, CartItem item, CartItem optimized) {
        if (stock.getQuantity().compareTo(item.getQuantity()) < 0)
            optimized.setQuantity(stock.getQuantity());
    }

    private boolean canFulfillRequiredQuantitiy(StocksEntity stock, CartItem item) {
        return stock.getQuantity() >= item.getQuantity();
    }

    private boolean isItemChanged(CartItem item, StocksEntity stock) {
        return !item.getStockId().equals(stock.getId()) || stock.getQuantity().compareTo(item.getQuantity()) < 0;
    }

    private boolean isPriceChanged(CartItem item, StocksEntity stock) {
        BigDecimal itemPrice = ofNullable(item.getPrice()).orElse(ZERO);
        BigDecimal itemDiscount = ofNullable(item.getDiscount()).orElse(ZERO);
        BigDecimal stkPrice = ofNullable(stock.getPrice()).orElse(ZERO);
        BigDecimal stkDiscount = ofNullable(stock.getDiscount()).orElse(ZERO);

        return itemPrice.compareTo(stkPrice) != 0
                || itemDiscount.compareTo(stkDiscount) != 0
                || stock.getQuantity().compareTo(item.getQuantity()) < 0;
    }



    private <T, P> void validateConfig(CartOptimizer<T, P> optimizer, P config) {
        if(!optimizer.isConfigValid(config)) {
            Long orgId = securityService.getCurrentUserOrganizationId();
            throw new RuntimeBusinessException(
                    INTERNAL_SERVER_ERROR, O$CRT$0013, orgId, optimizer.getOptimizerName());
        };
    }



    private <T> T parseConfigJson(String configJson, Class<? extends T> configClass) {
        try{
            return objectMapper.readValue(configJson, configClass);
        }catch(Throwable e) {
            Long orgId = securityService.getCurrentUserOrganizationId();
            logger.error(e,e);
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, O$CRT$0014, orgId, WAREHOUSE);
        }
    }



    private Optional<OrganizationCartOptimizationEntity> getOptimizationConfigEntity(
            String optimizer, String shippingServiceId) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        Optional<OrganizationCartOptimizationEntity> shippingServiceOptimizationParams =
                orgCartOptimizationRepo
                        .findFirstByOptimizationStrategyAndShippingServiceIdAndOrganization_IdOrderByIdDesc(
                                optimizer, shippingServiceId, orgId);

        Optional<OrganizationCartOptimizationEntity> orgOptimizationParams =
                orgCartOptimizationRepo
                        .findFirstByOptimizationStrategyAndOrganization_IdOrderByIdDesc(optimizer, orgId);

        return firstExistingValueOf(
                shippingServiceOptimizationParams
                ,orgOptimizationParams);
    }
}
