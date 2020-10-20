package com.nasnav.service;

import static com.nasnav.cache.Caches.ORGANIZATIONS_DOMAINS;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.exceptions.ErrorCodes.G$ORG$0001;
import static com.nasnav.exceptions.ErrorCodes.G$PRAM$0001;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

import com.nasnav.AppConfig;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.ReturnRequestEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.nasnav.dao.OrganizationDomainsRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dto.request.DomainUpdateDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationDomainsEntity;
import com.nasnav.persistence.OrganizationEntity;

@Service
public class DomainServiceImpl implements DomainService{
	
	private static Logger logger = LogManager.getLogger();
	
	@Autowired
	private SecurityService securityService; 
	
	
	@Autowired
	private OrganizationDomainsRepository domainRepo;
	
	@Autowired
	private OrganizationRepository orgRepo;

	@Autowired
	private AppConfig appConfig;
	
	
	@Override
	public String getOrganizationDomainAndSubDir() {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return getOrganizationDomainAndSubDir(orgId);
	}
	
	
	
	@Override
	public String getOrganizationDomainAndSubDir(Long orgId) {
		return domainRepo
				.findByOrganizationEntity_IdOrderByIdDesc(orgId)
				.stream()
				.findFirst()
				.flatMap(this::getDomainAndSubDir)
				.map(this::addProtocolIfNeeded)
				.orElse("");
	}
	
	
	
	@Override
	public List<String> getOrganizationDomainOnly(Long orgId) {
		return domainRepo
				.findByOrganizationEntity_IdOrderByIdDesc(orgId)
				.stream()
				.map(OrganizationDomainsEntity::getDomain)
				.map(this::addProtocolIfNeeded)
				.collect(toList());
	}



	
	
	
	@Override
	public String getCurrentServerDomain() {
		String baseUrl = "";
		try{
			baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
		}catch(Throwable t) {
			logger.error(t,t);					
			try {
				baseUrl = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				logger.error(e,e);	
			}
		}
		return ofNullable(baseUrl)
				.map(this::addProtocolIfNeeded)
				.orElse("");
	}
	
	
	
	
	
	private Optional<String> getDomainAndSubDir(OrganizationDomainsEntity orgDomain) {		
		Optional<String> domain  = 
				ofNullable(orgDomain)
				.map(OrganizationDomainsEntity::getDomain);
		Optional<String> subDir = 
				ofNullable(orgDomain)
				.map(OrganizationDomainsEntity::getSubdir);
		return domain
				.map(d -> 
					subDir
					.map(s -> format("%s/%s", d, s))
					.orElse(d) );		
	}
	
	
	
	
	
	private String addProtocolIfNeeded(String domain) {
		return domain.startsWith("http://") || domain.startsWith("https://") ? domain : "https://"+domain;
	}



	@Override
	@CacheEvict(cacheNames = {ORGANIZATIONS_DOMAINS})
	public void updateDomain(DomainUpdateDTO dto) {
		validateDomain(dto);
		
		Long orgId = dto.getOrganizationId();
		OrganizationDomainsEntity domainEntity = 
				domainRepo
				.findByOrganizationEntity_IdOrderByIdDesc(orgId)
				.stream()
				.findFirst()
				.orElse(new OrganizationDomainsEntity());
		
		OrganizationEntity organizationEntity = 
				orgRepo
				.findById(dto.getOrganizationId())
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, G$ORG$0001, orgId));
		
		domainEntity.setDomain(dto.getDomain());
		domainEntity.setSubdir(dto.getSubDirectroy());
		domainEntity.setOrganizationEntity(organizationEntity);
		
		domainRepo.save(domainEntity);
	}


	@Override
	public String getBackendUrl() {
		String backendUrl = ofNullable(appConfig.environmentHostName)
							.orElse(getCurrentServerDomain());
		return addProtocolIfNeeded(backendUrl);
	}



	@Override
	public String buildDashboardOrderPageUrl(Long orderId, Long orgId) {
		String domain =
				getOrganizationDomainOnly(orgId)
				.stream()
				.findFirst()
				.orElse("");
		String orderIdString =
				ofNullable(orderId)
				.map(id -> id.toString())
				.orElse("null");
		String path = appConfig.dashBoardOrderPageUrl.replace("{order_id}", orderIdString);
		return format("%s/%s", domain, path);
	}



	@Override
	public String buildDashboardReturnRequestPageUrl(Long returnRequestId, Long orgId) {
		String domain =
				getOrganizationDomainOnly(orgId).stream()
				.findFirst()
				.orElse("");
		String path = "dashboard/return-requests";
		return format("%s/%s/%d", domain, path, returnRequestId);
	}



	private void validateDomain(DomainUpdateDTO dto) {
		if(anyIsNull(dto.getOrganizationId(), dto.getDomain())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, dto.toString());
		}
	}
}
