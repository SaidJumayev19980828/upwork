package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountryInfoDTO {
    private Long id;
    private String name;
    private String type;
    @JsonProperty("parent_id")
    private Long parentId;
    @JsonProperty("iso_code")
    private Integer isoCode;
    private String currency;


    public CountryInfoDTO(Long id, String name, Long parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }

    public CountryInfoDTO(Long id, String name, Integer isoCode, String currency) {
        this.id = id;
        this.name = name;
        this.isoCode = isoCode;
        this.currency = currency;
    }
}
