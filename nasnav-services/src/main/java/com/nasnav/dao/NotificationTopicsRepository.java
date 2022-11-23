package com.nasnav.dao;

import com.nasnav.persistence.NotificationTopicsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTopicsRepository extends JpaRepository<NotificationTopicsEntity,Long> {
    NotificationTopicsEntity getByTopic(String topic);
}
