package com.nasnav.dto;

import lombok.Data;

import java.util.List;

@Data
public class SceneDTO {

    private List<HotSpotsDTO> hotSpots;
    private String panorama;
    private String hfov;
    private Long pitch;
    private String title;
    private String type;
    private Long yaw;

}
