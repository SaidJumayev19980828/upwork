package com.nasnav.service;

import com.nasnav.dao.OrganizationImagesRepository;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.OrganizationImagesEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@Service
public class OrderEmailServiceHelper {

    @Autowired
    private OrganizationImagesRepository orgImagesRepo;

    public String getOrganizationLogo(OrganizationEntity org) {
        return getOrganizationLogo(ofNullable(org));
    }



    public String getOrganizationLogo(Optional<OrganizationEntity> org) {
        return org
                .map(OrganizationEntity::getId)
                .map(orgId -> orgImagesRepo.findByOrganizationEntityIdAndShopsEntityNullAndTypeOrderByIdDesc(orgId, 1))
                .map(List::stream)
                .flatMap(Stream::findFirst)
                .map(OrganizationImagesEntity::getUri)
                .orElse("nasnav-logo.png");
    }
}
