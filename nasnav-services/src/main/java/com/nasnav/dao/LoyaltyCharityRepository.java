package com.nasnav.dao;

import com.nasnav.persistence.LoyaltyCharityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoyaltyCharityRepository extends JpaRepository<LoyaltyCharityEntity, Long> {

    List<LoyaltyCharityEntity> getByOrganization_Id(Long orgId);

    Optional<LoyaltyCharityEntity> getByIdAndOrganization_Id(Long Id, Long orgId);

    Boolean existsByIdAndOrganization_Id(Long Id, Long orgId);

    @Query("select (coalesce(userCharity.donationPercentage,0) * sum(coalesce(transaction.points, 0))) / 100 from LoyaltyCharityEntity charity " +
            " left join UserCharityEntity userCharity " +
            " left join UserEntity user " +
            " left join LoyaltyPointTransactionEntity transaction" +
            " where transaction.user = user and transaction.charity = charity and user.id = :userId ")
    Integer getTotalDonationByUser_Id(@Param("userId") Long userId);

    @Query("select (coalesce(userCharity.donationPercentage,0) * sum(coalesce(transaction.points, 0))) / 100 from LoyaltyCharityEntity charity " +
            " left join UserCharityEntity userCharity " +
            " left join UserEntity user " +
            " left join LoyaltyPointTransactionEntity transaction" +
            " where transaction.user = user and transaction.charity = charity")
    Integer getTotalDonation();
}
