package com.nasnav.service.helpers;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.SocialRepository;
import com.nasnav.dto.request.organization.OrganizationModificationDTO;
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
    @Autowired
    private SocialRepository socialRepository;

    public Optional<SocialEntity> createSocialEntity(OrganizationModificationDTO json, OrganizationEntity organization) {
        String instagram = json.getSocialInstagram();
        String twitter = json.getSocialTwitter();
        String facebook = json.getSocialFacebook();
        String youtube = json.getSocialYoutube();
        String linkedin = json.getSocialLinkedin();
        String pinterest = json.getSocialPinterest();
        String tiktok    =json.getSocialTiktok();
        String whatsapp = json.getSocialWhatsapp();

        if (allIsNull(instagram, twitter, facebook, youtube, linkedin, pinterest, tiktok,whatsapp)){
            return empty();
        }

        SocialEntity socialEntity =
                socialRepository
                        .findOneByOrganizationEntity_Id(organization.getId())
                        .orElseGet(SocialEntity::new);
        socialEntity.setOrganizationEntity(organization);

        validateAndSetTwitterUrl(twitter, socialEntity);
        validateAndSetFacebookUrl(facebook, socialEntity);
        validateAndSetInstagramUrl(instagram, socialEntity);
        validateAndSetYoutubeUrl(youtube, socialEntity);
        validateAndSetLinkedIn(linkedin, socialEntity);
        validateAndSetPinterest(pinterest, socialEntity);
        validateAndSetTiktok(tiktok,socialEntity);
        validateAndSetWhatsapp(whatsapp, socialEntity);

        return Optional.of(socialEntity);
    }

    private void validateAndSetTwitterUrl(String url, SocialEntity socialEntity) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(url)) {
            socialEntity.setTwitter(url);
        }else {
            socialEntity.setTwitter(null);
        }
    }

    private void validateAndSetFacebookUrl(String url, SocialEntity socialEntity) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(url)) {
            socialEntity.setFacebook(url);
        }else {
            socialEntity.setFacebook(null);
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
        if (org.apache.commons.lang3.StringUtils.isNotBlank(url)) {
            socialEntity.setYoutube(url);
        }else {
            socialEntity.setYoutube(null);
        }
    }

    private void validateAndSetLinkedIn(String url, SocialEntity socialEntity) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(url)) {
            socialEntity.setLinkedin(url);
        }else {
            socialEntity.setLinkedin(null);
        }
    }

    private void validateAndSetPinterest(String url, SocialEntity socialEntity) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(url)) {
            socialEntity.setPinterest(url);
        }else {
            socialEntity.setPinterest(null);
        }
    }

    private void validateAndSetTiktok(String url, SocialEntity socialEntity) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(url)) {
            socialEntity.setTiktok(url);
        }else {
            socialEntity.setTiktok(null);
        }
    }

    private void validateAndSetWhatsapp(String url, SocialEntity socialEntity) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(url)) {
            socialEntity.setWhatsapp(url);
        }else {
            socialEntity.setWhatsapp(null);
        }
    }
}
