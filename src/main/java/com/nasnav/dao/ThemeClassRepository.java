package com.nasnav.dao;

import com.nasnav.persistence.ThemeClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemeClassRepository extends JpaRepository<ThemeClassEntity, Integer> {
}
