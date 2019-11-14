package com.nasnav.dao;

import com.nasnav.persistence.TagsEntity;
import org.springframework.data.repository.CrudRepository;

public interface TagsRepository extends CrudRepository<TagsEntity, Long> {

}
