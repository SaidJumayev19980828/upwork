package com.nasnav.service;

import com.nasnav.dto.response.PickupItem;

import java.util.Set;

public interface PickupItemService {
    void movePickupItemToCartItem(Set<Long> pickupItems);
    void moveCartItemToPickupItem(Set<Long> cartItems);
    Set<PickupItem> getPickupItems();

}
