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

    



}
