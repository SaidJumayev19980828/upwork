package com.nasnav.dto;

import com.nasnav.dto.response.EventResponseDto;
import lombok.Data;

import java.util.List;

@Data
public class InfluencerDTO {
    private Long id;
    private String name;
    private String image;
    private String email;
    private String phoneNumber;
    private List<CategoryDTO> categories;
    private List<EventResponseDto> hostedEvents;
    private Integer interests;
    private Integer attends;
}
