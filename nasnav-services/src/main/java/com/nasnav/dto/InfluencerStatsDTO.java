package com.nasnav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class InfluencerStatsDTO {
    private LocalDate date;
    private Integer interests;
    private Integer attends;
}
