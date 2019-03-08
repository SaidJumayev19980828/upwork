package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "logo_url",
        "first_color",
        "second_color",
        "first_section",
        "first_section_product",
        "first_section_image_url",
        "second_section",
        "second_section_product",
        "second_section_image_url",
        "slider_body",
        "slider_header",
        "slider_images_urls"
})
@Data
@EqualsAndHashCode(callSuper = false)
public class OrganizationThemesRepresentationObject extends BaseRepresentationObject{

    @JsonProperty("logo_url")
    private String logoUrl;
    @JsonProperty("first_color")
    private String firstColor;
    @JsonProperty("second_color")
    private String secondColor;
    @JsonProperty("first_section")
    private Boolean firstSection;
    @JsonProperty("first_section_product")
    private Integer firstSectionProduct;
    @JsonProperty("first_section_image_url")
    private String firstSectionImageUrl;
    @JsonProperty("second_section")
    private Boolean secondSection;
    @JsonProperty("second_section_product")
    private Integer secondSectionProduct;
    @JsonProperty("second_section_image_url")
    private String secondSectionImageUrl;
    @JsonProperty("slider_body")
    private Boolean sliderBody;
    @JsonProperty("slider_header")
    private String sliderHeader;
    @JsonProperty("slider_images_urls")
    private String[] sliderImagesUrls = null;

}