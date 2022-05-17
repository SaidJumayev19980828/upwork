package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.enumerations.VideoChatStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Schema(name = "VideoChatLog")
public class VideoChatLogRepresentationObject  extends BaseRepresentationObject {

    private Long id;
    private String name;
    private String description;
    private String userName;
    private String assignedToName;
    private  String organizationName;
    private VideoChatStatus status;
    private Boolean isActive;
}
