package com.nasnav.dto;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record PersonalEventDTO(
        @NotBlank(message = "Event name must not be blank")
        String name,
        @FutureOrPresent(message = " Event start Date And Time must be in the Future or now")
        LocalDateTime startAt,
        @FutureOrPresent(message = " Event start Date And Time must be in the Future or now")
        LocalDateTime endAt,
        String description
) {

    @AssertTrue(message = "End date and time must be after start date and time")
    private boolean isValidDateTimeRange() {
        return startAt.isBefore(endAt);
    }

}