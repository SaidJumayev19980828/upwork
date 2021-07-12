package com.nasnav.dao;

import com.nasnav.persistence.CountriesEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CountryRepository extends CrudRepository<CountriesEntity, Long> {

    Optional<CountriesEntity> findByName(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query(value = "select a.id from AreasEntity a join a.citiesEntity city join city.countriesEntity country where country.id = :id")
    List<Long> findAreasByCountryId(@Param("id") Long id);

    Optional<CountriesEntity> findByNameIgnoreCase(String name);

    CountriesEntity findByIsoCode(Integer isoCode);

    @Query("select c.id from CountriesEntity c")
    List<Long> findAllCountriesIds();
}
