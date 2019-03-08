package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "day",
        "opening",
        "closing"
})
@Data
public class WorkTime {

    @JsonProperty("day")
    public Date day;
    @JsonProperty("opening")
    public String opening;
    @JsonProperty("closing")
    public String closing;

}