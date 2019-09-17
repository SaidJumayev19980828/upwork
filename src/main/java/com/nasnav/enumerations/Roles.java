package com.nasnav.enumerations;

import java.util.stream.Stream;

import com.google.common.collect.Streams;

import lombok.Getter;

import java.util.*;

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
    @Getter
    private static final Set<String> nasnavAdminPrelivedge = new HashSet<String>(Arrays.asList("NASNAV_ADMIN", "ORGANIZATION_ADMIN", "ORGANIZATION_MANAGER", "ORGANIZATION_EMPLOYEE",
            "STORE_ADMIN", "STORE_MANAGER", "STORE_EMPLOYEE"));
    @Getter
    private static final Set<String> organizationAdminPrelivedge = new HashSet<String>(Arrays.asList("ORGANIZATION_ADMIN", "ORGANIZATION_MANAGER", "ORGANIZATION_EMPLOYEE",
            "STORE_ADMIN", "STORE_MANAGER", "STORE_EMPLOYEE"));
    @Getter
    private static final Set<String> organizationManagerPrelivedge = new HashSet<String>(Arrays.asList("ORGANIZATION_MANAGER", "ORGANIZATION_EMPLOYEE",
            "STORE_MANAGER", "STORE_EMPLOYEE"));
    @Getter
    private static final Set<String> organizationEmployeePrelivedge = new HashSet<String>(Arrays.asList("ORGANIZATION_EMPLOYEE", "STORE_EMPLOYEE"));
    @Getter
    private static final Set<String> storeAdminPrelivedge = new HashSet<String>(Arrays.asList("STORE_ADMIN", "STORE_MANAGER", "STORE_EMPLOYEE"));
    @Getter
    private static final Set<String> storeManagerPrelivedge = new HashSet<String>(Arrays.asList("STORE_MANAGER", "STORE_EMPLOYEE"));
    @Getter
    private static final Set<String> storeEmployeePrelivedge = new HashSet<String>(Arrays.asList("STORE_EMPLOYEE"));
    Roles(String value) {
        this.value = value;
    }


    public static Roles fromString(String text) {
    	return Stream.of(Roles.values())
    		.filter( role -> role.value.equals(text))
    		.findFirst()
    		.orElseThrow(() -> new IllegalStateException("No Role Enum exists with value: " + text));

    }

    public static Map getAllPrevliges(){
        Map<String, Set<String>> result = new HashMap<>();
        result.put("NASNAV_ADMIN",nasnavAdminPrelivedge);
        result.put("ORGANIZATION_ADMIN", organizationAdminPrelivedge);
        result.put("ORGANIZATION_MANAGER",organizationManagerPrelivedge);
        result.put("ORGANIZATION_EMPLOYEE",organizationEmployeePrelivedge);
        result.put("STORE_ADMIN",storeAdminPrelivedge);
        result.put("STORE_MANAGER",storeManagerPrelivedge);
        result.put("STORE_EMPLOYEE",storeEmployeePrelivedge);
        return result;
    }
}