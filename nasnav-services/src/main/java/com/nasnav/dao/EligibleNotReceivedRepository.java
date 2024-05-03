package com.nasnav.dao;

import com.nasnav.persistence.CompensationRuleTierEntity;
import com.nasnav.persistence.EligibleNotReceivedEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.SubPostEntity;
import com.nasnav.persistence.UserEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EligibleNotReceivedRepository extends JpaRepository<EligibleNotReceivedEntity, Long> {
    Optional<EligibleNotReceivedEntity> findByUserAndSubPostAndCompensationTier(UserEntity user , SubPostEntity subPost , CompensationRuleTierEntity tier);

    PageImpl<EligibleNotReceivedEntity> findAllByOrganization(OrganizationEntity organizationEntity, Pageable pageable);
}