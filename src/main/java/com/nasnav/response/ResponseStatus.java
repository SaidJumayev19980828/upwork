package com.nasnav.response;

/**
 * Hold Response Status constants
 */
public enum ResponseStatus {
    EMAIL_EXISTS("the email is already registered in the database"),
    ACTIVATION_SENT("activation email sent to the user"),
    ACTIVATED("account is fully active"),
    NEED_ACTIVATION("account needs activation"),
    INVALID_EMAIL("provided email is invalid"),
    INVALID_NAME("provided name is invalid"),
    SYS_ERROR("Error handling request");

    private String value;
    ResponseStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}