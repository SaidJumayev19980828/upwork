package com.nasnav.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.OAuth2UserEntity;

public interface OAuth2UserRepository extends JpaRepository<OAuth2UserEntity, Long> {

	Optional<OAuth2UserEntity> findByoAuth2IdAndOrganizationId(String oAuth2Id, Long orgId);

	boolean existsByLoginToken(String socialLoginToken);

	Optional<OAuth2UserEntity> findByLoginToken(String socialLoginToken);

}
