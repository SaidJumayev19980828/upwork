package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Schema(name = "swatch images import meta data")
@NoArgsConstructor
public class SwatchImageBulkUpdateDTO {
    private boolean ignoreErrors;
    private boolean deleteOldImages;
    private Long featureId;
    private boolean crop;

    public SwatchImageBulkUpdateDTO(ProductImageBulkUpdateDTO metaData){
        this.setDeleteOldImages(metaData.isDeleteOldImages());
        this.setFeatureId(metaData.getFeatureId());
        this.setIgnoreErrors(metaData.isIgnoreErrors());
        this.setCrop(metaData.isCrop());
    }
}
