package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
public class TagsRepresentationObject extends BaseRepresentationObject {

    private Long id;
    private String name;
    private String alias;
    private String metadata;
    @JsonProperty("p_name")
    private String pname;
    @JsonProperty(value = "category_id")
    private Long categoryId;
    @JsonProperty(value = "org_id")
    private Long orgId;
    private Integer priority;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<TagsRepresentationObject> children;

    private Boolean allowReward;
    private Boolean buyWithCoins;
    private Boolean onlyBuyWithCoins;
    private Long minimumTierId;

    public TagsRepresentationObject() {
        children = new ArrayList<>();
    }

}
