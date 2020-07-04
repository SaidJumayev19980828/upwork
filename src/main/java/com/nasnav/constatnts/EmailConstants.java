package com.nasnav.constatnts;

/**
 * Contain all constants related to NasNav Emails.
 */
public final class EmailConstants {

    /* Represent subject of password recovery mail */
    public static final String CHANGE_PASSWORD_EMAIL_SUBJECT = "NasNav Password Recovery"; //NOSONAR

    /* Represent path of password recovery mail template */
    public static final String CHANGE_PASSWORD_EMAIL_TEMPLATE = "mail_templates/mail-recover-user.html"; //NOSONAR

    /* Represent UserName parameter to be replaced by the requester username and send at password recovery mail */
    public static final String USERNAME_PARAMETER = "#UserName#";

    /* Represent RecoveryLink parameter to be replaced by the requester username and send at password recovery mail */
    public static final String CHANGE_PASSWORD_URL_PARAMETER = "#RecoveryLink#"; //NOSONAR

    public static final String ACTIVATION_ACCOUNT_URL_PARAMETER = "#ActivationLink#";

    public static final String ACCOUNT_EMAIL_PARAMETER = "#AccountEmail#";

    public static final String ACTIVATION_ACCOUNT_EMAIL_SUBJECT = "NasNav Account Activation";

    public static final String NEW_EMAIL_ACTIVATION_TEMPLATE = "mail_templates/new_mail_recovery_template.html";
    
    public static final String ORDER_NOTIFICATION_TEMPLATE = "mail_templates/order_notification_template.html";
    
    public static final String ORDER_BILL_TEMPLATE = "order_bill_template.html";
}
