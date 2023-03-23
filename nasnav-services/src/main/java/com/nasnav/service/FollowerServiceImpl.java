package com.nasnav.service;

import com.nasnav.dao.FollowerRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.FollowerEntity;
import com.nasnav.persistence.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowerServiceImpl implements FollowerServcie{
    @Autowired
    private FollowerRepository followerRepository;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<UserRepresentationObject> getAllFollowersByUserId(long userId) {
        List<FollowerEntity> entities = followerRepository.getAllByUser_Id(userId);
        return entities.stream().map(o -> o.getFollower().getRepresentation()).collect(Collectors.toList());
    }

    @Override
    public List<UserRepresentationObject> getAllFollowingsByUserId(long followerId) {
        List<FollowerEntity> entities = followerRepository.getAllByFollower_Id(followerId);
        return entities.stream().map(o -> o.getUser().getRepresentation()).collect(Collectors.toList());
    }

    @Override
    public void followOrUnfollow(long userId, boolean followAction) throws BusinessException {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if (loggedInUser instanceof UserEntity){
            UserEntity follower = (UserEntity) loggedInUser;
            UserEntity user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("Following Can't be found","",HttpStatus.NOT_FOUND));
            FollowerEntity found = followerRepository.getByUser_IdAndFollower_Id(user.getId(), follower.getId());
            if(followAction){
                if(found != null){
                    throw new BusinessException("Already you follow the user", "", HttpStatus.NOT_ACCEPTABLE);
                }
                followerRepository.save(new FollowerEntity(user,follower));
            }
            else {
                if(found != null){
                    followerRepository.delete(found);
                }
                else {
                    throw new BusinessException("No Relationship found between both users", "", HttpStatus.NOT_FOUND);
                }
            }
        }
        else {
            throw new BusinessException("Customer User Only can Follow","", HttpStatus.NOT_ACCEPTABLE);
        }

    }
}
