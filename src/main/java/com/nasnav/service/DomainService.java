package com.nasnav.service;

public interface DomainService {

	String getOrganizationDomainAndSubDir();

	String getOrganizationDomainAndSubDir(Long orgId);

	String getCurrentServerDomain();

	String getOrganizationDomainOnly(Long orgId);
}
