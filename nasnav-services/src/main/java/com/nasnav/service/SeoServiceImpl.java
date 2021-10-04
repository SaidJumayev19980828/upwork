package com.nasnav.service;

import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.SeoKeywordRepository;
import com.nasnav.dao.TagsRepository;
import com.nasnav.dto.SeoKeywordsDTO;
import com.nasnav.enumerations.SeoEntityType;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.SeoKeywordEntity;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.cache.annotation.CacheResult;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.nasnav.cache.Caches.SEO_KEYWORDS;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.enumerations.SeoEntityType.*;
import static com.nasnav.exceptions.ErrorCodes.G$PRAM$0001;
import static com.nasnav.exceptions.ErrorCodes.SEO$ADD$0001;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;


@Service
public class SeoServiceImpl implements SeoService{


    @Autowired
    private SecurityService securityService;

    @Autowired
    private TagsRepository tagsRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private SeoKeywordRepository seoRepo;

    @Autowired
    OrganizationService organizationService;


    @Override
    public void addSeoKeywords(SeoKeywordsDTO seoKeywords) {
        if(anyIsNull(seoKeywords, seoKeywords.getId(), seoKeywords.getType(), seoKeywords.getKeywords())){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, seoKeywords.toString());
        }
        validateSeoEntity(seoKeywords);

        var currentUserOrg = securityService.getCurrentUserOrganizationId();
        var entityId = seoKeywords.getId();
        var typeId = seoKeywords.getType().getValue();

        seoRepo.deleteByEntityIdAndTypeAndOrganization_Id(entityId, typeId, currentUserOrg);

        seoKeywords
            .getKeywords()
            .stream()
            .map(keyword -> createSeoEntity(entityId, typeId, keyword))
            .collect(collectingAndThen(toList(), seoRepo::saveAll));

    }



    private SeoKeywordEntity createSeoEntity(Long entityId, Integer typeId, String keyword) {
        var org = securityService.getCurrentUserOrganization();
        var seoKeywordEntity = new SeoKeywordEntity();
        seoKeywordEntity.setEntityId(entityId);
        seoKeywordEntity.setKeyword(keyword);
        seoKeywordEntity.setOrganization(org);
        seoKeywordEntity.setTypeId(typeId);
        return seoKeywordEntity;
    }



    private void validateSeoEntity(SeoKeywordsDTO seoKeywords) {
        var entityId = seoKeywords.getId();
        switch(seoKeywords.getType()){
            case ORGANIZATION: validateOrganizationEntity(entityId); break;
            case PRODUCT: validateProductEntity(entityId); break;
            case TAG: validateTagEntity(entityId);
        }
    }



    private void validateTagEntity(Long entityId) {
        var currentUserOrg = securityService.getCurrentUserOrganizationId();
        if(!tagsRepo.existsByIdAndOrganizationEntity_Id(entityId, currentUserOrg)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, SEO$ADD$0001, TAG.name(), entityId, currentUserOrg);
        }
    }



    private void validateProductEntity(Long entityId) {
        var currentUserOrg = securityService.getCurrentUserOrganizationId();
        if(!productRepo.existsByIdAndOrganizationId(entityId, currentUserOrg)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, SEO$ADD$0001, PRODUCT.name(), entityId, currentUserOrg);
        }
    }



    private void validateOrganizationEntity(Long entityId) {
        var currentUserOrg = securityService.getCurrentUserOrganizationId();
        if(!Objects.equals(currentUserOrg, entityId)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, SEO$ADD$0001, ORGANIZATION.name(), entityId, currentUserOrg);
        }
    }



    @Override
    @CacheResult(cacheName = SEO_KEYWORDS)
    public List<SeoKeywordsDTO> getSeoKeywords(Long orgId, Long entityId, SeoEntityType type) {
        if(isNull(orgId) || (isNull(entityId) ^ isNull(type))){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, format("{orgId:%d, entityId:%d, type:%s}", orgId, entityId, type));
        }

        List<SeoKeywordEntity> seoKeywordEntities = emptyList();
        if(noneIsNull(entityId, type, orgId)){
            seoKeywordEntities = seoRepo.findByEntityIdAndTypeIdAndOrganization_Id(entityId, type.getValue(), orgId);
        } else if(nonNull(orgId)){
            seoKeywordEntities = seoRepo.findByOrganization_Id(orgId);
        } else if(noneIsNull(entityId, type)){
            seoKeywordEntities = seoRepo.findByEntityIdAndTypeId(entityId, type.getValue());
        }

        return toSeoKeywordsDTOList(seoKeywordEntities);
    }

    @Override
    public List<SeoKeywordsDTO> getSeoKeywords(Long entityId, SeoEntityType type) {
        return  organizationService.getYeshteryOrgs().stream()
                .map(org -> getSeoKeywords(org.getId(), entityId, type))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

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