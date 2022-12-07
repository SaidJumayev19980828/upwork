package com.nasnav.dao;

import com.nasnav.persistence.NotificationTopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTopicsRepository extends JpaRepository<NotificationTopicEntity,Long> {
    NotificationTopicEntity getByTopic(String topic);
    boolean existsByTopic(String topic);
}
