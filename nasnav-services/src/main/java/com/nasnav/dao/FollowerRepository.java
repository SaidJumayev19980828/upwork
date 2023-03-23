package com.nasnav.dao;

import com.nasnav.persistence.FollowerEntity;
import com.nasnav.persistence.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FollowerRepository extends CrudRepository<FollowerEntity, Long> {
    List<FollowerEntity> getAllByUser_Id(long userId);
    List<FollowerEntity> getAllByFollower_Id(long followerId);
    FollowerEntity getByUserAndFollower(UserEntity userEntity, UserEntity follower);
    FollowerEntity getByUser_IdAndFollower_Id(long userId, long followerId);
}
