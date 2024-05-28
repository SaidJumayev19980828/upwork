package com.nasnav.service.impl;

import com.nasnav.commons.utils.CustomPaginationPageRequest;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.response.*;
import com.nasnav.enumerations.PostStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.*;
import com.nasnav.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.PagingUtils.getQueryPage;

@Service
public class FollowerServiceImpl implements FollowerServcie{
    @Autowired
    private FollowerRepository followerRepository;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private EventLogsRepository eventLogsRepository;

    @Override
    public PageImpl<FollowerDTO> getAllFollowersByUserId(long userId, Integer start, Integer count) {
        List<FollowerDTO> dtos = new ArrayList<>();
        PageRequest page = getQueryPage(start, count);
        PageImpl<FollowerEntity> source = followerRepository.getAllByUser_Id(userId, page);
        source.getContent().forEach(o -> {
            FollowerDTO dto = new FollowerDTO();
            UserRepresentationObject representation = o.getFollower().getRepresentation();
            dto.setUserRepresentationObject(representation);
            representation.setFollowersCount(followerRepository.countAllByUser_Id(representation.getId()));
            representation.setFollowingsCount(followerRepository.countAllByFollower_Id(representation.getId()));
            representation.setEventsCount(eventLogsRepository.countAllByUserId(representation.getId()));
            dto.setIsFollowed(followerRepository.existsByFollower_IdAndUser_Id(userId, o.getFollower().getId()));
            dtos.add(dto);
        });
        return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
    }

    @Override
    public PageImpl<UserRepresentationObject> getAllFollowingsByUserId(long followerId, Integer start, Integer count) {
        PageRequest page = getQueryPage(start, count);
        PageImpl<FollowerEntity> source = followerRepository.getAllByFollower_Id(followerId, page);
        List<UserRepresentationObject> dtos = source.getContent().stream().map(o -> o.getUser().getRepresentation()).toList();
        dtos.forEach(followingRep -> {
            followingRep.setFollowersCount(followerRepository.countAllByUser_Id(followingRep.getId()));
            followingRep.setFollowingsCount(followerRepository.countAllByFollower_Id(followingRep.getId()));
            followingRep.setEventsCount(eventLogsRepository.countAllByUserId(followingRep.getId()));
        });
        return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
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

    @Override
    public List<UserEntity> getAllFollowingAsUserEntity(long followerId) {
        List<FollowerEntity> followings = followerRepository.getAllByFollower_Id(followerId);
        return followings.stream().map(o -> o.getUser()).collect(Collectors.toList());
    }

    @Override
    public FollowerInfoDTO getFollowerInfoByUserId(long userId) {
        FollowerInfoDTO dto = new FollowerInfoDTO();
        dto.setFollowersCount(followerRepository.countAllByUser_Id(userId));
        dto.setFollowingsCount(followerRepository.countAllByFollower_Id(userId));
        dto.setPostsCount(postRepository.countAllByUser_IdAndStatus(userId, PostStatus.APPROVED.getValue()));
        return dto;
    }

    @Override
    public PageImpl<UserListFollowProjection> getUsersWithFollowerStatus(Integer start,Integer count ) {
        Pageable page = new CustomPaginationPageRequest(start, count);
        BaseUserEntity currentUser = securityService.getCurrentUser();
        return  userRepository.findUsersWithFollowerStatus(currentUser.getId(),page);
    }
}
