package com.nasnav.enumerations;

import lombok.Getter;

public enum OrderSortOptions {
    ID("id"),
    CREATION_DATE("creationDate"),
    TOTAL("total"),
    QUANTITY("quantity");

    @Getter
    private String value;

    OrderSortOptions(String value){
        this.value = value;
    }
}