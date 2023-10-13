package com.nasnav.dao;

import com.nasnav.persistence.ChatWidgetSettingEntity;
import com.nasnav.persistence.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatWidgetSettingRepository extends JpaRepository<ChatWidgetSettingEntity,Long> {
    Optional<ChatWidgetSettingEntity> findByOrganizationAndType(OrganizationEntity org,Integer type);
}
