package com.nasnav.constatnts;

import lombok.Getter;

/**
 * Contain all constants related to Entities.
 */
public final class EntityConstants {
    public static final String INITIAL_PASSWORD = "!needs_reset!";
    public static final String EMAIL_PATTERN = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,6}$";
    public static final String Name_PATTERN = "^[a-zA-z \']+$";

    /*Password Min and Max length*/
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PASSWORD_MAX_LENGTH = 20;

    /*Generated Token default length*/
    public static final int TOKEN_LENGTH = 16;

    /*Number of hours that token stay valid during*/
    public static final int TOKEN_VALIDITY = 2;


    /**
     *Enum represent Keys of each {@link com.nasnav.persistence.Configuration} Entity in DB
     */
    public enum ConfigurationKey {

        MAIL_SERVER_HOST_NAME("email.hostname"),
        MAIL_SERVER_PORT("email.port"),
        MAIL_SERVER_EMAIL("email.username"),
        MAIL_SERVER_PASSWORD("email.password"),
        MAIL_SERVER_PROTOCOL("email.protocol"),
        MAIL_SERVER_SSL("email.ssl");

        @Getter
        private final String value;

        ConfigurationKey(String value) {
            this.value = value;
        }

        public static ConfigurationKey valueByDbKey(String dbKey) {
            for (ConfigurationKey key : values()) {
                if (key.getValue().equals(dbKey)) {
                    return key;
                }
            }
            return null;
        }
    }


    /**
     *Enum represent Keys of each {@link com.nasnav.persistence.Configuration} Entity in DB
     */
    public enum Roles {

        CUSTOMER("CUSTOMER"),
        ORGANIZATION_MANAGER("ORGANIZATION_MANAGER"),
        STORE_ADMIN("STORE_ADMIN"),
        STORE_MANAGER("STORE_MANAGER");

        @Getter
        private final String value;

        Roles(String value) {
            this.value = value;
        }

    }

}
