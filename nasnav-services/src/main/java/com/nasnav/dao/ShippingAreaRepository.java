package com.nasnav.dao;

import com.nasnav.persistence.ShippingAreaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShippingAreaRepository extends JpaRepository<ShippingAreaEntity, Integer> {

    @Query("select sh.providerId from ShippingAreaEntity sh where sh.area.id = :areaId and sh.shippingService.id = :serviceId")
    Optional<String> getProviderId(@Param("serviceId") String serviceId,
                                   @Param("areaId") Long areaId);
}
