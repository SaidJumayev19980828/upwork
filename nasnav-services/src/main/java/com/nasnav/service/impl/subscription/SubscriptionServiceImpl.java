package com.nasnav.service.impl.subscription;

import com.nasnav.dao.PackageRepository;
import com.nasnav.dao.SubscriptionRepository;
import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.dto.request.PackageRegisteredByUserDTO;
import com.nasnav.dto.stripe.StripeSubscriptionPendingDTO;
import com.nasnav.enumerations.SubscriptionMethod;
import com.nasnav.enumerations.SubscriptionStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.service.PackageService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.subscription.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public abstract class SubscriptionServiceImpl implements SubscriptionService {


    @Autowired
    private SecurityService securityService;

    @Autowired
    private PackageService packageService;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;



    @Transactional
    public SubscriptionInfoDTO getSubscriptionInfo() throws RuntimeBusinessException{
        SubscriptionInfoDTO subscriptionInfoDTO = new SubscriptionInfoDTO();
        subscriptionInfoDTO.setSubscribed(false);
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        List<SubscriptionEntity> subscriptionEntityList = subscriptionRepository.
                findByOrganizationAndStatusNotIn(org,
                        List.of(
                                SubscriptionStatus.CANCELED.getValue(),
                                SubscriptionStatus.INCOMPLETE_EXPIRED.getValue()
                        )
                );
        for(SubscriptionEntity subscriptionEntity : subscriptionEntityList ){
            if(subscriptionEntity.getExpirationDate() == null ||
                    subscriptionEntity.getExpirationDate().after(new Date())){
                //Hasn't expiration date or Not Expired
                subscriptionInfoDTO.setSubscribed(true);
                subscriptionInfoDTO.setType(subscriptionEntity.getType());
                subscriptionInfoDTO.setExpirationDate(subscriptionEntity.getExpirationDate());
                subscriptionInfoDTO.setStatus(subscriptionEntity.getStatus());
            }else{
                subscriptionEntity.setStatus("canceled");
                subscriptionRepository.save(subscriptionEntity);
            }
        }
        return subscriptionInfoDTO;
    }


    @Override
    public SubscriptionDTO subscribe(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException {
        SubscriptionInfoDTO subscriptionInfoDTO = getSubscriptionInfo();
        if(subscriptionInfoDTO.isSubscribed()){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,ORG$SUB$0005);
        }
        return subscriptionDTO;
    }



    protected SubscriptionDTO savePackageSuccessfulSubscription(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException {

        //Get Package Registered In Org
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        Long packageId = packageService.getPackageIdRegisteredInOrg(org);
        if(packageId == null){
            throw new RuntimeBusinessException(NOT_FOUND, ORG$SUB$0001);
        }

        //Save Organization Subscribed In Package
        PackageEntity packageEntity = packageRepository.findById(packageId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, PA$USR$0002, packageId));

        LocalDate startDate = LocalDate.now();
        LocalDate expirationDate = startDate.plusDays(packageEntity.getPeriodInDays());

        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setType(subscriptionDTO.getType());
        subscriptionEntity.setPaidAmount(subscriptionDTO.getPaidAmount());
        subscriptionEntity.setStartDate(java.sql.Date.valueOf(startDate));
        subscriptionEntity.setExpirationDate(java.sql.Date.valueOf(expirationDate));
        subscriptionEntity.setPackageEntity(packageEntity);
        subscriptionEntity.setOrganization(org);
        subscriptionEntity.setStatus(SubscriptionStatus.ACTIVE.getValue());
        subscriptionDTO.setId(subscriptionRepository.save(subscriptionEntity).getId());
        return subscriptionDTO;
    }

}
