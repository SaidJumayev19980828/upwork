package com.nasnav.dao;

import com.nasnav.persistence.PostClicksEntity;
import com.nasnav.persistence.PostEntity;
import com.nasnav.persistence.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface PostClicksRepository extends CrudRepository<PostClicksEntity, Long> {
    Long countAllByPost_Id(Long postId);
    @Query("select coalesce(SUM(clicks.clicksCount),0) from PostClicksEntity clicks where clicks.post.id = :postId")
    Long getClicksCountByPost(Long postId);
    PostClicksEntity getByUserAndPost(UserEntity userEntity, PostEntity postEntity);
}
