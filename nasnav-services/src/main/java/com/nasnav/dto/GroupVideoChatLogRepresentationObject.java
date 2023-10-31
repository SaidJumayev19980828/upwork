package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.enumerations.VideoChatStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Schema(name = "GroupVideoChatLog")
public class GroupVideoChatLogRepresentationObject extends BaseRepresentationObject {

    private Long id;
    private String name;
    private String description;
    private List<Long> userIds;
    private List<Long> employeeUserIds;
    private String organizationName;
    private Long organizationId;
    private Long shopId;
    private String shopName;
    private VideoChatStatus status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
}
