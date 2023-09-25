package com.nasnav.service.impl.subscription;

import com.nasnav.dao.PackageRegisteredRepository;
import com.nasnav.dao.PackageRepository;
import com.nasnav.dao.SubscriptionRepository;
import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.service.SecurityService;
import com.nasnav.service.subscription.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static com.nasnav.exceptions.ErrorCodes.PA$USR$0002;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public abstract class SubscriptionServiceImpl implements SubscriptionService {


    @Autowired
    private SecurityService securityService;

    @Autowired
    private PackageRegisteredRepository packageRegisteredRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;



    protected SubscriptionDTO savePackageSuccessfulSubscription(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException {

        OrganizationEntity org = securityService.getCurrentUserOrganization();
        PackageEntity packageEntity = packageRepository.findById(subscriptionDTO.getPackageId()).orElseThrow(
                () -> new RuntimeBusinessException(NOT_ACCEPTABLE, PA$USR$0002, subscriptionDTO.getPackageId()));

        LocalDate startDate = LocalDate.now();
        LocalDate expirationDate = startDate.plusDays(packageEntity.getPeriodInDays());

        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setType(subscriptionDTO.getType());
        subscriptionEntity.setPaidAmount(subscriptionDTO.getPaidAmount());
        subscriptionEntity.setStartDate(java.sql.Date.valueOf(startDate));
        subscriptionEntity.setExpirationDate(java.sql.Date.valueOf(expirationDate));
        subscriptionEntity.setPackageEntity(packageEntity);
        subscriptionEntity.setOrganization(org);
        subscriptionDTO.setId(subscriptionRepository.save(subscriptionEntity).getId());
        return subscriptionDTO;
    }
}
