package com.nasnav.service.impl.subscription;

import com.drew.lang.StringUtil;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.PackageRepository;
import com.nasnav.dao.StripeCustomerRepository;
import com.nasnav.dao.SubscriptionRepository;
import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.enumerations.SubscriptionMethod;
import com.nasnav.enumerations.SubscriptionStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.PackageEntity;
import com.nasnav.persistence.StripeCustomerEntity;
import com.nasnav.persistence.SubscriptionEntity;
import com.nasnav.service.PackageService;
import com.nasnav.service.StripeService;
import com.nasnav.service.StripeWebhookSubscriptionService;
import com.stripe.model.Event;
import com.stripe.model.SetupIntent;
import com.stripe.model.Subscription;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class StripeWebhookSubscriptionServiceImpl implements StripeWebhookSubscriptionService {

    @Autowired
    PackageService packageService;
    @Autowired
    PackageRepository packageRepository;
    @Autowired
    private StripeService stripeService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private StripeSubscriptionServiceImpl stripeSubscriptionServiceImpl;

    @Autowired
    private StripeCustomerRepository stripeCustomerRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private static final Logger logger = LogManager.getLogger("Subscription:StripeWebhookSubscriptionServiceImpl");


    @Override
    public void handleStripeSubscriptionCreated(Event event) throws RuntimeBusinessException {
        Subscription subscription = (Subscription) stripeService.getStripeObjectFromWebhookEvent(event);
        //check Exists in database
        Optional<SubscriptionEntity> subscriptionEntityOptional = subscriptionRepository.findByStripeSubscriptionId(subscription.getId());
        if(!subscriptionEntityOptional.isPresent()){
            //create
            createSubscription(subscription);
        }

    }
    @Override
    public void handleStripeSubscriptionUpdated(Event event) throws RuntimeBusinessException {
        Subscription subscription = (Subscription) stripeService.getStripeObjectFromWebhookEvent(event);
        //check Exists in database
        Optional<SubscriptionEntity> subscriptionEntityOptional = subscriptionRepository.findByStripeSubscriptionId(subscription.getId());
        if(subscriptionEntityOptional.isPresent()){
            //update
            updateSubscription(subscription);
        }else{
            //create
            createSubscription(subscription);
        }

    }
    @Override
    public void handleStripeSubscriptionDeleted(Event event) throws RuntimeBusinessException {
        logger.debug("Delete Subscription...");
        Subscription subscription = (Subscription) stripeService.getStripeObjectFromWebhookEvent(event);
        //check Exists in database
        SubscriptionEntity subscriptionEntityOptional = subscriptionRepository.findByStripeSubscriptionId(subscription.getId()).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, STR$WH$0004)
        );
        //update
        updateSubscription(subscription);
    }
    @Override
    public void handleStripeSetupIntent(Event event) throws RuntimeBusinessException {
        SetupIntent setupIntent = (SetupIntent) stripeService.getStripeObjectFromWebhookEvent(event);
        //Get Customer
        String customerId = setupIntent.getCustomer();

        StripeCustomerEntity stripeCustomerEntity = stripeCustomerRepository.findByCustomerId(customerId).orElse(null);
        if(stripeCustomerEntity == null){
            logger.error("handleStripeSetupIntent: " + ORG$SUB$0006.getValue());
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$SUB$0006);
        }
        OrganizationEntity organization = stripeCustomerEntity.getOrganization();
        //Get Current Subscription Exists in database
        SubscriptionInfoDTO subscriptionInfoDTO = stripeSubscriptionServiceImpl.getSubscriptionInfo(organization);
        if(
                !subscriptionInfoDTO.isSubscribed() ||
                !subscriptionInfoDTO.getType().equals(SubscriptionMethod.STRIPE.getValue()) ||
                subscriptionInfoDTO.getSubscriptionEntityId() == null
        ){
            logger.error("handleStripeSetupIntent: " + ORG$SUB$0006.getValue());
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$SUB$0006);
        }
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findById(subscriptionInfoDTO.getSubscriptionEntityId()).get();
        //Update Customer default_payment_method
//        stripeService.updateCustomerDefaultPaymentMethod(customerId,setupIntent.getPaymentMethod());
        //Update Subscription default_payment_method
        stripeService.updateSubscriptionDefaultPaymentMethod(subscriptionEntity.getStripeSubscriptionId(),setupIntent.getPaymentMethod());
        stripeService.lastOpenInvoicePayRetry(customerId,subscriptionEntity.getStripeSubscriptionId());
    }


    private void createSubscription(Subscription subscription){
        logger.debug("Create Subscription...");
        String customerId = subscription.getCustomer();

        StripeCustomerEntity stripeCustomerEntity = stripeCustomerRepository.findByCustomerId(customerId).orElse(null);
        if(stripeCustomerEntity == null){
            logger.error("handleStripeSetupIntent: " + STR$WH$0002.getValue());
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, STR$WH$0002);
        }
        OrganizationEntity organization = stripeCustomerEntity.getOrganization();

        PackageEntity packageEntity = stripeService.getPackageByStripeSubscription(subscription);
        //Get Start Date
        Long startDateMillis = (subscription.getStartDate() != null)? subscription.getStartDate() : subscription.getTrialStart();
        Date startDate = startDateMillis == null ? new Date() : new Date(TimeUnit.SECONDS.toMillis(startDateMillis));


        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setType(SubscriptionMethod.STRIPE.getValue());
        subscriptionEntity.setPaidAmount(packageEntity.getPrice());
        subscriptionEntity.setStartDate(startDate);
        if(subscription.getStatus().equals(SubscriptionStatus.ACTIVE.getValue())){
            subscriptionEntity.setPaymentDate(startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        subscriptionEntity.setPackageEntity(packageEntity);
        subscriptionEntity.setOrganization(organization);
        subscriptionEntity.setStripeSubscriptionId(subscription.getId());
        subscriptionEntity.setStatus(subscription.getStatus());
        subscriptionRepository.save(subscriptionEntity);
        logger.debug("Subscription Created Successfully for : " + organization.getName());
    }

    public void updateSubscription(Subscription subscription){
        logger.debug("Update Subscription...");
        //Package Of the Subscription Received From Webhook
        PackageEntity subscriptionPackage = stripeService.getPackageByStripeSubscription(subscription);
        //Saved Subscription In Database
        Optional<SubscriptionEntity> savedSubscriptionEntityOptional = subscriptionRepository.findByStripeSubscriptionId(subscription.getId());
        SubscriptionEntity savedSubscriptionEntity = savedSubscriptionEntityOptional.get();
       if(
                !savedSubscriptionEntity.getStatus().equals(SubscriptionStatus.ACTIVE.getValue()) &&
                subscription.getStatus().equals(SubscriptionStatus.ACTIVE.getValue())
        ){
           //Set Payment Date as the date where the status changed to active
            savedSubscriptionEntity.setPaymentDate(LocalDateTime.now());
        }
        savedSubscriptionEntity.setPaidAmount(subscriptionPackage.getPrice());
        savedSubscriptionEntity.setPackageEntity(subscriptionPackage);;
        savedSubscriptionEntity.setStatus(subscription.getStatus());
        subscriptionRepository.save(savedSubscriptionEntity);
        logger.debug("Subscription Updated Successfully for : " + savedSubscriptionEntity.getOrganization().getName());
    }


}
