package com.nasnav.service;

import com.nasnav.dao.PickupItemRepository;
import com.nasnav.dto.response.PickupItem;
import com.nasnav.persistence.*;
import com.nasnav.service.helpers.CartServiceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

@Service
public class PickupItemServiceImpl implements PickupItemService {

    @Autowired
    private SecurityService securityService;
    @Autowired
    private ProductService productService;
    @Autowired
    private CartServiceHelper cartServiceHelper;
    @Autowired
    private PickupItemRepository pickupItemRepo;

    @Override
    public void movePickupItemToCartItem(Set<Long> pickupItems) {
        Long userId = securityService.getCurrentUser().getId();
        if (pickupItems == null) {
            pickupItemRepo.moveAllPickupItemsToCartItems(userId);
        } else {
            pickupItemRepo.movePickupItemsToCartItems(pickupItems, userId);
        }
    }

    @Override
    public void moveCartItemToPickupItem(Set<Long> cartItems) {
        Long userId = securityService.getCurrentUser().getId();
        pickupItemRepo.moveCartItemsToPickupItems(cartItems, userId);
    }

    @Override
    public Set<PickupItem> getPickupItems() {
        Long userId = securityService.getCurrentUser().getId();
        return pickupItemRepo.findCurrentPickupItemsByUser_Id(userId)
                .stream()
                .map(this::toPickupItemDto)
                .collect(toSet());
    }

    private PickupItem toPickupItemDto(PickupItemEntity entity) {
        PickupItem item = new PickupItem();
        StocksEntity stock = entity.getStock();
        ProductVariantsEntity variant = stock.getProductVariantsEntity();
        ProductEntity product = variant.getProductEntity();
        BrandsEntity brand = product.getBrand();
        UserEntity user = entity.getUser();
        String unit = ofNullable(stock.getUnit())
                .map(StockUnitEntity::getName)
                .orElse("");
        Map<String,String> variantFeatures = ofNullable(productService.parseVariantFeatures(variant, 0))
                .orElse(new HashMap<>());
        Map<String,Object> additionalData = cartServiceHelper.getAdditionalDataAsMap(entity.getAdditionalData());

        item.setBrandId( brand.getId());
        item.setBrandLogo(brand.getLogo());
        item.setBrandName(brand.getName());

        item.setCoverImg(entity.getCoverImage());
        item.setPrice(stock.getPrice());
        item.setQuantity(entity.getQuantity());
        item.setVariantFeatures(variantFeatures);
        item.setName(product.getName());
        item.setWeight(variant.getWeight());
        item.setUnit(unit);

        item.setId(entity.getId());
        item.setProductId(product.getId());
        item.setVariantId(variant.getId());
        item.setVariantName(variant.getName());
        item.setProductType(product.getProductType());
        item.setStockId(stock.getId());
        item.setDiscount(stock.getDiscount());
        item.setAdditionalData(additionalData);
        item.setUserId(user.getId());
        item.setOrgId(product.getOrganizationId());
        return item;
    }
}
