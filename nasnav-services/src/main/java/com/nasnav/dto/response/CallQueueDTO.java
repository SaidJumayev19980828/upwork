package com.nasnav.dto.response;

import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.enumerations.CallQueueStatus;
import com.nasnav.persistence.ShopsEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CallQueueDTO {
    private Long id;
    private LocalDateTime joinsAt;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private UserRepresentationObject user;
    private UserRepresentationObject employee;
    private OrganizationRepresentationObject organization;
    private CallQueueStatus status;
    private String reason;
    private Integer position;
    private Integer total;
    private ShopsEntity shop;
}
