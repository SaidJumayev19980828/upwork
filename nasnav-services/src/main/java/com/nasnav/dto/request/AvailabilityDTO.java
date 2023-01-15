package com.nasnav.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class AvailabilityDTO {
    private Long id;
    @NotNull
    private LocalDateTime startsAt;
    @NotNull
    private LocalDateTime endsAt;
    @NotNull
    private Long organizationID;
    private Long shopID;
    @NotNull
    private Long period;
    private Long user;
    private Long employee;
}
