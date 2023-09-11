package com.nasnav.dao;

import com.nasnav.persistence.FollowerEntity;
import com.nasnav.persistence.UserEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FollowerRepository extends CrudRepository<FollowerEntity, Long> {
    PageImpl<FollowerEntity> getAllByUser_Id(long userId, Pageable page);
    PageImpl<FollowerEntity> getAllByFollower_Id(long followerId, Pageable page);
    List<FollowerEntity> getAllByFollower_Id(long followerId);
    FollowerEntity getByUser_IdAndFollower_Id(long userId, long followerId);
    Long countAllByFollower_Id(long followerId);
    Long countAllByUser_Id(long user);
}
