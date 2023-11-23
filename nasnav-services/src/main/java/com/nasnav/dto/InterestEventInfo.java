package com.nasnav.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class InterestEventInfo {

    private LocalDateTime startAt;
    private String eventName;
    private Long eventId;
    private Long organizationId;
    private String organizationName;
    private Long userId;
    private String userName;
    private String userEmail;
//    private Long employeeId;
//    private String employeeName;
//    private String employeeEmail;


}
