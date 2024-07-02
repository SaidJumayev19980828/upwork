package com.nasnav.service.impl.subscription;

import com.nasnav.dao.SubscriptionRepository;
import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.enumerations.SubscriptionStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.service.PackageService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.subscription.SubscriptionService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public abstract class SubscriptionServiceImpl implements SubscriptionService {

    private final SecurityService securityService;
    private final PackageService packageService;
    private final SubscriptionRepository subscriptionRepository;

    protected SubscriptionServiceImpl(SecurityService securityService, PackageService packageService,
                                    SubscriptionRepository subscriptionRepository) {
        this.securityService = securityService;
        this.packageService = packageService;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public SubscriptionInfoDTO getSubscriptionInfo() throws RuntimeBusinessException{
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        return getSubscriptionInfo(org);
    }

    @Transactional
    public SubscriptionInfoDTO getSubscriptionInfo(OrganizationEntity org) throws RuntimeBusinessException{
        SubscriptionInfoDTO subscriptionInfoDTO = new SubscriptionInfoDTO();
        subscriptionInfoDTO.setSubscribed(false);
        List<SubscriptionEntity> subscriptionEntityList = subscriptionRepository.
                findByOrganizationAndStatusNotIn(org,
                        List.of(
                                SubscriptionStatus.CANCELED.getValue(),
                                SubscriptionStatus.INCOMPLETE_EXPIRED.getValue()
                        )
                );
        subscriptionInfoDTO.setOrganizationId(org.getId());
        subscriptionInfoDTO.setOrganizationName(org.getName());
        for(SubscriptionEntity subscriptionEntity : subscriptionEntityList ){
            if(subscriptionEntity.getExpirationDate() == null ||
                    subscriptionEntity.getExpirationDate().after(new Date())){
                //Hasn't expiration date or Not Expired
                subscriptionInfoDTO.setSubscribed(true);
                subscriptionInfoDTO.setType(subscriptionEntity.getType());
                subscriptionInfoDTO.setExpirationDate(subscriptionEntity.getExpirationDate());
                subscriptionInfoDTO.setStatus(subscriptionEntity.getStatus());
                subscriptionInfoDTO.setSubscriptionEntityId(subscriptionEntity.getId());
                subscriptionInfoDTO.setPackageId(subscriptionEntity.getPackageEntity().getId());
            }else{
                subscriptionEntity.setStatus(SubscriptionStatus.CANCELED.getValue());
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
        PackageEntity packageEntity = packageService.getPackageRegisteredInOrg(org);
        if (packageEntity == null){
            throw new RuntimeBusinessException(NOT_FOUND, ORG$SUB$0001);
        }
        LocalDate startDate = LocalDate.now();
        LocalDate expirationDate = startDate.plusDays(packageEntity.getPeriodInDays());

        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setType(subscriptionDTO.getType());
        subscriptionEntity.setPaidAmount(subscriptionDTO.getPaidAmount());
        subscriptionEntity.setPaymentDate(LocalDateTime.now());
        subscriptionEntity.setStartDate(java.sql.Date.valueOf(startDate));
        subscriptionEntity.setExpirationDate(java.sql.Date.valueOf(expirationDate));
        subscriptionEntity.setPackageEntity(packageEntity);
        subscriptionEntity.setOrganization(org);
        subscriptionEntity.setStatus(SubscriptionStatus.ACTIVE.getValue());
        subscriptionDTO.setId(subscriptionRepository.save(subscriptionEntity).getId());
        return subscriptionDTO;
    }

    @Override
    public List<SubscriptionInfoDTO> getSubscriptionsByPackage(Long packageId) {
        List<SubscriptionEntity> subscriptions = subscriptionRepository.findByPackageEntity_IdAndStatusNotIn(packageId, List.of(SubscriptionStatus.CANCELED.getValue(),
                SubscriptionStatus.INCOMPLETE_EXPIRED.getValue()));
        return subscriptions.stream().map(subscriptionEntity -> {
            SubscriptionInfoDTO subscriptionInfoDTO = new SubscriptionInfoDTO();
            subscriptionInfoDTO.setExpirationDate(subscriptionEntity.getExpirationDate());
            subscriptionInfoDTO.setSubscribed(true);
            subscriptionInfoDTO.setPackageId(subscriptionEntity.getPackageEntity().getId());
            subscriptionInfoDTO.setSubscriptionEntityId(subscriptionEntity.getId());
            subscriptionInfoDTO.setStatus(subscriptionEntity.getStatus());
            subscriptionInfoDTO.setType(subscriptionEntity.getType());
            subscriptionInfoDTO.setOrganizationId(subscriptionEntity.getOrganization().getId());
            subscriptionInfoDTO.setOrganizationName(subscriptionEntity.getOrganization().getName());
            return subscriptionInfoDTO;
        }).toList();
    }
}
