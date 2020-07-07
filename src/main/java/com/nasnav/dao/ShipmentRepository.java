package com.nasnav.dao;

import com.nasnav.persistence.ShipmentEntity;
import com.nasnav.shipping.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShipmentRepository extends JpaRepository<ShipmentEntity, Long> {


    @Query(value = "select s from ShipmentEntity s "+
            "left join s.subOrder subOrder "+
            "left join subOrder.organizationEntity org "+
            "where s.shippingServiceId = :serviceId and s.externalId = :externalId and org.id = :orgId")
    ShipmentEntity findByShippingServiceIdAndExternalIdAndOrganizationId(@Param("serviceId") String serviceId,
                                                                         @Param("externalId") String externalId,
                                                                         @Param("orgId") Long orgId);

    ShipmentEntity findBySubOrder_Id(Long subOrderId);
}
