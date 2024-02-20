package com.nasnav.dao;

import com.nasnav.persistence.ReferralSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReferralSettingsRepo extends JpaRepository<ReferralSettings, Long> {

    Optional<ReferralSettings> findByOrganization_Id(Long organizationId);

}
