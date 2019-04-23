package com.nasnav.enumerations;

import lombok.Getter;

/**
 * Hold User Roles
 */
public enum Roles {
	CUSTOMER("CUSTOMER"),
    ORGANIZATION_MANAGER("ORGANIZATION_MANAGER"),
    STORE_ADMIN("STORE_ADMIN"),
    STORE_MANAGER("STORE_MANAGER"), 
    NASNAV_ADMIN("NASNAV_ADMIN"),
    ORGANIZATION_ADMIN("ORGANIZATION_ADMIN"),	
	ORGANIZATION_EMPLOYEE("ORGANIZATION_EMPLOYEE"),
	STORE_EMPLOYEE("STORE_EMPLOYEE");

    @Getter
    private final String value;

    Roles(String value) {
        this.value = value;
    }
}