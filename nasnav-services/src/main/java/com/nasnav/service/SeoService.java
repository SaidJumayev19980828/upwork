package com.nasnav.service;

import com.nasnav.dto.SeoKeywordsDTO;
import com.nasnav.enumerations.SeoEntityType;

import java.util.List;

public interface SeoService {
    void addSeoKeywords(SeoKeywordsDTO seoKeywords);
    List<SeoKeywordsDTO> getSeoKeywords(Long orgId, Long entityId, SeoEntityType type);
}
