package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
})
@Data
public class OpenWorkingDays {

    private WorkingDayDurationList mon;
    private WorkingDayDurationList tue;
    private WorkingDayDurationList wed;
    private WorkingDayDurationList thu;
    private WorkingDayDurationList fri;
    private WorkingDayDurationList sat;
    private WorkingDayDurationList sun;
}