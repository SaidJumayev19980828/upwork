package com.nasnav.dao;

import com.nasnav.persistence.SettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SettingRepository extends JpaRepository<SettingEntity, Long> {

	Optional<SettingEntity> findBySettingNameAndOrganization_Id(String name, long orgId);

	@Transactional
    @Modifying
    @Query("DELETE FROM SettingEntity setting "
    		+ " WHERE setting.settingName = :settingName "
    		+ " AND setting.organization.id = :orgId")
	void deleteBySettingNameAndOrganization_Id(
			@Param("settingName")String settingName, @Param("orgId")Long orgId);

	List<SettingEntity> findByOrganization_Id(Long orgId);

	List<SettingEntity> findByOrganization_IdAndType(Long orgId, Integer type);

}
