package com.nasnav.service;

import com.nasnav.dto.request.PostCreationDTO;
import com.nasnav.dto.response.PostResponseDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.PostEntity;

public interface PostService {
    public PostResponseDTO getPostById(long id) throws BusinessException;
    public PostResponseDTO createPost(PostCreationDTO dto) throws BusinessException;
    public Long likeOrDisLikePost(long postId, boolean likeAction);
}
