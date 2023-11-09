package com.nasnav.dto.request;

import com.nasnav.persistence.PostAttachmentsEntity;
import com.nasnav.request.ImageBase64;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class PostCreationDTO {
    private Boolean isReview;
    private String description;
    private Long organizationId;
    private Long advertisementId;
    private Set<Long> productsIds;
    private ImageBase64 attachment;
}
