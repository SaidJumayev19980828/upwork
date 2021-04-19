package com.nasnav.enumerations;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.nasnav.commons.utils.CollectionUtils.setOf;

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
    private static final Set<String> nasnavAdminPrelivedge =
            setOf(NASNAV_ADMIN.name(), ORGANIZATION_ADMIN.name(), ORGANIZATION_MANAGER.name(),
                    ORGANIZATION_EMPLOYEE.name(), STORE_MANAGER.name(), STORE_EMPLOYEE.name());
    @Getter
    private static final Set<String> organizationAdminPrelivedge =
            setOf(ORGANIZATION_ADMIN.name(), ORGANIZATION_MANAGER.name(), ORGANIZATION_EMPLOYEE.name(),
                    STORE_MANAGER.name(), STORE_EMPLOYEE.name());
    @Getter
    private static final Set<String> organizationManagerPrelivedge =
            setOf(ORGANIZATION_MANAGER.name(), ORGANIZATION_EMPLOYEE.name(),
                    STORE_MANAGER.name(), STORE_EMPLOYEE.name());
    @Getter
    private static final Set<String> organizationEmployeePrelivedge = setOf(ORGANIZATION_EMPLOYEE.name(), STORE_EMPLOYEE.name());
    @Getter
    private static final Set<String> storeManagerPrelivedge = setOf(STORE_MANAGER.name(), STORE_EMPLOYEE.name());
    @Getter
    private static final Set<String> storeEmployeePrelivedge = setOf(STORE_EMPLOYEE.name());


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




    public static Map<String, Set<String>> getAllPrevliges(){
        Map<String, Set<String>> result = new HashMap<>();
        result.put(NASNAV_ADMIN.name(), nasnavAdminPrelivedge);
        result.put(ORGANIZATION_ADMIN.name(), organizationAdminPrelivedge);
        result.put(ORGANIZATION_MANAGER.name(), organizationManagerPrelivedge);
        result.put(STORE_EMPLOYEE.name(), organizationEmployeePrelivedge);
        result.put(STORE_MANAGER.name(), storeManagerPrelivedge);
        result.put(STORE_EMPLOYEE.name(), storeEmployeePrelivedge);
        return result;
    }
}