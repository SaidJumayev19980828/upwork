package com.nasnav.dao;

import com.nasnav.persistence.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<ShipmentEntity, Long> {


    @Query(value = "select s from ShipmentEntity s "+
            "left join fetch s.subOrder subOrder "+
            "left join fetch subOrder.organizationEntity org "+
            "where s.shippingServiceId = :serviceId and s.externalId = :externalId and org.id = :orgId")
    Optional<ShipmentEntity> findByShippingServiceIdAndExternalIdAndOrganizationId(@Param("serviceId") String serviceId,
                                                                         @Param("externalId") String externalId,
                                                                         @Param("orgId") Long orgId);

    ShipmentEntity findBySubOrder_Id(Long subOrderId);

    
    @Query("SELECT COUNT(shipment) FROM ShipmentEntity shipment "
    		+ " LEFT JOIN shipment.subOrder ord "
    		+ " WHERE ord.userId = :userId "
    		+ " AND shipment.shippingFee = 0 "
    		+ " AND shipment.shippingServiceId = :serviceId ")
	Integer countFreeShipments(@Param("userId")Long userId, @Param("serviceId") String serviceId);
}
