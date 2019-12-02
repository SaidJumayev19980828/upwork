package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode(callSuper=true)
public class OrganizationTagsRepresentationObject extends BaseRepresentationObject {

    private Long id;
    private String alias;
    private String metadata;
    @JsonProperty("p_name")
    private String pname;
    public List<OrganizationTagsRepresentationObject> children;

    public OrganizationTagsRepresentationObject() {
        children = new ArrayList<>();
    }

}
