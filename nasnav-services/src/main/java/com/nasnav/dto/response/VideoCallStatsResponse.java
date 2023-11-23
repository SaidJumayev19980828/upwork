package com.nasnav.dto.response;

import com.nasnav.dto.VideoCallDetailsDto;
import com.nasnav.dto.VideoCallStatsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class VideoCallStatsResponse {

    private Integer totalWaitingCalls;
    private VideoCallStatsDto todayStats;
    private List<VideoCallDetailsDto> scheduledCalls;
    private List<VideoCallDetailsDto> upcomingCalls;

}
