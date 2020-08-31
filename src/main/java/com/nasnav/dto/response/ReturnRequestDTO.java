package com.nasnav.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.enumerations.ReturnRequestStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ReturnRequestDTO extends BaseRepresentationObject {
    private Long id;
    private LocalDateTime createdOn;
    private Long metaOrderId;
    private Long createdByUser;
    private Long createdByEmployee;
    private ReturnRequestStatus status;
    //Set<ReturnRequestItemEntity> returnedItems
}
