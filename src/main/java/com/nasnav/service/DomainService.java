package com.nasnav.service;

import com.nasnav.dto.request.DomainUpdateDTO;

public interface DomainService {

	String getOrganizationDomainAndSubDir();

	String getOrganizationDomainAndSubDir(Long orgId);

	String getCurrentServerDomain();

	String getOrganizationDomainOnly(Long orgId);

	void updateDomain(DomainUpdateDTO dto);
}
