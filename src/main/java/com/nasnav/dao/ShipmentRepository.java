package com.nasnav.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.ShipmentEntity;

public interface ShipmentRepository extends JpaRepository<ShipmentEntity, Long> {

}
