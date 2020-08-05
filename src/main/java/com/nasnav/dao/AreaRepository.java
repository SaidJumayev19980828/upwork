package com.nasnav.dao;

import com.nasnav.persistence.AreasEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AreaRepository extends CrudRepository<AreasEntity, Long> {

    Optional<AreasEntity> findByName(String name);

    AreasEntity findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query(value = "select a.id from AreasEntity a join a.citiesEntity city where city.id = :id")
    List<Long> findAreasByCityId(@Param("id") Long id);
}
