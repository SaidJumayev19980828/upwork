package com.nasnav.service;

import com.nasnav.dto.request.DomainUpdateDTO;

import java.util.List;

public interface DomainService {

	String getOrganizationDomainAndSubDir();

	String getOrganizationDomainAndSubDir(Long orgId);

	String getCurrentServerDomain();

	List<String> getOrganizationDomainOnly(Long orgId);

	void updateDomain(DomainUpdateDTO dto);
}
