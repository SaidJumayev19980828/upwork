package com.nasnav.dao;

import com.nasnav.persistence.GroupVideoChatLogEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.VideoChatLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupVideoChatLogRepository extends JpaRepository<GroupVideoChatLogEntity, Long> {
    Optional<GroupVideoChatLogEntity> findByName(String name);

}
