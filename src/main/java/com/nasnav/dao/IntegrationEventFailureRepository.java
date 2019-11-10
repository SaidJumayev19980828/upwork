package com.nasnav.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.IntegrationEventFailureEntity;

public interface IntegrationEventFailureRepository extends JpaRepository<IntegrationEventFailureEntity, Long> {

}
