package com.nasnav.service;

import java.util.Optional;

import com.nasnav.persistence.OrganizationEntity;

public interface OrderEmailServiceHelper {

  String getOrganizationLogo(OrganizationEntity org);

  String getOrganizationLogo(Optional<OrganizationEntity> org);

}