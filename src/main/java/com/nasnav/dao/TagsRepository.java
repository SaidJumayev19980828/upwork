package com.nasnav.dao;

import com.nasnav.persistence.TagsEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TagsRepository extends CrudRepository<TagsEntity, Long> {
    List<TagsEntity> findAll();
    TagsEntity findByNameIgnoreCase(String name);
}
