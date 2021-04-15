package com.nasnav.dao;

import com.nasnav.persistence.StockUnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockUnitRepository extends JpaRepository<StockUnitEntity, Long> {
    StockUnitEntity findByName(String name);
}
