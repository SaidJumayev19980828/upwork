package com.nasnav.dao;

import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.VideoChatLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoChatLogRepository extends JpaRepository<VideoChatLogEntity, Long> {

    Optional<VideoChatLogEntity> findByToken(String token);
    Optional<VideoChatLogEntity> findByName(String name);

    Optional<VideoChatLogEntity> findByNameAndOrganization(String sessionName, OrganizationEntity orgId);

    List<VideoChatLogEntity> findByOrganization_Id(Long orgId);

    List<VideoChatLogEntity> findByStatusAndOrganization_Id(Integer status, Long orgId);

    Long countByOrganization_IdAndStatusIn(Long orgId, List<Integer> statusList);
}
