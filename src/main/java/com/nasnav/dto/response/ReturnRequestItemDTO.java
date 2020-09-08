package com.nasnav.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.BaseRepresentationObject;
import lombok.Data;

import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class ReturnRequestItemDTO extends BaseRepresentationObject {
    private Long id;
    private Integer returnedQuantity;
    private Integer receivedQuantity;
    private Long receivedBy;
    private LocalDateTime receivedOn;
    private Long createdByUser;
    private Long createdByEmployee;
    private Long basketItem;
    private String productName;
}
