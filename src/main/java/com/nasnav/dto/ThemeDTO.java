package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.persistence.ThemeClassEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Data
public class ThemeDTO extends BaseRepresentationObject{

	//TODO: >>> extending BaseRepresentationObject is not needed, we can add it if we needed it later
	//we shouldn't assume future requirement before it actually exists
	
	//TODO: >>> remove unused imports, there should be a shortcut in intellij that do that for you.s
		
    private Integer id;
    private String name;

    @JsonProperty("preview_image")
    private String previewImage;

    @JsonProperty("default_settings")
    private String defaultSettings;

    @JsonProperty("theme_class_id")
    private Integer themeClassId;
}
