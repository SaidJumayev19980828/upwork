package com.nasnav.dao;

import com.nasnav.persistence.ReturnShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReturnShipmentRepository extends JpaRepository<ReturnShipmentEntity, Long> {
    @Query("SELECT shp FROM ReturnShipmentEntity shp " +
            " LEFT JOIN FETCH shp.returnRequestItems items " +
            " LEFT JOIN FETCH items.returnRequest req " +
            " WHERE req.id = :id")
    List<ReturnShipmentEntity> findByReturnRequest_Id(@Param("id")Long id);
}
