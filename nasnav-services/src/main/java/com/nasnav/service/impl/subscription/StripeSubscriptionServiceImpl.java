package com.nasnav.service.impl.subscription;

import com.nasnav.dao.PackageRepository;
import com.nasnav.dao.StripeCustomerRepository;
import com.nasnav.dao.SubscriptionRepository;
import com.nasnav.dto.StripeSubscriptionDTO;
import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.dto.stripe.StripeConfirmDTO;
import com.nasnav.enumerations.SubscriptionMethod;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.PackageEntity;
import com.nasnav.persistence.StripeCustomerEntity;
import com.nasnav.persistence.SubscriptionEntity;
import com.nasnav.service.PackageService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.StripeService;
import com.nasnav.service.subscription.StripeSubscriptionService;
import com.nasnav.service.subscription.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import javax.transaction.Transactional;
import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@Component("stripe")
public class StripeSubscriptionServiceImpl extends SubscriptionServiceImpl implements StripeSubscriptionService , SubscriptionService {

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

    private static final Logger logger = LoggerFactory.getLogger(StripeSubscriptionServiceImpl.class);

    @Override
    public boolean checkOrgHasStripeCustomer(){
        //Check Stripe Customer Exist for organization
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        return stripeCustomerRepository.findByOrganization(org).isPresent();
    }



    @Override
    /**
     * Used to create customer if not exist
     * @return the id of the customer
     */
    @Transactional
    public String getOrCreateStripeCustomer(){
        StripeCustomerEntity stripeCustomerEntity = null;
        //Check Stripe Customer Exist for organization
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        if(checkOrgHasStripeCustomer()){
            stripeCustomerEntity = stripeCustomerRepository.findByOrganization(org).get();
        }else{
            //call Stripe Service to create customer then save it
            if(org.getOwner() == null){
                throw new RuntimeBusinessException(NOT_FOUND, ORG$SUB$0004);
            }
            String customerId = stripeService.createCustomer(org.getOwner().getName() , org.getOwner().getEmail());
            stripeCustomerEntity = new StripeCustomerEntity();
            stripeCustomerEntity.setCustomerId(customerId);
            stripeCustomerEntity.setOrganization(org);
            stripeCustomerEntity = stripeCustomerRepository.save(stripeCustomerEntity);
        }
        return stripeCustomerEntity.getCustomerId();

    }


    //Capture Stripe Order
     @Override
    public SubscriptionDTO getPaymentInfo(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException {
        StripeSubscriptionDTO stripeSubscriptionDTO = (StripeSubscriptionDTO) subscriptionDTO;
        //Get Package Registered In Org
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        Long packageId = packageService.getPackageIdRegisteredInOrg(org);
        if(packageId == null){
            logger.error("Failed To GetPaymentInfo : Package Id is null");
            throw new RuntimeBusinessException(NOT_FOUND, ORG$SUB$0001);
        }
        PackageEntity packageEntity = packageRepository.findById(packageId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, ORG$SUB$0001));
        stripeSubscriptionDTO.setPackageId(packageId);
        stripeSubscriptionDTO.setStripePriceId(packageEntity.getStripePriceId());
        return stripeSubscriptionDTO;
    }

    @Override
    public SubscriptionDTO subscribe(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException {
        super.subscribe(subscriptionDTO);
        StripeSubscriptionDTO stripeSubscriptionDTO = new StripeSubscriptionDTO();
        stripeSubscriptionDTO.setStripeConfirmDTO(callStripeCreateSubscription());
        return stripeSubscriptionDTO;
    }

    private StripeConfirmDTO callStripeCreateSubscription() throws RuntimeBusinessException {
        StripeConfirmDTO stripeConfirmDTO = null;
        try{
            StripeSubscriptionDTO stripeSubscriptionDTO = (StripeSubscriptionDTO) getPaymentInfo(new StripeSubscriptionDTO());
            //Create Subscription
            stripeConfirmDTO = stripeService.createSubscription( stripeSubscriptionDTO.getStripePriceId() , getOrCreateStripeCustomer());
        }catch (RuntimeBusinessException e){
            throw e;
        } catch (Exception e) {
            logger.error("callStripeCreateSubscription : Exception" + e.getMessage());
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, STR$CAL$0004);
        }
        return stripeConfirmDTO;
    }

    /**
     * call setup intent to allow org add payment method in stripe then update payment method of subscription
     * using webhook
    **/
    @Override
    public StripeConfirmDTO setupIntent() throws RuntimeBusinessException {

        SubscriptionInfoDTO subscriptionInfoDTO = getSubscriptionInfo();
        if(!subscriptionInfoDTO.isSubscribed()){
            logger.error("Setup Intent : " + ORG$SUB$0006);
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$SUB$0006);
        }
        StripeConfirmDTO stripeConfirmDTO = stripeService.setupIntent(getOrCreateStripeCustomer());
        return stripeConfirmDTO;
    }


    @Override
    public void cancelSubscription() throws RuntimeBusinessException {
        SubscriptionInfoDTO subscriptionInfoDTO = getSubscriptionInfo();
        if(
                !subscriptionInfoDTO.isSubscribed() ||
                !subscriptionInfoDTO.getType().equals(SubscriptionMethod.STRIPE.getValue()) ||
                subscriptionInfoDTO.getSubscriptionEntityId() == null
        ){
            logger.error("Cancel Subscription : " + ORG$SUB$0006);
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$SUB$0006);
        }
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findById(subscriptionInfoDTO.getSubscriptionEntityId()).get();
        stripeService.cancelSubscription(subscriptionEntity.getStripeSubscriptionId());
    }

    @Override
    public void changePlan() throws RuntimeBusinessException {
        SubscriptionInfoDTO subscriptionInfoDTO = getSubscriptionInfo();
        if(
                !subscriptionInfoDTO.isSubscribed() ||
                        !subscriptionInfoDTO.getType().equals(SubscriptionMethod.STRIPE.getValue()) ||
                        subscriptionInfoDTO.getSubscriptionEntityId() == null
        ){
            logger.error("Change Plan : " + ORG$SUB$0006);
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$SUB$0006);
        }
        StripeSubscriptionDTO stripeSubscriptionDTO = (StripeSubscriptionDTO) getPaymentInfo(new StripeSubscriptionDTO());

        if(stripeSubscriptionDTO.getPackageId().equals(subscriptionInfoDTO.getPackageId())){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$SUB$0008,stripeSubscriptionDTO.getPackageId());
        }
        PackageEntity packageEntity = packageRepository.findById(stripeSubscriptionDTO.getPackageId()).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, PA$USR$0002));

        SubscriptionEntity subscriptionEntity = subscriptionRepository.findById(subscriptionInfoDTO.getSubscriptionEntityId()).get();
        stripeService.changePlan(subscriptionEntity.getStripeSubscriptionId(),packageEntity.getStripePriceId());
    }



}
