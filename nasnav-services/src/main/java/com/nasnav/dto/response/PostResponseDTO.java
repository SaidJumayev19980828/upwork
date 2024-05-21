package com.nasnav.dto.response;

import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.ProductDetailsDTO;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.enumerations.PostStatus;
import com.nasnav.enumerations.PostType;
import com.nasnav.persistence.PostAttachmentsEntity;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class PostResponseDTO {
    private Long id;
    private String description;
    private PostStatus status;
    private PostType type;
    private OrganizationRepresentationObject organization;
    private UserRepresentationObject user;
    /**
     * On its way to being removed due to a change in the Post handling way it will be dealt soon
     */
    @Deprecated(since = "21/3", forRemoval = true)
    private Set<ProductDetailsDTO> products;
    private List<PostAttachmentsEntity> attachments;
    private String productName;
    private Short rating;
    private ShopRepresentationObject shop;
    private Long likesCount;
    private Long clicksCount;
    private Boolean isLiked;
    private LocalDateTime createdAt;
    private Boolean isSaved;
    private Set<SubPostResponseDTO> subPosts;
    private long totalReviewLikes;
}
