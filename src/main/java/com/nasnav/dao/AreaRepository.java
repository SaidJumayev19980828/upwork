package com.nasnav.dao;

import com.nasnav.persistence.AreasEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AreaRepository extends CrudRepository<AreasEntity, Long> {

    Optional<AreasEntity> findByName(String name);

    boolean existsByNameIgnoreCase(String name);
}
