package com.nasnav.dao;

import com.nasnav.persistence.CompensationRuleTierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompensationRuleTierEntityRepository extends JpaRepository<CompensationRuleTierEntity, Long> {
}