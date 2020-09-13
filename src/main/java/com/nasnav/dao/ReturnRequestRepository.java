package com.nasnav.dao;

import com.nasnav.persistence.ReturnRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequestEntity, Long> {


    @Query(value = "select r from ReturnRequestEntity r where r.id = :id and r.status = :status and r.metaOrder.organization.id = :orgId")
    Optional<ReturnRequestEntity> findByIdAndOrganizationIdAndStatus(@Param("id") Long id,
                                                                     @Param("orgId") Long orgId,
                                                                     @Param("status") Integer status);
}
