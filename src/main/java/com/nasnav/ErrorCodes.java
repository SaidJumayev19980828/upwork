package com.nasnav;

import java.util.HashMap;

public class ErrorCodes {

    public final static int UNAUTHENTICATED = 1000;
    public final static int INSUFFICIENT_USER_RIGHTS = 1010;

    public final static int FIELD_EMPTY = 100;
    public final static int ERR_FIELD_TOO_SHORT = 101;
    public final static int ERR_FIELD_TOO_LONG = 102;
    public final static int ERR_FIELD_WRONG_TYPE = 103;
    public final static int FIELD_DOESNT_MATCH_REQUIRED_PATTERN = 104;
    public final static int ERR_FIELD_CONTAINS_INVALID_CHARACTERS = 105;

    public final static int INVALID_PARAM = 500;
    public final static int INVALID_EMAIL = 501;
    public final static int INVALID_NAME = 502;
    public final static int INVALID_STORE = 503;
    public final static int INVALID_ORGANIZATION = 504;
    public final static int INVALID_ROLE = 505;

    public final static int EMAIL_ALREADY_REGISTERED = 201;
    public final static int ACCOUNT_ACTIVATED = 202;
    public final static int ACCOUNT_NEEDS_ACTIVATION = 203;


    public final static HashMap<Integer, String> textStatus = initStatuses();

    private static HashMap<Integer, String> initStatuses() {
        HashMap<Integer, String> map = new HashMap<>();
        map.put(UNAUTHENTICATED, "UNAUTHENTICATED");
        map.put(INSUFFICIENT_USER_RIGHTS, "INSUFFICIENT_RIGHTS");

        map.put(FIELD_EMPTY, "MISSING_PARAM");

        map.put(INVALID_PARAM, "INVALID_PARAM");
        map.put(INVALID_EMAIL, "INVALID_EMAIL");
        map.put(INVALID_NAME, "INVALID_NAME");
        map.put(INVALID_STORE, "INVALID_STORE");
        map.put(INVALID_ORGANIZATION, "INVALID_ORGANIZATION");
        map.put(INVALID_ROLE, "INVALID_ROLE");

        map.put(EMAIL_ALREADY_REGISTERED, "EMAIL_EXISTS");
        map.put(ACCOUNT_ACTIVATED, "ACTIVATED");
        map.put(ACCOUNT_NEEDS_ACTIVATION, "NEED_ACTIVATION");
        return map;
    }
}
