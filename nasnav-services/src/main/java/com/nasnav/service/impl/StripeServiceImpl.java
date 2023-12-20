package com.nasnav.service.impl;

import com.nasnav.AppConfig;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.PackageRepository;
import com.nasnav.dto.stripe.StripeConfirmDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.PackageEntity;
import com.nasnav.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.stripe.param.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@Service
public class StripeServiceImpl implements StripeService {

    @Autowired
    AppConfig appConfig;
    @Autowired
    PackageRepository packageRepository;

    private static final Logger stripeLogger = LogManager.getLogger(StripeServiceImpl.class);

    @PostConstruct
    public void init() {
        if(StringUtils.isBlankOrNull(appConfig.stripeApiKey)){
            stripeLogger.error("init : Fail To Load Api key of Stripe");
        }else{
            Stripe.apiKey = appConfig.stripeApiKey;
            stripeLogger.info("init : API Key Initialized Successfully");
        }
    }

    public String createCustomer(String name ,String email){
        String customerId = null;
        try {
            CustomerCreateParams params =
                    CustomerCreateParams.builder()
                            .setEmail(email)
                            .setName(name)
                            .build();
            Customer customer = Customer.create(params);
            customerId = customer.getId();
        } catch (StripeException e) {
            stripeLogger.error("createCustomer : + " + String.format(STR$CAL$0003.getValue(),email));
            stripeLogger.error("createCustomer :  Exception : " + e.getMessage());
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, STR$CAL$0003,email);
        }

