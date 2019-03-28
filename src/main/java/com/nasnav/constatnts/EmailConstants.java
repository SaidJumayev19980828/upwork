package com.nasnav.constatnts;

/**
 * Contain all constants related to NasNav Emails.
 */
public final class EmailConstants {

    /* Represent subject of password recovery mail */
    public static final String CHANGE_PASSWORD_EMAIL_SUBJECT = "NasNav Password Recovery";

    /* Represent path of password recovery mail template */
    public static final String CHANGE_PASSWORD_EMAIL_TEMPLATE = "mail_templates/mail-recover-user.html";

    /* Represent URL of password recovery page that user should be redirected to when changing his password */
    public static final String CHANGE_PASSWORD_URL = "http://localhost:8080/recover?token=";

    /* Represent UserName parameter to be replaced by the requester username and send at password recovery mail */
    public static final String USERNAME_PARAMETER = "#UserName#";

    /* Represent RecoveryLink parameter to be replaced by the requester username and send at password recovery mail */
    public static final String CHANGE_PASSWORD_URL_PARAMETER = "#RecoveryLink#";
}
