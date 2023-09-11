package com.nasnav.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventOrganiseRequestDTO {
    @NotNull
    private Long id;
    @NotNull
    @JsonProperty("starts_at")
    private LocalDateTime startsAt;
    @NotNull
    @JsonProperty("ends_at")
    private LocalDateTime endsAt;
}
