package com.nasnav.yeshtery.services.impl;

import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.SeoKeywordRepository;
import com.nasnav.dao.TagsRepository;
import com.nasnav.dto.SeoKeywordsDTO;
import com.nasnav.enumerations.SeoEntityType;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.SeoKeywordEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.yeshtery.services.interfaces.SeoService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.nasnav.commons.utils.CollectionUtils.mapInBatches;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.enumerations.SeoEntityType.*;
import static com.nasnav.exceptions.ErrorCodes.G$PRAM$0001;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;


@Service("yeshterySeoServiceImpl")
public class SeoServiceImpl implements SeoService {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TagsRepository tagsRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private SeoKeywordRepository seoRepo;



    @Override
    public List<SeoKeywordsDTO> getSeoKeywords(Long entityId, SeoEntityType type) {
        if(anyIsNull(entityId, type)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, format("{entityId:%d, type:%s}", entityId, type));
        }

        List<SeoKeywordEntity> seoKeywordEntities;
        if(CATEGORY.equals(type)){
            seoKeywordEntities = getCategorySeoKeywords(entityId);
        }else if(ORGANIZATION.equals(type)){
            seoKeywordEntities = seoRepo.findByOrganization_Id(entityId);
        }else{
            seoKeywordEntities = seoRepo.findByEntityIdAndTypeId(entityId, type.getValue());
        }

        return toSeoKeywordsDTOList(seoKeywordEntities);
    }


    private List<SeoKeywordEntity> getCategorySeoKeywords(Long categoryId) {
        var categoryTags = tagsRepo.findByCategoryId(categoryId);
        return mapInBatches(categoryTags
                , 5000
                , tags -> seoRepo.findByEntityIdInAndTypeId(tags, TAG.getValue()));
    }



    private List<SeoKeywordsDTO> toSeoKeywordsDTOList(List<SeoKeywordEntity> seoKeywordEntities) {
        return seoKeywordEntities
                .stream()
                .collect(groupingBy(this::seoEntityKey))
                .entrySet()
                .stream()
                .map(this::toSeoKeywordsDTO)
                .collect(toList());
    }


    private SeoKeywordsDTO toSeoKeywordsDTO(Map.Entry<SeoEntityKey,List<SeoKeywordEntity>> seoEntityKeywords){
        var commonData = seoEntityKeywords.getKey();
        var keywords = getKeywords(seoEntityKeywords);
        var dto = new SeoKeywordsDTO();
        dto.setId(commonData.getEntityId());
        dto.setType(findEnum(commonData.getSeoTypeId()));
        dto.setKeywords(keywords);
        return dto;
    }



    private List<String> getKeywords(Map.Entry<SeoEntityKey, List<SeoKeywordEntity>> seoEntityKeywords) {
        return ofNullable(seoEntityKeywords)
                .map(Map.Entry::getValue)
                .orElse(emptyList())
                .stream()
                .map(SeoKeywordEntity::getKeyword)
                .collect(toList());
    }



    private SeoEntityKey seoEntityKey(SeoKeywordEntity entity){
        var key = new SeoEntityKey();
        key.setEntityId(entity.getEntityId());
        key.setSeoTypeId(entity.getTypeId());
        key.setOrganizationId(entity.getOrganization().getId());
        return key;
    }
}


@Data
class SeoEntityKey{
    private Long organizationId;
    private Long entityId;
    private Integer seoTypeId;
}