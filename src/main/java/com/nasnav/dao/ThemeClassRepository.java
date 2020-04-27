package com.nasnav.dao;

import com.nasnav.persistence.ThemeClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThemeClassRepository extends JpaRepository<ThemeClassEntity, Integer> {

    List<ThemeClassEntity> findByIdIn(List<Integer> classIds);
}
