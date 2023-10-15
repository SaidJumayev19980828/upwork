package com.nasnav.service.impl.subscription;

import com.nasnav.dao.PackageRepository;
import com.nasnav.dao.StripeCustomerRepository;
import com.nasnav.dao.SubscriptionRepository;
import com.nasnav.dto.StripeSubscriptionDTO;
import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.dto.stripe.StripeSubscriptionPendingDTO;
import com.nasnav.enumerations.SubscriptionStatus;
import com.nasnav.enumerations.SubscriptionMethod;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.PackageEntity;
import com.nasnav.persistence.StripeCustomerEntity;
import com.nasnav.persistence.SubscriptionEntity;
import com.nasnav.service.PackageService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.StripeService;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@Service
public class StripeSubscriptionServiceImpl extends SubscriptionServiceImpl{

    @Autowired
    PackageService packageService;
    @Autowired
    PackageRepository packageRepository;
    @Autowired
    private StripeService stripeService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private SecurityService securityService;

    @Autowired
    private StripeCustomerRepository stripeCustomerRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;


    private static final Logger stripeSubscriptionLogger = LogManager.getLogger("Subscription:STRIPE");



    public boolean checkOrgHasStripeCustomer(){
        //Check Stripe Customer Exist for organization
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        return stripeCustomerRepository.findByOrganization(org).isPresent();
    }



    /**
     * Used to create customer if not exist
     * @return the id of the customer
     */
    public String getOrCreateStripeCustomer(){
        String customerId = null;
        //Check Stripe Customer Exist for organization
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        if(checkOrgHasStripeCustomer()){
            StripeCustomerEntity stripeCustomerEntity = stripeCustomerRepository.findByOrganization(org).get();
            customerId = stripeCustomerEntity.getCustomerId();
        }else{
            //call Stripe Service to create customer
            if(org.getOwner() == null){
                throw new RuntimeBusinessException(NOT_FOUND, ORG$SUB$0004);
            }
            customerId = stripeService.createCustomer(org.getOwner().getName() , org.getOwner().getEmail());
        }
        return customerId;
    }


    //Capture Stripe Order
     @Override
    public SubscriptionDTO getPaymentInfo(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException {
        StripeSubscriptionDTO stripeSubscriptionDTO = (StripeSubscriptionDTO) subscriptionDTO;
        //Get Package Registered In Org
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        Long packageId = packageService.getPackageIdRegisteredInOrg(org);
        if(packageId == null){
            throw new RuntimeBusinessException(NOT_FOUND, ORG$SUB$0001);
        }
        PackageEntity packageEntity = packageRepository.findById(packageId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, ORG$SUB$0001));

        stripeSubscriptionDTO.setStripePriceId(packageEntity.getStripePriceId());
        return stripeSubscriptionDTO;
    }

    public void handleStripeSubscriptionCreated(Event event) throws RuntimeBusinessException {
        Subscription subscription = stripeService.getSubscriptionFromWebhookEvent(event);
        //check Exists in database
        Optional<SubscriptionEntity> subscriptionEntityOptional = subscriptionRepository.findByStripeSubscriptionId(subscription.getId());
        if(!subscriptionEntityOptional.isPresent()){
            //create
            createSubscription(subscription);
        }

    }

