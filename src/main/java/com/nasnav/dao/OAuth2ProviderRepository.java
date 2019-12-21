package com.nasnav.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.OAuth2ProviderEntity;

public interface OAuth2ProviderRepository extends JpaRepository<OAuth2ProviderEntity, Long> {

	Optional<OAuth2ProviderEntity> findByProviderNameIgnoreCase(String provider);

}
