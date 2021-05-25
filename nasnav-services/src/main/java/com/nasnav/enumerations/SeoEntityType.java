package com.nasnav.enumerations;

import lombok.Getter;

import java.util.Objects;

public enum SeoEntityType {

    ORGANIZATION(0),
    PRODUCT(1),
    TAG(2),
    CATEGORY(3);

    @Getter
    private final Integer value;

    SeoEntityType(Integer value){
        this.value = value;
    }

    public static SeoEntityType findEnum(Integer typeId) {
        for (SeoEntityType type : SeoEntityType.values()) {
            if ( Objects.equals(type.getValue() ,typeId) ) {
                return type;
            }
        }
        return null;
    }
}
