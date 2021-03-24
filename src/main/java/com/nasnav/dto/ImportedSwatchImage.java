package com.nasnav.dto;

import com.nasnav.service.model.ImportedImage;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class ImportedSwatchImage {
    private Long variantId;
    private Long featureId;
    private MultipartFile image;

    public ImportedSwatchImage(ImportedImage img, Long featureId){
        this.variantId = img.getImgMetaData().getVariantId();
        this.featureId = featureId;
        this.image = img.getImage();
    }
}
