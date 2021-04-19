package com.nasnav.service.cart.optimizers;

import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.dao.AddressRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.SubAreaRepository;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.persistence.SubAreasEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.service.cart.optimizers.parameters.ShopPerSubAreaOptCartParams;
import com.nasnav.service.cart.optimizers.parameters.ShopPerSubAreaOptConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.nasnav.commons.utils.EntityUtils.firstExistingValueOf;
import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0015;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.SHOP_PER_SUBAREA;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;


/**
 * Cart optimizer that assigns a shop based on the sub-area of the customer address.
 * */
@Service(SHOP_PER_SUBAREA)
public class ShopPerSubAreaOptimizer implements CartOptimizer<ShopPerSubAreaOptCartParams, ShopPerSubAreaOptConfig>{

    @Autowired
    private AddressRepository addressRepo;

    @Autowired
    private CartOptimizationHelper helper;

    @Autowired
    private ShopsRepository shopRepo;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private SubAreaRepository subAreaRepo;

    @Override
    public Optional<ShopPerSubAreaOptCartParams> createCartOptimizationParameters(CartCheckoutDTO dto) {
        return ofNullable(dto)
                .map(CartCheckoutDTO::getAddressId)
                .flatMap(addressRepo::findByIdWithDetails)
                .map(AddressesEntity::getSubAreasEntity)
                .map(SubAreasEntity::getId)
                .map(ShopPerSubAreaOptCartParams::new);
    }



    @Override
    public Optional<OptimizedCart> createOptimizedCart(Optional<ShopPerSubAreaOptCartParams> parameters, ShopPerSubAreaOptConfig config, Cart cart) {
        Long shopId =
                parameters
                .flatMap(params -> getSubAreaShop(params, config))
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0015));
        Map<Long, StocksEntity> stocks = helper.getCartItemsStockInShop(cart, shopId);
        return helper.createOptimizedCart(cart, stocks);
    }



    @Override
    public Class<? extends ShopPerSubAreaOptCartParams> getCartParametersClass() {
        return ShopPerSubAreaOptCartParams.class;
    }



    @Override
    public Class<? extends ShopPerSubAreaOptConfig> getConfigurationClass() {
        return ShopPerSubAreaOptConfig.class;
    }



    @Override
    public Boolean isConfigValid(ShopPerSubAreaOptConfig config) {
        return isRequiredConfigurationParametersExists(config)
                && isValidShopsProvided(config)
                && isValidSubAreasProvided(config);
    }



    private boolean isValidSubAreasProvided(ShopPerSubAreaOptConfig config) {
        Set<Long> providedSubAreas = getProvidedSubAreas(config);
        Set<Long> existingSubAreas = getExistingSubAreas(providedSubAreas);
        return existingSubAreas.containsAll(providedSubAreas);
    }



    private Set<Long> getExistingSubAreas(Set<Long> providedSubAreas) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return subAreaRepo.findByIdInAndOrganizationId_Id(providedSubAreas, orgId);
    }


    private Set<Long> getProvidedSubAreas(ShopPerSubAreaOptConfig config) {
        return ofNullable(config)
                .map(ShopPerSubAreaOptConfig::getSubAreaShopMapping)
                .map(Map::keySet)
                .orElse(emptySet())
                .stream()
                .map(EntityUtils::parseLongSafely)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
    }



    private boolean isValidShopsProvided(ShopPerSubAreaOptConfig config) {
        Set<Long> providedShops = getProvidedShops(config);
        Set<Long> existingShops = getExistingShops(providedShops);
        return existingShops.containsAll(providedShops);
    }



    private Set<Long> getExistingShops(Set<Long> providedShops) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return shopRepo
                .findByIdInAndOrganizationEntity_IdAndRemoved(providedShops, orgId, 0)
                .stream()
                .map(ShopsEntity::getId)
                .collect(toSet());
    }


    private Set<Long> getProvidedShops(ShopPerSubAreaOptConfig config) {
        Set<Long> shops = getSubAreaShops(config);
        ofNullable(config).map(ShopPerSubAreaOptConfig::getDefaultShop).ifPresent(shops::add);
        return shops;
    }



    private HashSet<Long> getSubAreaShops(ShopPerSubAreaOptConfig config) {
        return ofNullable(config)
                .map(ShopPerSubAreaOptConfig::getSubAreaShopMapping)
                .map(Map::values)
                .map(HashSet::new)
                .orElse(new HashSet<>());
    }



    private boolean isRequiredConfigurationParametersExists(ShopPerSubAreaOptConfig parameters) {
        return noneIsNull(parameters, parameters.getSubAreaShopMapping())
                && !parameters.getSubAreaShopMapping().isEmpty();
    }



    @Override
    public Boolean areCartParametersValid(ShopPerSubAreaOptCartParams parameters) {
        return true;
    }



    @Override
    public String getOptimizerName() {
        return SHOP_PER_SUBAREA;
    }



    private Optional<Long> getSubAreaShop(ShopPerSubAreaOptCartParams parameters, ShopPerSubAreaOptConfig config) {
        Optional<Long> defaultShop = ofNullable(config.getDefaultShop());
        Optional<Long> subAreaShop = doGetSubAreaShop(parameters, config);
        return firstExistingValueOf(subAreaShop, defaultShop);
    }



    private Optional<Long> doGetSubAreaShop(ShopPerSubAreaOptCartParams parameters, ShopPerSubAreaOptConfig config) {
        Map<String, Long> subAreaShopMapping = config.getSubAreaShopMapping();
        return getSubArea(parameters)
                .map(Object::toString)
                .map(subAreaShopMapping::get);
    }



    private Optional<Long> getSubArea(ShopPerSubAreaOptCartParams parameters) {
        return ofNullable(parameters)
                .map(ShopPerSubAreaOptCartParams::getSubAreaId);
    }
}
