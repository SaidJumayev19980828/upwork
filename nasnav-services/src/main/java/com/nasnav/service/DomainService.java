package com.nasnav.service;

import com.nasnav.dto.request.DomainUpdateDTO;
import org.apache.http.client.utils.URIBuilder;

import java.util.List;

public interface DomainService {

	String getOrganizationDomainAndSubDir();

	String getOrganizationDomainAndSubDir(Long orgId);

	List<DomainUpdateDTO> getOrganizationDomains(Long orgId);

	void deleteOrgDomain(Long id, Long orgId);

	String getCurrentServerDomain();

	String getBackendUrl();

	String buildDashboardOrderPageUrl(Long orderId, Long orgId);

	String buildDashboardReturnRequestPageUrl(Long returnRequestId, Long orgId);

	List<String> getOrganizationDomainOnly(Long orgId);

	void updateDomain(DomainUpdateDTO dto);

	URIBuilder validateDomainCharacters(String inputUrl);

}
