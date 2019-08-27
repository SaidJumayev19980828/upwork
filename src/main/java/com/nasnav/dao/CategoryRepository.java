package com.nasnav.dao;

import com.nasnav.persistence.CategoriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoriesEntity, Long> {

    //CategoriesEntity findById
    List<CategoriesEntity> findByParentId(Integer parentId);
}
