package com.nasnav.dao;

import com.nasnav.persistence.PostLikesEntity;
import org.springframework.data.repository.CrudRepository;

public interface PostLikesRepository extends CrudRepository<PostLikesEntity, Long> {
}
