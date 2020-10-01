package com.nasnav.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.enumerations.ReturnRequestStatus;
import com.nasnav.persistence.ReturnRequestItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
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
    private String userName;
    private String phoneNumber;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Set<ReturnRequestItemDTO> returnedItems;

    public ReturnRequestDTO() {
        returnedItems = new HashSet<>();
    }
}
