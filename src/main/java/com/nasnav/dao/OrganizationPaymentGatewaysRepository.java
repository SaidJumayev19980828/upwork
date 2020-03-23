package com.nasnav.dao;

import com.nasnav.persistence.OrganizationDomainsEntity;
import com.nasnav.persistence.OrganizationPaymentGatewaysEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationPaymentGatewaysRepository extends JpaRepository<OrganizationPaymentGatewaysEntity, Integer> {

    List<OrganizationPaymentGatewaysEntity> findAllByOrganizationId(long orgId);
    List<OrganizationPaymentGatewaysEntity> findAllByOrganizationIdIsNull();
    Optional<OrganizationPaymentGatewaysEntity> findByOrganizationIdAndGateway(long orgId, String gateway);

}
