package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.exceptions.ErrorCodes.G$ORG$0001;
import static com.nasnav.exceptions.ErrorCodes.G$PRAM$0001;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
	
	@Autowired
	private SecurityService securityService; 
	
	
	@Autowired
	private OrganizationDomainsRepository domainRepo;
	
	@Autowired
	private OrganizationRepository orgRepo;
	
	
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



	@Override
	public void updateDomain(DomainUpdateDTO dto) {
		validateDomain(dto);
		
		Long orgId = dto.getOrganizationId();
		OrganizationDomainsEntity domainEntity = 
				domainRepo
				.findByOrganizationEntity_Id(orgId)
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



	private void validateDomain(DomainUpdateDTO dto) {
		if(anyIsNull(dto.getOrganizationId(), dto.getDomain())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, dto.toString());
		}
	}
}
