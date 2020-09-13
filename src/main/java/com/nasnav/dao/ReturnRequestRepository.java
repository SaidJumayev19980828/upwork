package com.nasnav.dao;

import com.nasnav.persistence.ReturnRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequestEntity, Long> {

    @Query(value = "select returnRequest from ReturnRequestEntity returnRequest " +
            " left join fetch returnRequest.createdByEmployee emp " +
            " left join fetch returnRequest.createdByUser user " +
            " left join fetch returnRequest.metaOrder meta " +
            " left join fetch returnRequest.returnedItems item " +
            " left join fetch item.basket basket " +
            " left join fetch basket.stocksEntity stock " +
            " left join fetch stock.productVariantsEntity variant " +
            " left join fetch variant.productEntity product " +
            " left join fetch returnRequest.createdByEmployee emp " +
            " left join fetch returnRequest.createdByUser user " +
            " left join fetch returnRequest.createdByEmployee emp " +
            " left join fetch returnRequest.createdByUser user " +
            " left join fetch item.createdByUser itemUser " +
            " left join fetch item.createdByEmployee itemEmployee " +
            " where returnRequest.id = :id and meta.organization.id = :orgId")
    Optional<ReturnRequestEntity> findByReturnRequestId(@Param("id") Long id,
                                                        @Param("orgId") Long orgId);
	
	
    @Query(value = "select r from ReturnRequestEntity r where r.id = :id and r.status = :status and r.metaOrder.organization.id = :orgId")
    Optional<ReturnRequestEntity> findByIdAndOrganizationIdAndStatus(@Param("id") Long id,
                                                                     @Param("orgId") Long orgId,
                                                                     @Param("status") Integer status);
}
