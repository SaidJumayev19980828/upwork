package com.nasnav.dao;

import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    Optional<SubscriptionEntity> findFirstByOrganizationOrderByExpirationDateDesc(OrganizationEntity organization);
    List<SubscriptionEntity> findByOrganizationAndStatusNotIn(OrganizationEntity organization,List<String> excludedStatues);

    Optional<SubscriptionEntity> findByStripeSubscriptionId(String stripeSubscriptionId);

    List<SubscriptionEntity> findByOrganizationAndTypeAndStatusNotIn(OrganizationEntity organizationEntity,String type,List<String> excludedStatues);

    List<SubscriptionEntity> findByPackageEntity_IdAndStatusNotIn(Long packageEntityId, Collection<String> excludedStatues);
}
