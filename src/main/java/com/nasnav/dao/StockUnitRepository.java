package com.nasnav.dao;

import com.nasnav.persistence.StockUnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockUnitRepository extends JpaRepository<StockUnitEntity, Long> {
    StockUnitEntity findByName(String name);
}
