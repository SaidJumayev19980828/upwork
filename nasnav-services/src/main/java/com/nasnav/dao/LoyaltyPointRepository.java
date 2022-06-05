package com.nasnav.dao;

import com.nasnav.persistence.LoyaltyPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoyaltyPointRepository extends JpaRepository<LoyaltyPointEntity, Long> {

    Optional<LoyaltyPointEntity> findByIdAndOrganization_Id(Long id, Long orgId);

    List<LoyaltyPointEntity> findByOrganization_IdOrderByEndDateDesc(Long orgId);

    @Query("select l from LoyaltyPointEntity l " +
            " where l.organization.id = :orgId and l.amount <= :amount " +
            " and l.endDate >= now()" +
            " order by l.points desc")
    List<LoyaltyPointEntity> findByOrganization_IdAndAmountLessThanEqual(@Param("orgId") Long orgId,
                                                                         @Param("amount") Integer amount);

    Long countByType_Id(Long typeId);

    boolean existsByIdAndOrganization_Id(Long id, Long orgId);
}
