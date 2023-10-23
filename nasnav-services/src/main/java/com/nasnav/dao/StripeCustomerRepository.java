package com.nasnav.dao;

import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.StripeCustomerEntity;
import com.nasnav.persistence.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StripeCustomerRepository extends JpaRepository<StripeCustomerEntity, Long> {

    Optional<StripeCustomerEntity> findByOrganization(OrganizationEntity organization);
    Optional<StripeCustomerEntity> findByCustomerId(String customerId);

}
