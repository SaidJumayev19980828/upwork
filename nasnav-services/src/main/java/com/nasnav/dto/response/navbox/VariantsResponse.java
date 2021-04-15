package com.nasnav.dto.response.navbox;

import com.nasnav.dto.VariantDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class VariantsResponse {
    private Long total;
    private List<VariantDTO> variants;
}
