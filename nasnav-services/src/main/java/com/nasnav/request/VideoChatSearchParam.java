package com.nasnav.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.enumerations.VideoChatStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@AllArgsConstructor
public class VideoChatSearchParam extends BaseSearchParams{
    private VideoChatStatus status;
    private Boolean isActive;
    private Boolean isAssigned;
    private Boolean hasShop;
    private Long shopId;
    private Long orgId;
    private Integer start;
    private Integer count;
}
