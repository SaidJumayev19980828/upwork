package com.nasnav.enumerations;

import lombok.Getter;

import java.util.*;
import java.util.stream.Stream;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static java.util.Collections.emptySet;

/**
 * Hold User Roles
 */
public enum Roles {
	CUSTOMER("CUSTOMER", 100000, false ),
    ORGANIZATION_MANAGER("ORGANIZATION_MANAGER", 2, false),
    STORE_MANAGER("STORE_MANAGER", 4, true),
    NASNAV_ADMIN("NASNAV_ADMIN", -100000, true),
    NASNAV_EMPLOYEE("NASNAV_EMPLOYEE", -99999, false),
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
    private static final Set<String> nasnavAdminPrivilege =
            setOf(NASNAV_ADMIN.name(), NASNAV_EMPLOYEE.name(), ORGANIZATION_ADMIN.name(), ORGANIZATION_MANAGER.name(),
                    ORGANIZATION_EMPLOYEE.name(), STORE_MANAGER.name(), STORE_EMPLOYEE.name());

    @Getter
    private static final Set<String> nasnavEmployeePrivilege =
            setOf(NASNAV_EMPLOYEE.name(), ORGANIZATION_ADMIN.name(), ORGANIZATION_MANAGER.name(),
                    ORGANIZATION_EMPLOYEE.name(), STORE_MANAGER.name(), STORE_EMPLOYEE.name());
    @Getter
    private static final Set<String> organizationAdminPrivilege =
            setOf(ORGANIZATION_MANAGER.name(), ORGANIZATION_EMPLOYEE.name(),
                    STORE_MANAGER.name(), STORE_EMPLOYEE.name());
    @Getter
    private static final Set<String> organizationManagerPrivilege =
            setOf(ORGANIZATION_EMPLOYEE.name(),
                    STORE_MANAGER.name(), STORE_EMPLOYEE.name());
    @Getter
    private static final Set<String> organizationEmployeePrivilege = setOf(STORE_EMPLOYEE.name());
    @Getter
    private static final Set<String> storeManagerPrivilege = setOf(STORE_EMPLOYEE.name());
    @Getter
    private static final Set<String> storeEmployeePrivilege = emptySet();


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


    public static List<Roles> getSortedEmployeeRoles() {
        return List.of(NASNAV_ADMIN, NASNAV_EMPLOYEE, ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, ORGANIZATION_EMPLOYEE, STORE_MANAGER, STORE_EMPLOYEE);
    }

    public static Map<String, Set<String>> getAllPrivileges(){
        Map<String, Set<String>> result = new HashMap<>();
        result.put(NASNAV_ADMIN.name(), nasnavAdminPrivilege);
        result.put(NASNAV_EMPLOYEE.name(), nasnavEmployeePrivilege);
        result.put(ORGANIZATION_ADMIN.name(), organizationAdminPrivilege);
        result.put(ORGANIZATION_MANAGER.name(), organizationManagerPrivilege);
        result.put(ORGANIZATION_EMPLOYEE.name(), organizationEmployeePrivilege);
        result.put(STORE_MANAGER.name(), storeManagerPrivilege);
        result.put(STORE_EMPLOYEE.name(), storeEmployeePrivilege);
        return result;
    }
}