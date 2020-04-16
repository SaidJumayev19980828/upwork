package com.nasnav.dao;

import com.nasnav.persistence.ThemeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemesRepository extends JpaRepository<ThemeEntity, Long> {

}
