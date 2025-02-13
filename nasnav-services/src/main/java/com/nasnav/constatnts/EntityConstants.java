package com.nasnav.constatnts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Contain all constants related to Entities.
 */
public final class EntityConstants {
    public static final String INITIAL_PASSWORD = "";
    public static final String EMAIL_PATTERN = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,6}$";
    public static final String Name_PATTERN = "^[a-zA-z0-9 \'&-]+$";
    public static final String DATE_TIME_PATTERN =
                    "^20[1-9][0-9]-" +  // YYYY
                    "(0{1}[1-9]|1[0-2])-" + // MM
                    "(0{1}[1-9]|1[0-9]|2[0-9]|3[0-1])" + // DD
                    "(T|:)(0{1}[0-9]|1[0-9]|2[0-3]):" + // HH
                    "(0{1}[0-9]|1[0-9]|2[0-9]|3[0-9]|4[0-9]|5[0-9]):?" + // MM
                    "(0{1}[0-9]|1[0-9]|2[0-9]|3[0-9]|4[0-9]|5[0-9])?.?[\\d]*" + // SS.MMMM
                    "Z?(\\[UTC\\])?$"; // Z[UTC]
    /*Password Min and Max length*/
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PASSWORD_MAX_LENGTH = 20;

    /*Generated Token default length*/
    public static final int TOKEN_LENGTH = 16;

    /*Number of hours that token stay valid during*/
    public static final int TOKEN_VALIDITY = 2;

    //Number of seconds that login token stay valid during
    public static final int AUTH_TOKEN_VALIDITY = 2592000; // 1 month

    public static final String OAUTH_ENTER_EMAIL_PAGE = "/user/login/oauth2/complete_registeration?token=";


    public static final String NASNAV_DOMAIN = "nasnav.com";
    public static final String NASORG_DOMAIN = "nasnav.org";

    public static final String PROTOCOL = "https://";
    
    public static final String TOKEN_HEADER = "User-Token";

    public enum Operation{
    	UPDATE("update"),
    	CREATE("create"),
    	ADD("add"),
    	DELETE("delete");
    	
    	
    	@Getter
    	@JsonValue
        private final String value;
    	
    	@JsonCreator
    	Operation(String value) {
            this.value = value;
        }
    }


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

}
