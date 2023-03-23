package com.nasnav.dto.request;

import com.nasnav.persistence.PostAttachmentsEntity;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class PostCreationDTO {
    private String description;
    private Long organizationId;
    private Set<Long> productsIds;
    private List<PostAttachmentsEntity> attachments;
}
