package com.nasnav.dto.response;

import com.nasnav.dto.ProductDetailsDTO;
import lombok.Data;

@Data
public class SubPostResponseDTO {
    private Long parentPostId;
    private Long id;
    private ProductDetailsDTO product;
    private boolean isLiked;
    private long likesCount;

}
