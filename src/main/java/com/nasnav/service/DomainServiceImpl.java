package com.nasnav.service;

import static com.nasnav.cache.Caches.ORGANIZATIONS_DOMAINS;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.enumerations.Roles.NASNAV_ADMIN;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.nasnav.AppConfig;
import com.nasnav.persistence.*;
import org.apache.http.client.utils.URIBuilder;
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
				.findByOrganizationEntity_IdOrderByPriorityDescIdDesc(orgId)
				.stream()
				.findFirst()
				.flatMap(this::getDomainAndSubDir)
				.map(this::addProtocolIfNeeded)
				.orElse("");
	}

	@Override
	public List<DomainUpdateDTO> getOrganizationDomains(Long orgId) {
		orgId = validateAndReturnAdminOrgId(orgId);
		return domainRepo
				.findByOrganizationEntity_IdOrderByPriorityDescIdDesc(orgId)
				.stream()
				.map(this::toDomainDTO)
				.collect(toList());
	}

	private DomainUpdateDTO toDomainDTO(OrganizationDomainsEntity domainsEntity) {
		String domain = addProtocolIfNeeded(domainsEntity.getDomain());
		DomainUpdateDTO dto = new DomainUpdateDTO();
		dto.setId(domainsEntity.getId());
		dto.setDomain(domain);
		dto.setSubDirectroy(domainsEntity.getSubdir());
		dto.setPriority(domainsEntity.getPriority());
		return dto;
	}

	@Override
	public void deleteOrgDomain(Long id, Long orgId) {
		domainRepo.deleteByIdAndOrganizationEntity_Id(id, orgId);
	}

	private Long validateAndReturnAdminOrgId(Long orgId) {
		if (!securityService.userHasRole(NASNAV_ADMIN)) {
			return securityService.getCurrentUserOrganizationId();
		}
		return orgId;
	}

	@Override
	public List<String> getOrganizationDomainOnly(Long orgId) {
		return domainRepo
				.findByOrganizationEntity_IdOrderByPriorityDescIdDesc(orgId)
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

		Long id = ofNullable(dto.getId()).orElse(-1L);
		Long orgId = dto.getOrganizationId();
		String domain = dto.getDomain().startsWith("http") ? dto.getDomain(): "http://" + dto.getDomain();
		String subDir = ofNullable(dto.getSubDirectroy()).orElse("");
		domain = validateDomainCharacters(domain + "/" + subDir).getHost();
		OrganizationDomainsEntity domainEntity =
				domainRepo
						.findByIdAndOrganizationEntity_Id(id, orgId)
						.orElse(new OrganizationDomainsEntity());
		if (domainRepo.existsByDomainAndSubdir(domain, dto.getSubDirectroy()) && domainEntity.getId() == null) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0021, dto.getDomain()+","+ dto.getSubDirectroy());
		}

		OrganizationEntity organizationEntity =
				orgRepo
						.findById(dto.getOrganizationId())
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, G$ORG$0001, orgId));

		Integer priority = ofNullable(dto.getPriority()).orElse(0);

		domainEntity.setDomain(domain);
		domainEntity.setSubdir(dto.getSubDirectroy());
		domainEntity.setOrganizationEntity(organizationEntity);
		domainEntity.setPriority(priority);

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
		if (dto.getPriority() != null && dto.getPriority() == 1) {
			domainRepo.resetOrganizationDomainsCanonical(dto.getOrganizationId());
		}
	}

	public URIBuilder validateDomainCharacters(String inputUrl) {
		try {
			URIBuilder url = new URIBuilder(inputUrl);
			return url;
		} catch (URISyntaxException e) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0005);
		}
	}
}
