package com.nasnav.service.helpers;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.SocialRepository;
import com.nasnav.dto.OrganizationDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.SocialEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class OrganizationServiceHelper  {

    private SocialRepository socialRepository;

    @Autowired
    public OrganizationServiceHelper(SocialRepository socialRepository){
        this.socialRepository = socialRepository;
    }

    public SocialEntity addSocialLinks(OrganizationDTO.OrganizationModificationDTO json, OrganizationEntity organization) throws BusinessException {
        if (json.socialInstagram == null && json.socialTwitter == null && json.socialFacebook == null)
            return null;

        SocialEntity socialEntity = socialRepository.findOneByOrganizationEntity_Id(json.organizationId);
        if (socialEntity == null){
            socialEntity = new SocialEntity();
            socialEntity.setOrganizationEntity(organization);
        }
        if (json.socialTwitter != null) {
            if (StringUtils.validateUrl(json.socialTwitter,
                    "(http(s)?:\\/\\/)?(www\\.)?twitter\\.com\\/[A-z0-9_\\-\\.]+\\/?.*"))
                socialEntity.setTwitter(json.socialTwitter);
            else
                throw new BusinessException("INVALID_PARAM: social_twitter", "the URL is malformed", HttpStatus.NOT_ACCEPTABLE);

        }
        if (json.socialFacebook != null) {
            if (StringUtils.validateUrl(json.socialFacebook,"(http(s)?:\\/\\/)?(www\\.)?(facebook|fb)\\.com\\/[A-z0-9_\\-\\.]+\\/?.*"))
                socialEntity.setFacebook(json.socialFacebook);
            else
                throw new BusinessException("INVALID_PARAM: social_facebook", "the URL is malformed", HttpStatus.NOT_ACCEPTABLE);

        }
        if (json.socialInstagram != null) {
            if (StringUtils.validateUrl(json.socialInstagram,"(http(s)?:\\/\\/)?(www\\.)?instagram\\.com\\/[A-Za-z0-9_\\-\\.]+\\/?.*"))
                socialEntity.setInstagram(json.socialInstagram);
            else
                throw new BusinessException("INVALID_PARAM: social_instagram", "the URL is malformed", HttpStatus.NOT_ACCEPTABLE);

        }
        return socialEntity;
    }
}
