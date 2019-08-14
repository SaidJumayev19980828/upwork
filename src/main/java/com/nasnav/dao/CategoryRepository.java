package com.nasnav.dao;

import com.nasnav.persistence.CategoriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoriesEntity, Long> {

    //CategoriesEntity findByIdA
}
