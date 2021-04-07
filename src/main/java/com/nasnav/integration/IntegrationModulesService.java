package com.nasnav.integration;

import com.nasnav.dao.IntegrationParamRepository;
import com.nasnav.dao.IntegrationParamTypeRepostory;
import com.nasnav.dto.OrganizationIntegrationInfoDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.enums.IntegrationParam;
import com.nasnav.integration.model.OrganizationIntegrationInfo;
import com.nasnav.persistence.IntegrationParamEntity;
import com.nasnav.persistence.IntegrationParamTypeEntity;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;

import static com.nasnav.commons.utils.EntityUtils.failSafeFunction;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.*;
import static com.nasnav.integration.enums.IntegrationParam.DISABLED;
import static java.util.stream.Collectors.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@Service
@Scope(value = SCOPE_SINGLETON)
public class IntegrationModulesService {
    private final Logger logger = Logger.getLogger(getClass());

    private Map<Long, OrganizationIntegrationInfo> orgIntegration;
    private Set<IntegrationParamTypeEntity> mandatoryIntegrationParams;

    @Autowired
    private IntegrationParamRepository paramRepo;

    @Autowired
    private IntegrationUtils utils;

    @Autowired
    private IntegrationParamTypeRepostory paramTypeRepo;

    @PostConstruct
    public void init() throws BusinessException {
        orgIntegration = new HashMap<>();
        mandatoryIntegrationParams = getMandatoryParams();
    }



    public void loadIntegrationModules(IntegrationService integrationService) throws BusinessException {
        List<Long> integratedOrgs = getIntegratedOrganizations();
        for(Long orgId: integratedOrgs) {
            try {
                loadOrganizationIntegrationModule(orgId, integrationService);
            }
            catch(Exception e) {
                String msg = String.format(ERR_INTEGRATION_MODULE_LOAD_FAILED, orgId);
                logger.error(msg, e);
                utils.throwIntegrationInitException(msg);
            }
        }
    }



    public void loadOrganizationIntegrationModule(Long orgId, IntegrationService integrationService) throws BusinessException {
        OrganizationIntegrationInfo integration = getOrganizationIntegrationInfo(orgId, integrationService);
        this.orgIntegration.put(orgId, integration);
    }



    public IntegrationModule getIntegrationModule(Long orgId) {
        return this.orgIntegration.get(orgId).getIntegrationModule();
    }



    public OrganizationIntegrationInfo getIntegrationInfo(Long orgId) {
        return orgIntegration.get(orgId);
    }


    public boolean hasIntegrationModule(Long orgId){
        return orgIntegration.containsKey(orgId);
    }



    public List<Long> getIntegratedOrganizations() {
        String paramName = IntegrationParam.INTEGRATION_MODULE.getValue();
        return paramRepo.findByType_typeName( paramName )
                .stream()
                .map(IntegrationParamEntity::getOrganizationId)
                .collect(toList());
    }



    @Transactional
    public void removeIntegrationModule(Long organizationId) {
        paramRepo.deleteByOrganizationId(organizationId);
        orgIntegration.remove(organizationId);
    }



    public void clearAllIntegrationModules() {
        orgIntegration.clear();
    }



    public List<OrganizationIntegrationInfoDTO> getAllIntegrationModules() {
        return orgIntegration.entrySet()
                .stream()
                .map(this::toOrganizationIntegrationInfoDTO)
                .collect(toList());
    }



    private OrganizationIntegrationInfoDTO toOrganizationIntegrationInfoDTO(Map.Entry<Long, OrganizationIntegrationInfo> infoEntry) {
        OrganizationIntegrationInfoDTO dto = new OrganizationIntegrationInfoDTO();
        Long orgId = infoEntry.getKey();
        OrganizationIntegrationInfo info = infoEntry.getValue();
        Map<String,String> params = info.getParameters()
                .stream()
                .collect(toMap(IntegrationParamEntity::getParameterTypeName
                        , IntegrationParamEntity::getParamValue));
        dto.setOrganizationId(orgId);
        dto.setIntegrationModule(info.getIntegrationModule().getClass().getName());
        dto.setMaxRequestRate(1000/info.getRequestMinDelayMillis().intValue());
        dto.setIntegrationParameters(params);

        return dto;
    }



    private boolean isModuleDisabled(List<IntegrationParamEntity> params) {
        return Optional.ofNullable(params)
                .orElseGet(ArrayList::new)
                .stream()
                .map(IntegrationParamEntity::getType)
                .map(IntegrationParamTypeEntity::getTypeName)
                .anyMatch( name -> Objects.equals(name, DISABLED.getValue()) );
    }



