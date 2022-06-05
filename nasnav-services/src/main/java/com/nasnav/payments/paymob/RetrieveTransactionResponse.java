package com.nasnav.payments.paymob;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class RetrieveTransactionResponse {
    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public class Merchant{
        private int id;
        private Object created_at;
        private ArrayList<String> phones;
        private ArrayList<String> company_emails;
        private String company_name;
        private String state;
        private String country;
        private String city;
        private String postal_code;
        private String street;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public class ShippingData{
        private int id;
        private String first_name;
        private String last_name;
        private String street;
        private String building;
        private String floor;
        private String apartment;
        private String city;
        private String state;
        private String country;
        private String email;
        private String phone_number;
        private String postal_code;
        private String extra_description;
        private String shipping_method;
        private int order_id;
        private int order;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public class Data{
        private MigsTransaction migs_transaction;
        private String card_type;
        private String authorize_id;
        private MigsOrder migs_order;
        private String transaction_no;
        private double captured_amount;
        private String avs_result_code;
        private String merchant;
        private String secure_hash;
        private String txn_response_code;
        private double refunded_amount;
        private double amount;
        private String message;
        private int gateway_integration_pk;
        private String avs_acq_response_code;
        private String klass;
        private String acq_response_code;
        private String currency;
        private Object batch_no;
        private String merchant_txn_ref;
        private String receipt_no;
        private String order_info;
        private Object created_at;
        private String migs_result;
        private double authorised_amount;
        private String card_num;
        private int status_code;
        private Json json;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public class Order{
        private int id;
        private Object created_at;
        private boolean delivery_needed;
        private Merchant merchant;
        private Object collector;
        private int amount_cents;
        private ShippingData shipping_data;
        private String currency;
        private boolean is_payment_locked;
        private boolean is_return;
        private boolean is_cancel;
        private boolean is_returned;
        private boolean is_canceled;
        private Object merchant_order_id;
        private Object wallet_notification;
        private int paid_amount_cents;
        private boolean notify_user_with_email;
        private ArrayList<Object> items;
        private String order_url;
        private int commission_fees;
        private int delivery_fees_cents;
        private int delivery_vat_cents;
        private String payment_method;
        private Object merchant_staff_tag;
        private String api_source;
        private Data data;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public class SourceData{
        private String type;
        private Object tenure;
        private String sub_type;
        private String pan;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public class Acquirer{
        private String date;
        private String id;
        private String timeZone;
        private String merchantId;
        private String transactionId;
        private int batch;
        private String settlementDate;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public class MigsTransaction{
        private String type;
        private double amount;
        private Acquirer acquirer;
        private String id;
        private String source;
        private String currency;
        private String receipt;
        private String terminal;
        private String frequency;
        private String authorizationCode;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public class MigsOrder{
        private String currency;
        private double amount;
        private boolean acceptPartialAmount;
        private String status;
        private String id;
        private Object creationTime;
        private double totalRefundedAmount;
        private double totalAuthorizedAmount;
        private double totalCapturedAmount;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public class Error{
        private String field;
        private String cause;
        private String explanation;
        private String validationType;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public class Json{
        private String result;
        private Error error;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public class BillingData{
        private int id;
        private String first_name;
        private String last_name;
        private String street;
        private String building;
        private String floor;
        private String apartment;
        private String city;
        private String state;
        private String country;
        private String email;
        private String phone_number;
        private String postal_code;
        private String ip_address;
        private String extra_description;
        private int transaction_id;
        private Object created_at;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public class Result{
        private int id;
        private boolean pending;
        private int amount_cents;
        private boolean success;
        private boolean is_auth;
        private boolean is_capture;
        private boolean is_standalone_payment;
        private boolean is_voided;
        private boolean is_refunded;
        private boolean is_3d_secure;
        private int integration_id;
        private Object terminal_id;
        private String terminal_branch_id;
        private boolean has_parent_transaction;
        private Order order;
        private Object created_at;
        private Object paid_at;
        private String currency;
        private SourceData source_data;
        private String api_source;
        private String fees;
        private String vat;
        private String converted_gross_amount;
        private Data data;
        private boolean is_cashout;
        private Object wallet_transaction_type;
        private boolean is_upg;
        private boolean is_internal_refund;
        private BillingData billing_data;
        private Object installment;
        private String integration_type;
        private String card_type;
        private String routing_bank;
        private String card_holder_bank;
        private int merchant_commission;
        private Object extra_detail;
        private ArrayList<Object> discount_details;
        private Object pre_conversion_currency;
        private Object pre_conversion_amount_cents;
        private boolean is_host2host;
        private boolean is_void;
        private boolean is_refund;
        private boolean is_hidden;
        private boolean error_occured;
        private boolean is_live;
        private Object other_endpoint_reference;
        private int refunded_amount_cents;
        private int source_id;
        private boolean is_captured;
        private int captured_amount;
        private Object merchant_staff_tag;
        private Object updated_at;
        private int owner;
        private Object parent_transaction;
    }

    private int count;
    private String next;
    private Object previous;
    private ArrayList<Result> results;
    
}
