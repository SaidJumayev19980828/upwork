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
import java.util.Optional;
import java.util.Set;

public interface LoyaltyPointTransactionRepository extends JpaRepository<LoyaltyPointTransactionEntity, Long> {

    List<LoyaltyPointTransactionEntity> findByUser_IdAndOrganization_Id(Long userId, Long orgId); // used for listing transactions only

    List<LoyaltyPointTransactionEntity> findByUser_IdAndOrganization_IdAndType(Long userId, Long orgId, Integer type);

    List<LoyaltyPointTransactionEntity> findByOrganization_IdIn(List<Long> orgIds); // used for testing only

    @Query("select t from LoyaltyPointTransactionEntity t " +
            " left join LoyaltySpentTransactionEntity s on s.transaction = t" +
            " left join s.reverseTransaction r" +
            " where t.isValid = true and t.id in :ids and t.user.id = :userId and t.organization.id = :orgId and t.type < 100" +
            " and (r.id is null or t.points > r.points)" +
            " and t.startDate <= now()" +
            " and (t.endDate is null or t.endDate >= now())" +
            " order by t.endDate asc ")
    List<LoyaltyPointTransactionEntity> getTransactionsByIdInAndUserIdAndOrgId(@Param("ids") Set<Long> ids,
                                                                               @Param("userId") Long userId,
                                                                               @Param("orgId") Long orgId);

    @Query("select t from LoyaltyPointTransactionEntity t " +
            " left join LoyaltySpentTransactionEntity s on s.transaction = t" +
            " left join s.reverseTransaction r" +
            " where t.isValid = true and t.user.yeshteryUserId = :yeshteryUserId and t.organization.id in :orgIds and t.type < 100" +
            " and (r.id is null or t.points > r.points)" +
            " and t.startDate <= now()" +
            " and (t.endDate is null or t.endDate >= now())" +
            " order by t.endDate desc")
    List<LoyaltyPointTransactionEntity> getSpendablePointsByUserIdAndOrgIds(@Param("yeshteryUserId") Long yeshteryUserId,
                                                                            @Param("orgIds") List<Long> orgId);

    @Query("select sum(t.points) from LoyaltyPointTransactionEntity t " +
            " left join LoyaltySpentTransactionEntity s on s.transaction = t" +
            " left join s.reverseTransaction r" +
            " where t.isValid = true and t.shop.allowOtherPoints = true and t.user.id = :userId" +
            " and (r.id is null or t.points > r.points)" +
            " and (t.order is not null or t.metaOrder is not null) "+
            " and t.startDate <= now()" +
            " and (t.endDate is null or t.endDate >= now())")
    Integer findAllRedeemablePoints(@Param("userId") Long userId);

    @Query("select new com.nasnav.persistence.dto.query.result.OrganizationPoints(o.id, o.name, image.uri, sum(t.points))" +
            " from LoyaltyPointTransactionEntity t" +
            " inner join t.organization o" +
            " left join o.images image" +
            " left join LoyaltySpentTransactionEntity s on s.transaction = t" +
            " left join s.reverseTransaction r" +
            " where t.isValid = true and t.organization.id = t.user.organizationId and t.type < 100" +
            " and (r.id is null or t.points > r.points)" +
            " and t.startDate <= now()" +
            " and (t.endDate is null or t.endDate >= now())" +
            " and t.user.id in (select u.id from UserEntity u where u.yeshteryUserId = :yeshteryUserId)" +
            " and image.type = 1 and image.shopsEntity is null " +
            " group by o.id, image.uri")
    List<OrganizationPoints> findRedeemablePointsPerOrg(@Param("yeshteryUserId") Long yeshteryUserId);

    @Query("select COALESCE(sum(t.points), 0) from LoyaltyPointTransactionEntity t" +
            " left join LoyaltySpentTransactionEntity s on s.transaction = t" +
            " left join s.reverseTransaction r" +
            " where t.isValid = true and t.organization.id = :orgId and t.user.id = :userId and t.type < 100" +
            " and (r.id is null or t.points > r.points)" +
            " and t.startDate <= now()" +
            " and (t.endDate is null or t.endDate >= now())")
    Integer findOrgRedeemablePoints(@Param("userId") Long userId,
                                    @Param("orgId") Long orgId);
    @Query("select t from LoyaltyPointTransactionEntity t where t.order.id in :orderIds or t.metaOrder.id in :metaOrderIds")
    List<LoyaltyPointTransactionEntity> findByOrderIdInOrYeshteryMetaOrderIdIn(@Param("orderIds") Set<Long> orderIds,
                                                                               @Param("metaOrderIds") Set<Long> metaOrderIds);

    List<LoyaltyPointTransactionEntity> getByCharity_Id(Long charityId);

    @Query("Select count(transaction) from LoyaltyPointTransactionEntity transaction " +
            " where transaction.user.id = :userId and DATE(transaction.createdAt) BETWEEN :dateFrom and :dateTo")
    Integer getCoinsDropTransactionsByUser_IdAndCreatedAt(Long userId, LocalDate dateFrom, LocalDate dateTo);

    @Query("select t from LoyaltyPointTransactionEntity t where t.order.id = :orderId and (t.endDate is null or t.endDate >= now())")
    Optional<LoyaltyPointTransactionEntity> findByOrder_Id(@Param("orderId") Long orderId);

    @Transactional
    @Modifying
    @Query("update LoyaltyPointTransactionEntity t set t.isValid = true " +
            "where t.type = 0 and t.user.id = :userId and now() > t.startDate and (t.endDate is null or now() < t.endDate)")
    void setTransactionAsValidByUserId(@Param("userId") Long userId);

    @Query("select t from LoyaltyPointTransactionEntity t " +
            " left join LoyaltySpentTransactionEntity s on s.transaction = t" +
            " left join s.reverseTransaction r" +
            " where t.isValid = true and t.user.yeshteryUserId = :yeshteryUserId and t.organization.id = :orgId and t.type < 100" +
            " and (r.id is null or t.points > r.points)" +
            " and t.startDate <= now()" +
            " and (t.endDate is null or t.endDate >= now())" +
            " order by t.endDate desc")
    List<LoyaltyPointTransactionEntity> getSpendablePointsByUserIdAndOrgId(@Param("yeshteryUserId") Long yeshteryUserId, @Param("orgId") Long orgId);
}
