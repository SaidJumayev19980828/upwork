package com.nasnav.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class VideoCallStatsDto {

    private Integer todayTotalCalls;
    private Integer todayAnsweredCalls;

}
