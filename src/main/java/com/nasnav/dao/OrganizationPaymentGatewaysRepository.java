package com.nasnav.dao;

import com.nasnav.persistence.OrganizationDomainsEntity;
import com.nasnav.persistence.OrganizationPaymentGatewaysEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrganizationPaymentGatewaysRepository extends JpaRepository<OrganizationPaymentGatewaysEntity, Integer> {

    List<OrganizationPaymentGatewaysEntity> findAllByOrganizationId(long orgId);
    List<OrganizationPaymentGatewaysEntity> findAllByOrganizationIdIsNull();
    Optional<OrganizationPaymentGatewaysEntity> findByOrganizationIdAndGateway(long orgId, String gateway);

    @Query(value = "SELECT * FROM organization_payments op WHERE op.organization_id IS null AND gateway = :gateway", nativeQuery = true)
    Optional<OrganizationPaymentGatewaysEntity> getDefaultGateway(@Param("gateway") String gateway);
}
