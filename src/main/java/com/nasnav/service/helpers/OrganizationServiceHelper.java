package com.nasnav.service.helpers;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.SocialRepository;
import com.nasnav.dto.OrganizationDTO;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.SocialEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class OrganizationServiceHelper  {

    private SocialRepository socialRepository;

    @Autowired
    public OrganizationServiceHelper(SocialRepository socialRepository){
        this.socialRepository = socialRepository;
    }

    public String[] addSocialLinks(OrganizationDTO.OrganizationModificationDTO json, OrganizationEntity organization){
        if (json.socialInstagram == null && json.socialTwitter == null && json.socialFacebook == null)
            return new String[]{"0","",""};

        SocialEntity socialEntity = socialRepository.findOneByOrganizationEntity_Id(json.organizationId);
        if (socialEntity == null){
            socialEntity = new SocialEntity();
            socialEntity.setCreatedAt(new Date());
        }
        socialEntity.setOrganizationEntity(organization);
        if (json.socialTwitter != null) {
            if (StringUtils.validateUrl(json.socialTwitter, "((https?://)?(www\\.)?twitter\\.com/)?(@|#!/)?([A-Za-z0-9_]{1,15})(/([-a-z]{1,20}))?")) {
                socialEntity.setTwitter(json.socialTwitter);
            } else {
                return new String[]{"1", "INVALID_PARAM: social_twitter", "the URL is malformed"};
            }
        }
        if (json.socialFacebook != null) {
            if (StringUtils.validateUrl(json.socialFacebook, "http(s)?:\\/\\/(www\\.)?(facebook|fb)\\.com\\/[A-z0-9_\\-\\.]+\\/?")) {
                socialEntity.setFacebook(json.socialFacebook);
            } else {
                return new String[]{"1", "INVALID_PARAM: social_facebook", "the URL is malformed"};
            }
        }
        if (json.socialInstagram != null) {
            if (StringUtils.validateUrl(json.socialInstagram,
                    "https?:\\/\\/(www\\.)?instagram\\.com\\/([A-Za-z0-9_](?:(?:[A-Za-z0-9_]|(?:\\.(?!\\.))){0,28}(?:[A-Za-z0-9_]))?)")) {
                socialEntity.setInstagram(json.socialInstagram);
            } else {
                return new String[]{"1", "INVALID_PARAM: social_instagram", "the URL is malformed"};
            }
        }
        socialEntity.setUpdatedAt(new Date());
        socialRepository.save(socialEntity);
        return new String[]{"0","",""};
    }
}
