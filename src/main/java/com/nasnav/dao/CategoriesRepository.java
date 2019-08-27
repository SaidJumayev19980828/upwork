package com.nasnav.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.CategoriesEntity;

public interface CategoriesRepository extends JpaRepository<CategoriesEntity, Long> {

}