        return customerId;
    }



    public StripeConfirmDTO createSubscription(String stripePriceId , String customerId){
        StripeConfirmDTO stripeConfirmDTO = null;
        try {
            stripeLogger.info(String.format("createSubscription : For customerId : %s , To stripePriceId :" , customerId,stripePriceId));
            SubscriptionCreateParams.PaymentSettings paymentSettings =
                    SubscriptionCreateParams.PaymentSettings
                            .builder()
                            .setSaveDefaultPaymentMethod(SubscriptionCreateParams.PaymentSettings.SaveDefaultPaymentMethod.ON_SUBSCRIPTION)
                            .build();

            SubscriptionCreateParams subCreateParams = SubscriptionCreateParams
                    .builder()
                    .setCustomer(customerId)
                    .addItem(
                            SubscriptionCreateParams
                                    .Item.builder()
                                    .setPrice(stripePriceId)
                                    .build()
                    )
                    .setPaymentSettings(paymentSettings)
                    .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                    .addAllExpand(Arrays.asList("latest_invoice.payment_intent", "pending_setup_intent"))
                    .build();

            Subscription subscription = Subscription.create(subCreateParams);
            stripeConfirmDTO = new StripeConfirmDTO();
            if (subscription.getPendingSetupIntentObject() != null) {
                stripeConfirmDTO.setType("setup");
                stripeConfirmDTO.setClientSecret(subscription.getPendingSetupIntentObject().getClientSecret());
            }
            else {
                stripeConfirmDTO.setType("payment");
                stripeConfirmDTO.setClientSecret( subscription.getLatestInvoiceObject().getPaymentIntentObject().getClientSecret());
            }

        } catch (StripeException e) {
            stripeLogger.error("createSubscription : " + STR$CAL$0004.getValue());
            stripeLogger.error("createSubscription : Exception : " + e.getMessage());
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, STR$CAL$0004);
        }
        return stripeConfirmDTO;
    }



    public StripeConfirmDTO setupIntent(String customerId){
        StripeConfirmDTO stripeConfirmDTO = null;
        try {
            stripeLogger.info(String.format("setupIntent : For customerId : %s" , customerId));
            SetupIntentCreateParams params =
            SetupIntentCreateParams.builder()
                    .setCustomer(customerId)
                    .setAutomaticPaymentMethods(
                            SetupIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                    )
                    .build();
            SetupIntent setupIntent = SetupIntent.create(params);
            stripeConfirmDTO = new StripeConfirmDTO();
            stripeConfirmDTO.setClientSecret(setupIntent.getClientSecret());
//            System.out.println(setupIntent.get());
        } catch (StripeException e) {
            stripeLogger.error("setupIntent : " + STR$CAL$0002.getValue());
            stripeLogger.error("setupIntent : Exception : " + e.getMessage());
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, STR$CAL$0002);
        }
        return stripeConfirmDTO;

}

    @Override
    public StripeObject getStripeObjectFromWebhookEvent(Event event){
        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().get();
        stripeLogger.info(String.format("Stripe Object From Webhook Event : %s " , stripeObject.toString()));
        return stripeObject;
    }

    @Override
    public void updateCustomerDefaultPaymentMethod(String customerId, String paymentMethodId){
        try {
            Customer resource = Customer.retrieve(customerId);
            CustomerUpdateParams params =
                    CustomerUpdateParams.builder()
                            .setInvoiceSettings(
                                    CustomerUpdateParams.InvoiceSettings.builder()
                                            .setDefaultPaymentMethod(paymentMethodId)
                                            .build()
                            )
                            .build();
            Customer customer = resource.update(params);
        } catch (StripeException e) {
            stripeLogger.error("updateCustomerDefaultPaymentMethod : " + STR$WH$0005.getValue());
            stripeLogger.error("updateCustomerDefaultPaymentMethod : Exception : " + e.getMessage());
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, STR$WH$0005);
        }
    }

    @Override
    public void updateSubscriptionDefaultPaymentMethod(String subscriptionId, String paymentMethodId){
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            Map<String, Object> params = new HashMap<>();
            params.put("default_payment_method", paymentMethodId);
            subscription.update(params);
        } catch (StripeException e) {
            stripeLogger.error("updateSubscriptionDefaultPaymentMethod : " + STR$WH$0006.getValue());
            stripeLogger.error("updateSubscriptionDefaultPaymentMethod : Exception : " + e.getMessage());
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, STR$WH$0006);
        }
    }





    @Override
    public Event verifyAndGetEventWebhook(String signatureHeader , String body){
        Event event = null;
        try {
            event = Webhook.constructEvent(body, signatureHeader, appConfig.stripeWebhookSecret);
        } catch (Exception e) {
            stripeLogger.error("verifyAndGetEventWebhook : " + STR$WH$0001.getValue());
            stripeLogger.error("verifyAndGetEventWebhook : Exception : " + e.getMessage());
            // Invalid payload
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, STR$WH$0001);
        }
        return event;

    }



    public PackageEntity getPackageByStripeSubscription(Subscription subscription){
        PackageEntity packageEntity = null;
        try {
            List<SubscriptionItem> subscriptionItemList = subscription.getItems().getData();
            SubscriptionItem subscriptionItem = subscriptionItemList.get(0);
            String priceId = subscriptionItem.getPrice().getId();
            packageEntity = packageRepository.findByStripePriceId(priceId).get();
        }catch (Exception ex){
            stripeLogger.error("getPackageByStripeSubscription : " + STR$WH$0003.getValue());
            stripeLogger.error("getPackageByStripeSubscription : Exception : " + ex.getMessage());
            throw new RuntimeBusinessException(NOT_FOUND, STR$WH$0003);
        }
        return packageEntity;
    }


    public void lastOpenInvoicePayRetry(String customerId , String subscriptionId){
        try {
            stripeLogger.info(String.format("Open Last Invoice To Retry Pay : For customerId : %s , To subscriptionId :" , customerId,subscriptionId));
            Map<String, Object> params = new HashMap<>();
            params.put("customer", customerId);
            params.put("subscription", subscriptionId);
            params.put("status", "open");
            params.put("limit", 1);
            List<Invoice> invoices = Invoice.list(params).getData();
            if(!invoices.isEmpty()){
                invoices.get(0).pay();
            }
        }catch (StripeException e){
            stripeLogger.error("lastOpenInvoicePayRetry : " + STR$WH$0007.getValue());
            stripeLogger.error("lastOpenInvoicePayRetry : Exception : " + e.getMessage());
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, STR$WH$0007);
        }
    }

    public void cancelSubscription(String subscriptionId){
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            Subscription deletedSubscription = subscription.cancel();
        }catch (StripeException e){
            stripeLogger.error("cancelSubscription : " + STR$CAL$0001.getValue());
            stripeLogger.error("cancelSubscription : Exception : " + e.getMessage());
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, STR$CAL$0001);
        }
    }

    public Subscription changePlan(String subscriptionId, String priceId){
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            SubscriptionUpdateParams params = SubscriptionUpdateParams
                    .builder()
                    .addItem(
                            SubscriptionUpdateParams
                                    .Item.builder()
                                    .setId(subscription.getItems().getData().get(0).getId())
                                    .setPrice(priceId)
                                    .build()
                    )
                    .setCancelAtPeriodEnd(false)
                    .build();

            return subscription.update(params);
        }catch (StripeException e){
            stripeLogger.error("changePlan : Exception : " + e.getMessage());
            if(e.getMessage().startsWith("No such price")){
                stripeLogger.error("changePlan : " + String.format(STR$CAL$0006.getValue(),priceId));
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, STR$CAL$0006,priceId);
            }else {
                stripeLogger.error("changePlan : " + STR$CAL$0005.getValue());
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, STR$CAL$0005);
            }
        }
    }


}