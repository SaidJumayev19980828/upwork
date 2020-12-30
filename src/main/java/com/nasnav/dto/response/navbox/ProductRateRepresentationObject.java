package com.nasnav.dto.response.navbox;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.BaseRepresentationObject;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductRateRepresentationObject extends BaseRepresentationObject {
    private Long id;
    private String review;
    private Integer rate;
    private Long userId;
    private String userName;
    private LocalDateTime submissionDate;
    private boolean approved;
}
