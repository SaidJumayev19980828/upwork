package com.nasnav.dao;

import com.nasnav.dto.Pair;
import com.nasnav.persistence.ReturnRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequestEntity, Long> {

    @Query(value = "select returnRequest from ReturnRequestEntity returnRequest " +
            " left join fetch returnRequest.createdByEmployee emp " +
            " left join fetch returnRequest.createdByUser user " +
            " left join fetch returnRequest.metaOrder meta " +
            " left join fetch meta.organization org " +
            " left join fetch meta.user customer " +
            " left join fetch returnRequest.returnedItems item " +
            " left join fetch item.returnShipment returnShp " +
            " left join fetch item.basket basket " +
            " left join fetch basket.ordersEntity subOrder " +
            " left join fetch basket.stocksEntity stock " +
            " left join fetch subOrder.shopsEntity subOrderShop " +
            " left join fetch subOrder.addressEntity subOrderAddr " +
            " left join fetch stock.productVariantsEntity variant " +
            " left join fetch variant.productEntity product " +
            " left join fetch item.createdByUser itemUser " +
            " left join fetch item.createdByEmployee itemEmployee " +
            " where returnRequest.id = :id " +
            " and (org.id = :orgId or user.organizationId = :orgId)")
    Optional<ReturnRequestEntity> findByReturnRequestId(@Param("id") Long id,
                                                        @Param("orgId") Long orgId);
	
	
    @Query(value = "select r from ReturnRequestEntity r " +
            " where r.id = :id " +
            " and r.status = :status " +
            " and r.metaOrder.organization.id = :orgId")
    Optional<ReturnRequestEntity> findByIdAndOrganizationIdAndStatus(@Param("id") Long id,
                                                                     @Param("orgId") Long orgId,
                                                                     @Param("status") Integer status);

    @Query(value = "select r from ReturnRequestEntity r " +
            " where r.id = :id " +
            " and r.metaOrder.organization.id = :orgId")
    Optional<ReturnRequestEntity> findByIdAndOrganizationId(@Param("id") Long id,
                                                            @Param("orgId") Long orgId);

    @Query(value = "SELECT NEW com.nasnav.dto.Pair(r.id, count(items))" +
            " from ReturnRequestEntity r left join r.returnedItems items" +
            " group by r.id having r.id in :ids ")
    List<Pair> getReturnRequestsItemsCount(@Param("ids") Set<Long> ids);
}
