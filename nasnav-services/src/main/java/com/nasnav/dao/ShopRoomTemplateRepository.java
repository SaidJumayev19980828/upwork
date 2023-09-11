package com.nasnav.dao;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.ShopRoomTemplateEntity;

public interface ShopRoomTemplateRepository extends JpaRepository<ShopRoomTemplateEntity, Long> {
	Optional<ShopRoomTemplateEntity> findByShopId(Long shopId);

	int deleteTemplateByShopIdAndShopOrganizationEntityId(Long shopId, Long orgId);

	Set<ShopRoomTemplateEntity> findAllByShopOrganizationEntityId(@Param("orgId") Long orgId);

	@Query("select rt from ShopRoomTemplateEntity rt join fetch rt.shop s join fetch s.organizationEntity o where o.yeshteryState = 1")
	Set<ShopRoomTemplateEntity> findAllByShopOrganizationEntityYeshteryStateEquals1();
}
