package com.nasnav.dao;

import com.nasnav.persistence.LoyaltyPointTransactionEntity;
import com.nasnav.persistence.UserLoyaltyTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserLoyaltyTransactionsRepository extends JpaRepository<UserLoyaltyTransactions, Long> {
    @Query("""
            SELECT ult  FROM UserLoyaltyTransactions ult
            JOIN ult.userLoyaltyPoints ul
            WHERE ul.user.id = :userId AND ult.organization.id = :orgId
            """)
    List<UserLoyaltyTransactions> findByUser_IdAndOrganization_Id(Long userId, Long orgId);
}