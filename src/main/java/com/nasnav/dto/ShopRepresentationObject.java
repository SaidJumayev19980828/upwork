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
public class ShopRepresentationObject {

    @JsonProperty("id")
    public Long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("p_name")
    public String pName;
    @JsonProperty("area")
    public Object area;
    @JsonProperty("p_area")
    public String pArea;
    @JsonProperty("country")
    public String country;
    @JsonProperty("city")
    public Object city;
    @JsonProperty("street")
    public String street;
    @JsonProperty("floor")
    public String floor;
    @JsonProperty("lat")
    public String lat;
    @JsonProperty("lng")
    public String lng;
    @JsonProperty("logo")
    public String logo;
    @JsonProperty("cover_image")
    public String cover_image;
    @JsonProperty("panorama_link")
    public String panorama_link;
    @JsonProperty("work_time")
    public List<WorkTime> workTime = null;

}