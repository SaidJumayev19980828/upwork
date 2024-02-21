package com.nasnav.enumerations;

import lombok.Getter;

@Getter
public enum LoyaltyTransactions {
    ORDER_ONLINE("Add points after completing an order"),
    SHARE_POINTS("Add points after sharing points between users"),
    REFERRAL("Add points as referral points"),
    TRANSFER_POINTS("Transfer points to another user"),
    REDEEM_POINTS("Redeem points"),
    SPEND_IN_ORDER("Use points to buy an online order"),
    PICKUP_FROM_SHOP("Use points to Pickup from shop");

    private final String description;

    LoyaltyTransactions(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
