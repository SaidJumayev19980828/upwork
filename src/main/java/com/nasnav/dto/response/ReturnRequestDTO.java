package com.nasnav.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.enumerations.ReturnRequestStatus;
import lombok.Data;

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
    private AddressRepObj address;
    private Long itemsCount;
    private String operator;
    private String paymentUid;
    private String shippingServiceId;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Set<ReturnRequestItemDTO> returnedItems;

    public ReturnRequestDTO() {
        returnedItems = new HashSet<>();
        itemsCount = 0L;
    }
}
