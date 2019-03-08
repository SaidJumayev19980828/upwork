package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "name",
        "p_name",
        "area",
        "p_area",
        "country",
        "city",
        "street",
        "floor",
        "lat",
        "lng",
        "logo",
        "cover_image",
        "panorama_link",
        "work_time"
})
@Data
public class ShopRepresentationObject extends BaseRepresentationObject{

    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("p_name")
    private String pName;
    @JsonProperty("area")
    private Object area;
    @JsonProperty("p_area")
    private String pArea;
    @JsonProperty("country")
    private String country;
    @JsonProperty("city")
    private Object city;
    @JsonProperty("street")
    private String street;
    @JsonProperty("floor")
    private String floor;
    @JsonProperty("lat")
    private String lat;
    @JsonProperty("lng")
    private String lng;
    @JsonProperty("logo")
    private String logo;
    @JsonProperty("cover_image")
    private String cover_image;
    @JsonProperty("panorama_link")
    private String panorama_link;
    @JsonProperty("work_time")
    private List<WorkTimeRepresentationObject> workTime = null;

}