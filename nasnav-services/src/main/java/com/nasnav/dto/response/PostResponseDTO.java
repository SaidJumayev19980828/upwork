package com.nasnav.dto.response;

import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.ProductDetailsDTO;
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
    private Set<ProductDetailsDTO> products;
    private List<PostAttachmentsEntity> attachments;
    private Long likesCount;
    private Long clicksCount;
    private Boolean isLiked;
    private LocalDateTime createdAt;
}
