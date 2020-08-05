package com.nasnav.dao;

import com.nasnav.persistence.CitiesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<CitiesEntity, Long> {

    Optional<CitiesEntity> findByName(String name);

    CitiesEntity findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
