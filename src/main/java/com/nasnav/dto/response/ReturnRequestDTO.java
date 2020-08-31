package com.nasnav.dto.response;

import com.nasnav.enumerations.ReturnRequestStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ReturnRequestDTO {
    private Long id;
    private LocalDateTime createdOn;
    private Long metaOrderId;
    private Long createdByUser;
    private Long createdByEmployee;
    private ReturnRequestStatus status;
    //Set<ReturnRequestItemEntity> returnedItems
}
