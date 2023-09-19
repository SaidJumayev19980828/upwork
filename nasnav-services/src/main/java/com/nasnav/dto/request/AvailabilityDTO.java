package com.nasnav.dto.request;

import com.nasnav.dto.UserRepresentationObject;
import lombok.Data;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Data
public class AvailabilityDTO {
    private Long id;
    @NotNull
    @JsonProperty("starts_at")
    private LocalDateTime startsAt;
    @NotNull
    @JsonProperty("ends_at")
    private LocalDateTime endsAt;
    @NotNull
    @JsonProperty("organization_id")
    private Long organizationId;
    @JsonProperty("shop_id")
    private Long shopId;
    @NotNull
    private Long period;
    private Long user;
    private Long employee;
    private UserRepresentationObject employeeRepresentation;
}
