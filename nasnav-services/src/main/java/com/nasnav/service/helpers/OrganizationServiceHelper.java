package com.nasnav.service.helpers;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.SocialRepository;
import com.nasnav.dto.OrganizationDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.SocialEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.nasnav.commons.utils.EntityUtils.allIsNull;
import static com.nasnav.exceptions.ErrorCodes.GEN$0012;
import static java.util.Optional.empty;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class OrganizationServiceHelper  {

    private SocialRepository socialRepository;

    @Autowired
    public OrganizationServiceHelper(SocialRepository socialRepository){
        this.socialRepository = socialRepository;
    }

    public Optional<SocialEntity> createSocialEntity(OrganizationDTO.OrganizationModificationDTO json, OrganizationEntity organization) throws BusinessException {
        if (allIsNull(json.socialInstagram, json.socialTwitter, json.socialFacebook, json.socialYoutube, json.socialLnkedin, json.socialPinterest, json.socialWhatsapp)){
            return empty();
        }

        SocialEntity socialEntity =
                socialRepository
                        .findOneByOrganizationEntity_Id(organization.getId())
                        .orElseGet(SocialEntity::new);
        socialEntity.setOrganizationEntity(organization);

        validateAndSetTwitterUrl(json.socialTwitter, socialEntity);
        validateAndSetFacebookUrl(json.socialFacebook, socialEntity);
        validateAndSetInstagramUrl(json.socialInstagram, socialEntity);
        validateAndSetYoutubeUrl(json.socialYoutube, socialEntity);
        validateAndSetLinkedIn(json.socialLnkedin, socialEntity);
        validateAndSetPinterest(json.socialPinterest, socialEntity);
        validateAndSetWhatsapp(json.socialWhatsapp, socialEntity);

        return Optional.of(socialEntity);
    }



    private void validateAndSetTwitterUrl(String url, SocialEntity socialEntity) {
        if (url != null) {
            if (url.equals("")) {
                socialEntity.setTwitter(null);
            }
            else {
                socialEntity.setTwitter(url);
            }
        }
    }



    private void validateAndSetFacebookUrl(String url, SocialEntity socialEntity) {
        if (url != null) {
            if (url.equals("")) {
                socialEntity.setFacebook(null);
            }
            else {
                socialEntity.setFacebook(url);
            }
        }
    }



    private void validateAndSetInstagramUrl(String url, SocialEntity socialEntity) {
        if (url != null) {
            if (StringUtils.validateUrl(url,"(http(s)?:\\/\\/)?(www\\.)?instagram\\.com\\/[A-Za-z0-9_\\-\\.]+\\/?.*")){
                socialEntity.setInstagram(url);
            }else{
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0012, url);
            }
        }
    }



    private void validateAndSetYoutubeUrl(String url, SocialEntity socialEntity) {
        if (url != null){
            if (url.equals("")) {
                socialEntity.setYoutube(null);
            }
            else {
                socialEntity.setYoutube(url);
            }
        }
    }



    private void validateAndSetLinkedIn(String url, SocialEntity socialEntity) {
        if (url != null){
            if (url.equals("")) {
                socialEntity.setLinkedin(null);
            }
            else {
                socialEntity.setLinkedin(url);
            }
        }
    }



    private void validateAndSetPinterest(String url, SocialEntity socialEntity) {
        if (url != null){
            if (url.equals("")) {
                socialEntity.setPinterest(null);
            }
            else {
                socialEntity.setPinterest(url);
            }
        }
    }

    private void validateAndSetWhatsapp(String url, SocialEntity socialEntity) {
        if (url != null){
            if (url.equals("")) {
                socialEntity.setWhatsapp(null);
            }
            else {
                socialEntity.setWhatsapp(url);
            }
        }
    }
}
