package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WorkingDayDurationList {

    private List<FromUntilTime> workingTimesDuringDay;
}
