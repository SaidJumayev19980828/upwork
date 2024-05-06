package com.nasnav.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class InfluencerDTO {
    private Long id;
    private Long userId;
    private Long employeeId;
    private String name;
    private String image;
    private String email;
    private String phoneNumber;
    private List<CategoryDTO> categories;
    private Integer hostedEvents;
    private Integer interests;
    private Integer attends;
    private Boolean isGuided;
    private LocalDateTime date;
    private UserRepresentationObject userRepresentationObject;
    private Long share;
    private Integer sellingProduct;
}
