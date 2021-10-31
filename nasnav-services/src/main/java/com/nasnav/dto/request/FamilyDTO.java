package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class FamilyDTO {
    private Long id;
    private String familyName;
    private Long parentId;
    private Boolean isActive;
    private Long boosterId;
    private Long orgId;
}
