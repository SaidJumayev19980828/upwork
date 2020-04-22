package com.nasnav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThemeClassDTO extends BaseRepresentationObject{
	
    private Integer id;
    private String name;

}
