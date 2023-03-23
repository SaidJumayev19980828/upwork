package com.nasnav.service;

import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.FollowerEntity;

import java.util.List;


public interface FollowerServcie {
    public List<UserRepresentationObject> getAllFollowersByUserId(long userId);
    public List<UserRepresentationObject> getAllFollowingsByUserId(long userId);
    public void followOrUnfollow(long userId,boolean followAction) throws BusinessException;
}
