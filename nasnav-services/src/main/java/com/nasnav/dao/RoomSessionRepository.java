package com.nasnav.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.RoomSessionEntity;

public interface RoomSessionRepository extends JpaRepository<RoomSessionEntity, Long> {

}
