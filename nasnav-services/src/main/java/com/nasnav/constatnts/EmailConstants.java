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

    public static final String OTP_PARAMETER = "#OTP#";

    public static final String ACTIVATION_ACCOUNT_EMAIL_SUBJECT = " Account Activation";

    public static final String NEW_EMAIL_ACTIVATION_TEMPLATE = "mail_templates/new_mail_recovery_template.html";

    public static final String OTP_TEMPLATE = "mail_templates/otp_template.html";

    public static final String ORDER_NOTIFICATION_TEMPLATE = "new_order_notification_template.html";

    public static final String META_ORDER_NOTIFICATION_TEMPLATE = "new_meta_order_notification_template.html";

    public static final String ORDER_BILL_TEMPLATE = "new_order_bill_template.html";
    
    public static final String ORDER_REJECT_TEMPLATE = "new_order_reject_template.html";

    public static final String ORDER_RETURN_REJECT_TEMPLATE = "order_return_reject_template.html";
    
    public static final String ORDER_CANCEL_NOTIFICATION_TEMPLATE = "new_order_cancel_notification_template.html";

    public static final String ORDER_RETURN_CONFIRM_TEMPLATE = "order_return_confirm_template.html";

    public static final String ORDER_RETURN_RECEIVED_TEMPLATE = "order_return_received_template.html";

    public static final String ORDER_RETURN_NOTIFICATION_TEMPLATE = "order_return_notification_template.html";

    public static final String USER_SUBSCRIPTION_TEMPLATE = "mail_templates/user_subscription_template.html";

    public static final String ABANDONED_CART_TEMPLATE = "abandoned_cart_template_2.html";

    public static final String RESTOCKED_WISHLIST_TEMPLATE = "restocked_wishlist_template.html";
    public static final String NEW_CLIENT_EMAIL_Booked_Appointment_TEMPLATE = "mail_templates/NEW_CLIENT_EMAIL_Booked_Appointment_TEMPLATE.html";
    public static final String NEW_EMPLOYEE_EMAIL_Booked_Appointment_TEMPLATE = "mail_templates/NEW_EMPLOYEE_EMAIL_Booked_Appointment_TEMPLATE.html";

    public static final String CONTACT_US_CUSTOMER_MAIL = "mail_templates/contact_us_customer_mail.html";

    public static final String CONTACT_US_FEEDBACK_MAIL = "mail_templates/contact_us_feedback_mail.html";


}
