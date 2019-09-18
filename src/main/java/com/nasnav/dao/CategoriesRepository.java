package com.nasnav.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.CategoriesEntity;

public interface CategoriesRepository extends JpaRepository<CategoriesEntity, Long> {

	@Query("select c.id from CategoriesEntity c where c.name = :categoryName")
	Long findByName(@Param("categoryName")String categoryName);

}
