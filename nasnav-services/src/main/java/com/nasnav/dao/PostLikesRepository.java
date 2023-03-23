package com.nasnav.dao;

import com.nasnav.persistence.PostEntity;
import com.nasnav.persistence.PostLikesEntity;
import com.nasnav.persistence.UserEntity;
import org.springframework.data.repository.CrudRepository;

public interface PostLikesRepository extends CrudRepository<PostLikesEntity, Long> {
    Long countAllByPost_Id(Long postId);
    PostLikesEntity getByUserAndPost(UserEntity userEntity, PostEntity postEntity);
}
