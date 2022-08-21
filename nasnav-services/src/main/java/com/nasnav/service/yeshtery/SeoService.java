package com.nasnav.service.yeshtery;

import com.nasnav.dto.SeoKeywordsDTO;
import com.nasnav.enumerations.SeoEntityType;

import java.util.List;

public interface SeoService {
    List<SeoKeywordsDTO> getSeoKeywords(Long entityId, SeoEntityType type);
}
