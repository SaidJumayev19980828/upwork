package com.nasnav.dao;

import com.nasnav.persistence.CategoriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoriesRepository extends JpaRepository<CategoriesEntity, Long> {

	@Query("select c.id from CategoriesEntity c where c.name = :categoryName")
	Long findByName(@Param("categoryName")String categoryName);

	
	@Query("select distinct tag.categoriesEntity from "
			+ " TagsEntity tag where tag.organizationEntity.id = :orgId"
			+ " and tag.categoriesEntity.id = :catId")
	List<CategoriesEntity> findByOrganizationIdAndCategoryId(@Param("orgId") Long organizationId, @Param("catId") Long categoryId);
	
	
	@Query("select distinct tag.categoriesEntity from "
			+ " TagsEntity tag where tag.organizationEntity.id = :orgId")
	List<CategoriesEntity> findByOrganizationId(@Param("orgId") Long organizationId);
	
	
	List<CategoriesEntity> findByParentId(Long parentId);

	@Query("select c.id from CategoriesEntity c where c.parentId = :parentId")
	List<Long> findCategoriesIdsByParentId(@Param("parentId") Long parentId);
}
