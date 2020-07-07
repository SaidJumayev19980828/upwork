package com.nasnav.service;

public interface DomainService {

	String getOrganizationDomain();

	String getOrganizationDomain(Long orgId);

	String getCurrentServerDomain();
}
