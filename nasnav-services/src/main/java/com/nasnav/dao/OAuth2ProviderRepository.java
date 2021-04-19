package com.nasnav.dao;

import com.nasnav.persistence.OAuth2ProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuth2ProviderRepository extends JpaRepository<OAuth2ProviderEntity, Long> {

	Optional<OAuth2ProviderEntity> findByProviderNameIgnoreCase(String provider);
	boolean existsByProviderNameIgnoreCase(String provider);

}
