package com.nasnav.service;

import com.nasnav.dto.UserFollow;
import com.nasnav.dto.UserListFollowProjection;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.response.FollowerDTO;
import com.nasnav.dto.response.FollowerInfoDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.FollowerEntity;
import com.nasnav.persistence.UserEntity;
import org.springframework.data.domain.PageImpl;

import java.util.List;


public interface FollowerServcie {
    public PageImpl<FollowerDTO> getAllFollowersByUserId(long userId, Integer start, Integer count);
    public PageImpl<UserRepresentationObject> getAllFollowingsByUserId(long userId, Integer start, Integer count);
    public void followOrUnfollow(long userId,boolean followAction) throws BusinessException;
    public List<UserEntity> getAllFollowingAsUserEntity(long followerId);
    public FollowerInfoDTO getFollowerInfoByUserId(long userId);

    public PageImpl<UserListFollowProjection> getUsersWithFollowerStatus(Integer start, Integer count);
}
