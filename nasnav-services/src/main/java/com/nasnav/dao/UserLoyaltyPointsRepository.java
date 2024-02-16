package com.nasnav.dao;

import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.UserLoyaltyPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLoyaltyPointsRepository extends JpaRepository<UserLoyaltyPoints, Long> {

    Optional<UserLoyaltyPoints> findByUser(UserEntity userId);
}