    public void handleStripeSubscriptionUpdated(Event event) throws RuntimeBusinessException {
        Subscription subscription = stripeService.getSubscriptionFromWebhookEvent(event);
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

    public void handleStripeSubscriptionDeleted(Event event) throws RuntimeBusinessException {
        Subscription subscription = stripeService.getSubscriptionFromWebhookEvent(event);
        //check Exists in database
        SubscriptionEntity subscriptionEntityOptional = subscriptionRepository.findByStripeSubscriptionId(subscription.getId()).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, STR$WH$0004)
        );
        //update
        updateSubscription(subscription);
    }
    private void createSubscription(Subscription subscription){

        String customerId = subscription.getCustomer();
        OrganizationEntity organization = stripeCustomerRepository.findByCustomerId(customerId).orElseThrow(
                ()-> new RuntimeBusinessException(NOT_FOUND, STR$WH$0002)).getOrganization();


        PackageEntity packageEntity = stripeService.getPackageByStripeSubscription(subscription);
        //Get Start Date
        Long startDateMillis = (subscription.getStartDate() != null)? subscription.getStartDate() : subscription.getTrialStart();
        Date startDate = startDateMillis == null ? new Date() : new Date(TimeUnit.SECONDS.toMillis(startDateMillis));


        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setType(SubscriptionMethod.STRIPE.getValue());
        subscriptionEntity.setPaidAmount(packageEntity.getPrice());
        subscriptionEntity.setStartDate(startDate);
        subscriptionEntity.setPackageEntity(packageEntity);
        subscriptionEntity.setOrganization(organization);
        subscriptionEntity.setStripeSubscriptionId(subscription.getId());
        subscriptionEntity.setStatus(subscription.getStatus());
        subscriptionRepository.save(subscriptionEntity);

    }

    public void updateSubscription(Subscription subscription){

        PackageEntity packageEntity = stripeService.getPackageByStripeSubscription(subscription);
        //Get Start Date
//        Long startDateMillis = (subscription.getStartDate() != null)? subscription.getStartDate() : subscription.getTrialStart();
//        Date startDate = startDateMillis == null ? new Date() : new Date(TimeUnit.SECONDS.toMillis(startDateMillis));
        Optional<SubscriptionEntity> subscriptionEntityOptional = subscriptionRepository.findByStripeSubscriptionId(subscription.getId());
        SubscriptionEntity subscriptionEntity = subscriptionEntityOptional.get();
        subscriptionEntity.setPaidAmount(packageEntity.getPrice());
//        subscriptionEntity.setStartDate(startDate);
        subscriptionEntity.setPackageEntity(packageEntity);;
        subscriptionEntity.setStatus(subscription.getStatus());
        subscriptionRepository.save(subscriptionEntity);

    }

    public SubscriptionEntity getCurrentStripeSubscription(){
        SubscriptionEntity currentSubscription = null;
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        List<SubscriptionEntity> subscriptionEntities = subscriptionRepository.findByOrganizationAndTypeAndStatusNotIn(org,SubscriptionMethod.STRIPE.getValue(),List.of(SubscriptionStatus.CANCELED.getValue(),SubscriptionStatus.INCOMPLETE_EXPIRED.getValue()));
        Optional<SubscriptionEntity> activeSubscription = subscriptionEntities.stream().filter(subscriptionEntity -> subscriptionEntity.getStatus().equals(SubscriptionStatus.ACTIVE.getValue())).findFirst();
        if(activeSubscription.isPresent()){
            currentSubscription = activeSubscription.get();
        }else{
            Optional<SubscriptionEntity> pendingSubscription = subscriptionEntities.stream().filter(subscriptionEntity -> !subscriptionEntity.getStatus().equals(SubscriptionStatus.ACTIVE.getValue())).findFirst();
            if(pendingSubscription.isPresent()){
                currentSubscription = pendingSubscription.get();
            }
        }
        return currentSubscription;
    }




    @Override
    public SubscriptionDTO subscribe(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException {
        super.subscribe(subscriptionDTO);
        StripeSubscriptionDTO stripeSubscriptionDTO = new StripeSubscriptionDTO();
        stripeSubscriptionDTO.setStripeSubscriptionPendingDTO(callStripeCreateSubscription());
        return stripeSubscriptionDTO;
    }

    private StripeSubscriptionPendingDTO callStripeCreateSubscription() throws RuntimeBusinessException {
        StripeSubscriptionDTO stripeSubscriptionDTO = (StripeSubscriptionDTO) getPaymentInfo(new StripeSubscriptionDTO());
        //Create Subscription
        StripeSubscriptionPendingDTO stripeSubscriptionPendingDTO = stripeService.createSubscription( stripeSubscriptionDTO.getStripePriceId() , getOrCreateStripeCustomer());
        return stripeSubscriptionPendingDTO;
    }



}
