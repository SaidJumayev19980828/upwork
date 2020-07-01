package com.nasnav.dao;

import com.nasnav.persistence.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShipmentRepository extends JpaRepository<ShipmentEntity, Long> {


    @Query(value = "select s from ShipmentEntity s where s.shippingServiceId = :serviceId and s.externalId = :externalId "+
            "and s.subOrder in (select o from OrdersEntity o where o.organizationEntity.id = :orgId)")
    ShipmentEntity findByShippingServiceIdAndExternalIdAndOrganizationId(@Param("serviceId") String serviceId,
                                                                         @Param("externalId") String externalId,
                                                                         @Param("orgId") Long orgId);
}
