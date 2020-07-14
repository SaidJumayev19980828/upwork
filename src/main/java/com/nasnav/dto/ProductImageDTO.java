package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductImageDTO extends BaseRepresentationObject {
    private Long id;
    private Long productId;
    private Long variantId;
    private String barcode;
    @JsonProperty("url")
    private String imagePath;
    private Integer priority;

    public ProductImageDTO(Long id, String imagePath, Long productId, Long variantId, Integer priority) {
        this.id = id;
        this.imagePath = imagePath;
        this.productId = productId;
        this.variantId = variantId;
        this.priority = priority;
    }

    public ProductImageDTO(Long id, String imagePath, Integer priority) {
        this.id = id;
        this.imagePath = imagePath;
        this.priority = priority;
    }
}
