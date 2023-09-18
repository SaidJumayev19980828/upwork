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

import javax.transaction.Transactional;
import java.util.Date;

import static com.nasnav.exceptions.ErrorCodes.PR$Org$0001;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PackageRegisteredRepository packageRegisteredRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public Long completeSubscription(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException {

        OrganizationEntity org = securityService.getCurrentUserOrganization();
        //Get Package that the org selected before
        PackageEntity packageEntity = packageRepository.findPackageByPackageRegisteredOrganization(org).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, PR$Org$0001, org.getId())
        );

        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setType(subscriptionDTO.getType());
        subscriptionEntity.setPaidAmount(subscriptionDTO.getPaidAmount());
        subscriptionEntity.setStartDate(java.sql.Date.valueOf(subscriptionDTO.getStartDate()));
        subscriptionEntity.setExpirationDate(java.sql.Date.valueOf(subscriptionDTO.getStartDate().plusDays(packageEntity.getPeriod())));
        subscriptionEntity.setPackageEntity(packageEntity);
        subscriptionEntity.setOrganization(org);
        return subscriptionRepository.save(subscriptionEntity).getId();
    }
}
