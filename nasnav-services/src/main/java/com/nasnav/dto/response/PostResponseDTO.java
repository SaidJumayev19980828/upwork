package com.nasnav.dto.response;

import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.ProductDetailsDTO;
import com.nasnav.persistence.PostAttachmentsEntity;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class PostResponseDTO {
    private Long id;
    private String description;
    private OrganizationRepresentationObject organization;
    private GeneralRepresentationDto user;
    private Set<ProductDetailsDTO> products;
    private List<PostAttachmentsEntity> attachments;
    private Long likesCount;
}
