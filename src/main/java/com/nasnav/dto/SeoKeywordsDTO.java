package com.nasnav.dto;

import com.nasnav.enumerations.SeoEntityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeoKeywordsDTO {
    private SeoEntityType type;
    private Long id;
    private List<String> keywords;
}
