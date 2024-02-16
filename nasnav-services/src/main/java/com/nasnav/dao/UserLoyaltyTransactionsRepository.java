package com.nasnav.dao;

import com.nasnav.persistence.UserLoyaltyTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserLoyaltyTransactionsRepository extends JpaRepository<UserLoyaltyTransactions, Long> {
}