    private Long getOrgMinRequestDelay(List<IntegrationParamEntity> params) {
        return params.stream()
                .filter( param -> Objects.equals( param.getType().getTypeName() , IntegrationParam.MAX_REQUEST_RATE.getValue()))
                .map( IntegrationParamEntity::getParamValue )
                .map( failSafeFunction(Long::valueOf) )
                .filter( Objects::nonNull )
                .map( this::calcMinRequestDelayMillis )
                .findFirst()
                .orElse(0L);
    }



    private Long calcMinRequestDelayMillis(Long requestRatePerSec) {
        if(requestRatePerSec == null || requestRatePerSec == 0)
            return 0L;
        else
            return (long) ((1.0/requestRatePerSec)*1000);
    }



    private List<IntegrationParamEntity> getOrgIntegrationParams(Long orgId) throws BusinessException {
        List<IntegrationParamEntity> params = paramRepo.findByOrganizationId(orgId);

        validateLoadedIntegrationParams(orgId, params);
        return params;
    }



    private void validateLoadedIntegrationParams(Long orgId, List<IntegrationParamEntity> params) throws BusinessException {
        if(params == null || params.isEmpty()) {
            utils.throwIntegrationInitException(ERR_NO_INTEGRATION_PARAMS, orgId);
        }
        if(!hasMandatoryParams(params)) {
            utils.throwIntegrationInitException(ERR_MISSING_MANDATORY_PARAMS, orgId);
        }
    }



    private Boolean hasMandatoryParams(List<IntegrationParamEntity> params) {
        Set<String> mandatoryParamNames =
                mandatoryIntegrationParams
                        .stream()
                        .map(IntegrationParamTypeEntity::getTypeName)
                        .collect(toSet());
        return params
                .stream()
                .map(IntegrationParamEntity::getType)
                .map(IntegrationParamTypeEntity::getTypeName)
                .collect(toSet())
                .containsAll(mandatoryParamNames);
    }



    private Set<IntegrationParamTypeEntity> getMandatoryParams() {
        return paramTypeRepo.findByIsMandatory(true);
    }


    private IntegrationModule loadIntegrationModuleClass(Long orgId, String integrationModuleClass, IntegrationService integrationService
    ) throws BusinessException {
        IntegrationModule integrationModule = null;
        try {
            @SuppressWarnings("unchecked")
            Class<IntegrationModule> moduleClass = (Class<IntegrationModule>) this.getClass().getClassLoader().loadClass(integrationModuleClass);
            integrationModule = moduleClass.getDeclaredConstructor(IntegrationService.class).newInstance(integrationService);
        } catch (Throwable e) {
            logger.error(e,e);
            utils.throwIntegrationInitException(ERR_LOADING_INTEGRATION_MODULE_CLASS, integrationModuleClass, orgId);
        }

        return integrationModule;
    }



    private String getIntegrationModuleClassName(Long orgId, List<IntegrationParamEntity> params) throws BusinessException {
        String integrationModuleClass = params.stream()
                .filter(this::isIntegrationModuleNameParam)
                .map(IntegrationParamEntity::getParamValue)
                .findFirst()
                .orElseThrow(() -> utils.getIntegrationInitException(ERR_NO_INTEGRATION_MODULE, orgId));
        return integrationModuleClass;
    }



    private boolean isIntegrationModuleNameParam(IntegrationParamEntity p) {
        return Objects.equals(p.getType().getTypeName(), IntegrationParam.INTEGRATION_MODULE.getValue() );
    }



    private IntegrationModule loadIntegrationModule(Long orgId, List<IntegrationParamEntity> params, IntegrationService integrationService)
            throws BusinessException {
        String integrationModuleClass = getIntegrationModuleClassName(orgId,params);

        return loadIntegrationModuleClass(orgId, integrationModuleClass, integrationService);
    }



    private OrganizationIntegrationInfo getOrganizationIntegrationInfo(Long orgId, IntegrationService integrationService) throws BusinessException {

        List<IntegrationParamEntity> params = getOrgIntegrationParams(orgId);
        IntegrationModule integrationModule = loadIntegrationModule(orgId, params, integrationService);
        Long minRequestDelay = getOrgMinRequestDelay(params);

        OrganizationIntegrationInfo info = new OrganizationIntegrationInfo(integrationModule, minRequestDelay, params);
        info.setDisabled( isModuleDisabled(params));

        return info;
    }
}
