package com.nasnav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThemeClassDTO extends BaseRepresentationObject{
	//TODO: >>> extending BaseRepresentationObject is not needed, we can add it if we needed it later
	//we shouldn't assume future requirement before it actually exists
	
    private Integer id;
    private String name;

}
