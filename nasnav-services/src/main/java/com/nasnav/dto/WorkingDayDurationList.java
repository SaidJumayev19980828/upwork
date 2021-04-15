package com.nasnav.dto;

import lombok.Data;

import java.util.List;

@Data
public class WorkingDayDurationList {

    private List<FromUntilTime> workingTimesDuringDay;
}
