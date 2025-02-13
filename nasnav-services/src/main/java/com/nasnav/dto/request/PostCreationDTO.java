package com.nasnav.dto.request;

import com.nasnav.request.ImageBase64;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Set;

@Data
public class PostCreationDTO {
    private boolean isReview;
    private String description;
    private Long organizationId;
    private Long advertisementId;
    private Set<Long> productsIds;
    private List<ImageBase64> attachment;
    private Long shopId;
    @Min(value = 0, message = "Rating must be at least 0")
    @Max(value = 5, message = "Rating must be at most 5")
    private Short rating;
    private String productName;

    @AssertTrue(message = "Attachment cannot be empty for reviews")
    private boolean isAttachmentValid() {
        return !isReview || (attachment != null && !attachment.isEmpty());
    }


    @AssertTrue(message = "Products cannot be empty for Post")
    private boolean isProductsIdsValid() {
        return isReview || (productsIds != null && !productsIds.isEmpty());
    }
}
