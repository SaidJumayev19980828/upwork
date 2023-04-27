package com.nasnav.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CallQueueStatusDTO {
    private Long id;
    private LocalDateTime joinsAt;
    private Integer position;
    private Integer total;
}
