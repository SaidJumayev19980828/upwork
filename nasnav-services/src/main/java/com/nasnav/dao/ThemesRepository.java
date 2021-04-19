package com.nasnav.dao;

import com.nasnav.persistence.ThemeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ThemesRepository extends JpaRepository<ThemeEntity, Integer> {

    List<ThemeEntity> findByThemeClassEntity_Id(Integer classId);
    Integer countByThemeClassEntity_Id(Integer classId);

    Optional<ThemeEntity> findByUid(String uid);

    boolean existsByUid(String uid);

    boolean deleteByUid(String uid);

    boolean existsByUidAndThemeClassEntity_IdIn(String themeUId, List<Integer> classesIds);
}
