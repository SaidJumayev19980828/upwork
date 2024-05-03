package com.nasnav.dao;

import com.nasnav.persistence.CompensationRuleTierEntity;
import com.nasnav.persistence.ReceivedAwardEntity;
import com.nasnav.persistence.SubPostEntity;
import com.nasnav.persistence.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceivedAwardRepository extends JpaRepository<ReceivedAwardEntity, Long> {
    Optional<ReceivedAwardEntity> findByUserAndSubPostAndCompensationTier(UserEntity user , SubPostEntity post , CompensationRuleTierEntity tier);
}