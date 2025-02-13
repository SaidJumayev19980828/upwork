package com.nasnav.service;

import com.nasnav.dto.request.PostCreationDTO;
import com.nasnav.dto.response.LikePostResponse;
import com.nasnav.dto.response.PostResponseDTO;
import com.nasnav.enumerations.PostStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.PostEntity;
import org.springframework.data.domain.PageImpl;

import java.io.IOException;

public interface PostService {
    public PostResponseDTO getPostById(long id) throws BusinessException;
     PostResponseDTO createPost(PostCreationDTO dto) throws BusinessException, IOException;
    LikePostResponse likeOrDisLikePost(long postId, boolean likeAction);
    long likeOrDisLikeReview(long postId);
    public void clickOnPost(long postId);
    public void approveOrRejectReview(long postId, PostStatus postStatus);
    public PageImpl<PostResponseDTO> getHomeTimeLine(Integer start, Integer count);
    public PageImpl<PostResponseDTO> getUserTimeLine(long userId, Integer start, Integer count);
    public PageImpl<PostResponseDTO> getFilterForUser(Long userId, Integer start, Integer count,String type);
    public PageImpl<PostResponseDTO> getUserPendingPosts(Integer start, Integer count);
    public PageImpl<PostResponseDTO> getOrgReviews(PostStatus postStatus, Integer start, Integer count);
    public PageImpl<PostResponseDTO> getOrgSharedProducts(Integer start, Integer count);

    PageImpl<PostEntity>  getAllPostsWithinAdvertisement(Integer start, Integer count);

     void saveForLater(Long postId);
     void unSavePost(Long postId);

     PageImpl<PostResponseDTO> getUserSavedPosts(Integer start, Integer count);

    }
