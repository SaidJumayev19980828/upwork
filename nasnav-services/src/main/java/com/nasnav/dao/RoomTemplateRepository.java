package com.nasnav.dao;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.RoomTemplateEntity;

public interface RoomTemplateRepository extends JpaRepository<RoomTemplateEntity, Long> {
	Optional<RoomTemplateEntity> findByShopId(Long shopId);

	int deleteTemplateByShopIdAndShopOrganizationEntityId(Long shopId, Long orgId);

	Set<RoomTemplateEntity> findAllByShopOrganizationEntityId(@Param("orgId") Long orgId);

	@Query("select rt from RoomTemplateEntity rt join fetch rt.shop s join fetch s.organizationEntity o where o.yeshteryState = 1")
	Set<RoomTemplateEntity> findAllByShopOrganizationEntityYeshteryStateEquals1();
}
