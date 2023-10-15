package com.nasnav.service.impl;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.PackageRepository;
import com.nasnav.dto.stripe.StripeSubscriptionPendingDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.PackageEntity;
import com.nasnav.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@Service
public class StripeServiceImpl implements StripeService {

    @Value("${stripe.apikey}")
    public String apiKey;

    @Value("${stripe.webhook.secret}")
    public String webhookSecret;

    @Autowired
    PackageRepository packageRepository;

    private static final Logger stripeLogger = LogManager.getLogger("Subscription:STRIPE");

    @PostConstruct
    public void init() {
        if(StringUtils.isBlankOrNull(apiKey)){
            stripeLogger.error("Fail To Load Api key of Stripe");
        }else{
            Stripe.apiKey = apiKey;
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
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, STR$CAL$0003);
        }

        return customerId;
    }



    public StripeSubscriptionPendingDTO createSubscription(String stripePriceId , String customerId){
        StripeSubscriptionPendingDTO stripeSubscriptionPendingDTO = null;
        try {
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
            stripeSubscriptionPendingDTO = new StripeSubscriptionPendingDTO();
            if (subscription.getPendingSetupIntentObject() != null) {
                stripeSubscriptionPendingDTO.setType("setup");
                stripeSubscriptionPendingDTO.setClientSecret(subscription.getPendingSetupIntentObject().getClientSecret());
            }
            else {
                stripeSubscriptionPendingDTO.setType("payment");
                stripeSubscriptionPendingDTO.setClientSecret( subscription.getLatestInvoiceObject().getPaymentIntentObject().getClientSecret());
            }

        } catch (StripeException e) {
            stripeLogger.error("Failed To Create Stripe Subscription :" + e.getMessage());
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, STR$CAL$0004);
        }
        return stripeSubscriptionPendingDTO;
    }


    @Override
    public Subscription getSubscriptionFromWebhookEvent(Event event){
        Subscription subscription= (Subscription) event.getDataObjectDeserializer().getObject().get();
        System.out.println(subscription.getStatus());
        System.out.println(subscription.getStartDate());
        System.out.println(subscription.getStartDate());
//        System.out.println(subscription.getStartDate());
        return subscription;


    }







    @Override
    public Event verifyAndGetEventWebhook(String signatureHeader , String body){
        Event event = null;
        try {
            event = Webhook.constructEvent(body, signatureHeader, webhookSecret);
        } catch (Exception e) {
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
            throw new RuntimeBusinessException(NOT_FOUND, STR$WH$0003);
        }
        return packageEntity;
    }
}