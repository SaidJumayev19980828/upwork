package com.nasnav.dao;

import com.nasnav.persistence.CompensationActionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompensationActionsEntityRepository extends JpaRepository<CompensationActionsEntity, Long> {
}