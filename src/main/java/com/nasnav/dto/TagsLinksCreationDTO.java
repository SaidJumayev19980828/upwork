package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TagsLinksCreationDTO {
    @JsonProperty(value = "clear_tree")
    private boolean clearTree;

    @JsonProperty(value = "tags_links")
    private List<TagsLinkDTO> tagsLinks;
}
