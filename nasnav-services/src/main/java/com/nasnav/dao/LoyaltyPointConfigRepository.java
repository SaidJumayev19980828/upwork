package com.nasnav.dao;

import com.nasnav.persistence.LoyaltyPointConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoyaltyPointConfigRepository extends JpaRepository<LoyaltyPointConfigEntity, Long> {

    boolean existsByIdAndOrganization_Id(Long id, Long orgId);

    Optional<LoyaltyPointConfigEntity> findByIdAndOrganization_Id(Long id, Long orgId);

    List<LoyaltyPointConfigEntity> findByOrganization_IdOrderByCreatedAtDesc(Long orgId);

    @Query("select config from LoyaltyPointConfigEntity config "
            +" where config.organization.id = :orgId and config.shop.id = :shopId "
            +" and config.amountFrom <= :amount and config.amountTo >= :amount and config.isActive = true"
            +" order by config.points desc")
    List<LoyaltyPointConfigEntity> findByOrganizationIdAndShopIdAndAmount(@Param("orgId") Long orgId,
                                                                          @Param("shopId") Long shopId,
                                                                          @Param("amount") Integer amount);
}
