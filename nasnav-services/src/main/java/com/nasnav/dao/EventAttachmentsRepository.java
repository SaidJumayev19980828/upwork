package com.nasnav.dao;

import com.nasnav.persistence.EventAttachmentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventAttachmentsRepository extends JpaRepository<EventAttachmentsEntity, Long> {
    void deleteAllByEvent_Id(Long eventId);
}
