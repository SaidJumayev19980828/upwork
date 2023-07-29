package com.nasnav.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.RocketChatCustomerTokenEntity;
import com.nasnav.persistence.UserEntity;
import java.util.Optional;

public interface RocketChatCustomerTokenRepository extends JpaRepository<RocketChatCustomerTokenEntity, Long> {
	public Optional<RocketChatCustomerTokenEntity> findByUser(UserEntity user);
}