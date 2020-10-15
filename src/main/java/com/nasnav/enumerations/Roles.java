package com.nasnav.enumerations;

import java.util.stream.Stream;

import com.google.common.collect.Streams;

import lombok.Getter;

import java.util.*;

/**
 * Hold User Roles
 */
public enum Roles {
	CUSTOMER("CUSTOMER", 100000, false ),
    ORGANIZATION_MANAGER("ORGANIZATION_MANAGER", 2, false),
    STORE_MANAGER("STORE_MANAGER", 4, true),
    NASNAV_ADMIN("NASNAV_ADMIN", -100000, true),
    ORGANIZATION_ADMIN("ORGANIZATION_ADMIN", 1, true),
	ORGANIZATION_EMPLOYEE("ORGANIZATION_EMPLOYEE", 3, false),
	STORE_EMPLOYEE("STORE_EMPLOYEE", 5, false);

    @Getter
    private final String value;

    @Getter
    private final int level;

    @Getter
    private final boolean canCreateUsers;

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


    Roles(String value, int level, boolean canCreateUsers) {
        this.value = value;
        this.level = level;
        this.canCreateUsers = canCreateUsers;
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