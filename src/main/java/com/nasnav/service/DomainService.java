package com.nasnav.service;

import com.nasnav.dto.request.DomainUpdateDTO;
import com.nasnav.persistence.OrdersEntity;

import java.util.List;

public interface DomainService {

	String getOrganizationDomainAndSubDir();

	String getOrganizationDomainAndSubDir(Long orgId);

	String getCurrentServerDomain();

	String getBackendUrl();

	String buildDashboardOrderPageUrl(Long orderId, Long orgId);

	String buildDashboardReturnRequestPageUrl(Long returnRequestId, Long orgId);

	List<String> getOrganizationDomainOnly(Long orgId);

	void updateDomain(DomainUpdateDTO dto);

}
