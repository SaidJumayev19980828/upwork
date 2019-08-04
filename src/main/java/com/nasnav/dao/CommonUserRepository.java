package com.nasnav.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nasnav.persistence.BaseUserEntity;


@Repository
public interface CommonUserRepository{
	Optional<? extends BaseUserEntity> findByAuthenticationToken(String authToken);
}
