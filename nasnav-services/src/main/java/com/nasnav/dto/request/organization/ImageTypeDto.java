package com.nasnav.dto.request.organization;



import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;


@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ImageTypeDto {

    private Long type_id;
    private Long organization_id;
    private String label;
    private String text;
}
