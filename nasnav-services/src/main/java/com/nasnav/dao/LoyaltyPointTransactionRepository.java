package com.nasnav.dao;

import com.nasnav.persistence.LoyaltyPointTransactionEntity;
import com.nasnav.persistence.dto.query.result.OrganizationPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface LoyaltyPointTransactionRepository extends JpaRepository<LoyaltyPointTransactionEntity, Long> {

    Long countByLoyaltyPoint_Id(Long id);

    List<LoyaltyPointTransactionEntity> findByUser_IdAndOrganization_Id(Long userId, Long orgId); // used for listing transactions only

    @Query("select sum(t.points) from LoyaltyPointTransactionEntity t " +
            " where t.isValid = true and t.shop.allowOtherPoints = true and t.user.id = :userId" +
            " and (t.endDate is null or t.endDate <= now())")
    Integer findAllRedeemablePoints(@Param("userId") Long userId);

    @Query("select new com.nasnav.persistence.dto.query.result.OrganizationPoints(o.id, o.name, image.uri, sum(t.points))" +
            " from LoyaltyPointTransactionEntity t" +
            " inner join t.organization o" +
            " left join o.images image" +
            " where t.isValid = true and t.organization.id = t.user.id" +
            " and (t.endDate is null or t.endDate <= now())" +
            " and t.user.id in (select u.id from UserEntity u where u.yeshteryUserId = :yeshteryUserId)" +
            " and image.type = 1 and image.shopsEntity is null " +
            " group by o.id, image.uri")
    List<OrganizationPoints> findRedeemablePointsPerOrg(@Param("yeshteryUserId") Long yeshteryUserId);

    @Query("select COALESCE(sum(t.points), 0) from LoyaltyPointTransactionEntity t" +
            " where t.isValid = true and t.organization.id = :orgId and t.user.id = :userId" +
            " and (t.endDate is null or t.endDate <= now())")
    Integer findOrgRedeemablePoints(@Param("userId") Long userId,
                                    @Param("orgId") Long orgId);

    @Query("select COALESCE(sum(t.points), 0) from LoyaltyPointTransactionEntity t INNER JOIN UserEntity u " +
            " on u = t.user where t.isValid = true and t.organization.id = :orgId and u.yeshteryUserId = :yeshteryUserId" +
            " and (t.endDate is null or t.endDate <= now())")
    Integer findOrgRedeemablePointsByOrgAndYeshteryUserId(@Param("yeshteryUserId") Long yeshteryUserId,
                                                          @Param("orgId") Long orgId);
    @Query("select t from LoyaltyPointTransactionEntity t where t.order.id in :orderIds or t.metaOrder.id in :metaOrderIds")
    List<LoyaltyPointTransactionEntity> findByOrderIdInOrYeshteryMetaOrderIdIn(@Param("orderIds") Set<Long> orderIds,
                                                                               @Param("metaOrderIds") Set<Long> metaOrderIds);

    List<LoyaltyPointTransactionEntity> getByCharity_Id(Long charityId);

    @Query("Select count(transaction) from LoyaltyPointTransactionEntity transaction " +
            " where transaction.user.id = :userId and DATE(transaction.createdAt) BETWEEN :dateFrom and :dateTo")
    Integer getCoinsDropTransactionsByUser_IdAndCreatedAt(Long userId, LocalDate dateFrom, LocalDate dateTo);

    @Query("select t from LoyaltyPointTransactionEntity t where t.order.id = :orderId and (t.endDate is null or t.endDate <= now())")
    List<LoyaltyPointTransactionEntity> findByOrder_Id(@Param("orderId") Long orderId);
}
