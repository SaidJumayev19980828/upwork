package com.nasnav.dao;

import com.nasnav.persistence.CountriesEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CountryRepository extends CrudRepository<CountriesEntity, Long> {

    Optional<CountriesEntity> findByName(String name);

    boolean existsByNameIgnoreCase(String name);
}
