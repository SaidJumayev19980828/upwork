package com.nasnav.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class VideoCallDetailsDto {

    private Long userId;
    private String userName;
    private String userEmail;
    private String employeeName;
    private String organizationName;
    private LocalDateTime joinsAt;

}
