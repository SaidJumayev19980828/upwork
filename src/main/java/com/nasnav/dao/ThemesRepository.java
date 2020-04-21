package com.nasnav.dao;

import com.nasnav.persistence.ThemeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThemesRepository extends JpaRepository<ThemeEntity, Integer> {

    List<ThemeEntity> findByThemeClassEntity_Id(Integer classId);
}
