package com.nasnav.service;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.nasnav.dao.OrganizationDomainsRepository;
import com.nasnav.persistence.OrganizationDomainsEntity;

@Service
public class DomainServiceImpl implements DomainService{
	
	@Autowired
	private SecurityService securityService; 
	
	
	@Autowired
	private OrganizationDomainsRepository domainRepo;
	
	
	@Override
	public String getOrganizationDomainAndSubDir() {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return getOrganizationDomainAndSubDir(orgId);
	}
	
	
	
	@Override
	public String getOrganizationDomainAndSubDir(Long orgId) {
		return domainRepo
				.findByOrganizationEntity_Id(orgId)
				.flatMap(this::getDomainAndSubDir)
				.map(this::addProtocolIfNeeded)
				.orElse("");
	}
	
	
	
	@Override
	public String getOrganizationDomainOnly(Long orgId) {
		return domainRepo
				.findByOrganizationEntity_Id(orgId)
				.map(OrganizationDomainsEntity::getDomain)
				.map(this::addProtocolIfNeeded)
				.orElse("");
	}
	
	
	
	@Override
	public String getCurrentServerDomain() {
		final String baseUrl = 
				ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
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
		return domain.startsWith("http:") ? domain : "https://"+domain;
	}
}
