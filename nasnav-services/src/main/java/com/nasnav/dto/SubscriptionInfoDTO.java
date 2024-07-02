package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@NoArgsConstructor
public class SubscriptionInfoDTO {
    //Has Subscription Not(Canceled or Expired)
    private boolean isSubscribed;
    private String type;
    private String status;
    private Date expirationDate;
    @JsonIgnore
    private Long subscriptionEntityId;
    private Long packageId;
    private Long organizationId;
    private String organizationName;
}
