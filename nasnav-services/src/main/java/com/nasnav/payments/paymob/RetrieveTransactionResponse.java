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
    public class Order{
        public int id;
        public Object created_at;
        public boolean delivery_needed;
        public Merchant merchant;
        public Object collector;
        public int amount_cents;
        public ShippingData shipping_data;
        public String currency;
        public boolean is_payment_locked;
        public boolean is_return;
        public boolean is_cancel;
        public boolean is_returned;
        public boolean is_canceled;
        public String merchant_order_id;
        public Object wallet_notification;
        public int paid_amount_cents;
        public boolean notify_user_with_email;
        public ArrayList<Object> items;
        public String order_url;
        public int commission_fees;
        public int delivery_fees_cents;
        public int delivery_vat_cents;
        public String payment_method;
        public Object merchant_staff_tag;
        public String api_source;
        public Object data;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class PaymentKeyClaims{
        public String currency;
        public boolean lock_order_when_paid;
        public int amount_cents;
        public int exp;
        public BillingData billing_data;
        public int integration_id;
        public int order_id;
        public String pmk_ip;
        public int user_id;
    }


        public int id;
        public boolean pending;
        public int amount_cents;
        public boolean success;
        public boolean is_auth;
        public boolean is_capture;
        public boolean is_standalone_payment;
        public boolean is_voided;
        public boolean is_refunded;
        public boolean is_3d_secure;
        public int integration_id;
        public int profile_id;
        public boolean has_parent_transaction;
        public Order order;
        public Object created_at;
        public Object transaction_processed_callback_responses;
        public String currency;
        public SourceData source_data;
        public String api_source;
        public Object terminal_id;
        public int merchant_commission;
        public Object installment;
        public boolean is_void;
        public boolean is_refund;
        public Object data;
        public boolean is_hidden;
        public PaymentKeyClaims payment_key_claims;
        public boolean error_occured;
        public boolean is_live;
        public Object other_endpoint_reference;
        public int refunded_amount_cents;
        public int source_id;
        public boolean is_captured;
        public int captured_amount;
        public Object merchant_staff_tag;
        public Object updated_at;
        public int owner;
        public Object parent_transaction;
        public String unique_ref;


    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class SourceData{
        public String sub_type;
        public Object tenure;
        public String pan;
        public String type;
    }


}
