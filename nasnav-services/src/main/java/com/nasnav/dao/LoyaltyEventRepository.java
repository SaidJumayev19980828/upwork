package com.nasnav.dao;

import com.nasnav.dto.request.LoyaltyEventDTO;
import com.nasnav.persistence.LoyaltyEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyEventRepository extends JpaRepository< LoyaltyEventEntity, Long> {
    List<LoyaltyEventEntity> findByOrganization_Id(Long orgId);
}
