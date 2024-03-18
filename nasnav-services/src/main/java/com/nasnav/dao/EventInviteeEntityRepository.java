package com.nasnav.dao;

import com.nasnav.persistence.EventInviteeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventInviteeEntityRepository extends JpaRepository<EventInviteeEntity, Long> {
}