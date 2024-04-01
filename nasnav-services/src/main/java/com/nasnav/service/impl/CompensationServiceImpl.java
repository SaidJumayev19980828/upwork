package com.nasnav.service.impl;

import com.nasnav.commons.utils.CustomPaginationPageRequest;
import com.nasnav.dao.CompensationActionsEntityRepository;
import com.nasnav.dao.CompensationRulesRepository;
import com.nasnav.dto.CompensationAction;
import com.nasnav.dto.CompensationRule;
import com.nasnav.dto.RuleTier;
import com.nasnav.enumerations.CompensationActions;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.CompensationActionsEntity;
import com.nasnav.persistence.CompensationRuleTierEntity;
import com.nasnav.persistence.CompensationRulesEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.service.CompensationService;
import com.nasnav.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.nasnav.exceptions.ErrorCodes.COMPEN$001;
import static com.nasnav.exceptions.ErrorCodes.COMPEN$002;

@Service
@RequiredArgsConstructor
public class CompensationServiceImpl implements CompensationService {
    private final CompensationActionsEntityRepository actionRepository;
    private final CompensationRulesRepository ruleRepository;
    private final SecurityService security;

    /**
     * Get all the compensation actions available in the system.
     * This is used to populate the dropdown in the UI.
     * @return List of all the actions available in the system.
     * @see CompensationActions#values()
     */
    @Override
    public List<CompensationActions> getCompensationActions() {
        return List.of(CompensationActions.values());
    }

    /**
     * Create a new compensation action.
     * This is used to populate the dropdown in the UI.
     * @param dto Compensation action to be created.
     * @throws RuntimeBusinessException if the action already exists.
     * @return the created action.
     * @see CompensationActionsEntity
     */
    @Override
    public CompensationActionsEntity createAction(CompensationAction dto) {
       return actionRepository.save(buildAction(dto));
    }

    /**
     * Get a compensation action by id.
     * This is used to populate the dropdown in the UI.
     * @param id Id of the compensation action to be retrieved.
     * @throws RuntimeBusinessException if the action does not exist.
     * @return the compensation action.
     * @see CompensationActionsEntity
     */
    @Override
    public CompensationActionsEntity getAction(long id) {
        return actionRepository.findById(id).orElseThrow(()-> new RuntimeBusinessException(HttpStatus.NOT_FOUND,COMPEN$001,id));
    }

    /**
     * Get all the compensation actions available in the system.
     * This is used to populate the dropdown in the UI.
     * @see CompensationActionsEntity
     * @return List of all the actions available in the system.
     */
    @Override
    public List<CompensationActionsEntity> getAllActions() {
        return actionRepository.findAll();
    }

    /**
     * Create a new compensation rule.
     * @param dto Compensation rule to be created.
     * @throws RuntimeBusinessException if the action did not exist or Unique Exception From DB.
     * @see CompensationRulesEntity
     */
    @Override
    public CompensationRulesEntity createRule(CompensationRule dto) {
       return ruleRepository.save(buildRule(dto));
    }

    /**
     * Get a compensation rule by id.
     * @param id Id of the compensation rule to be retrieved.
     * @throws RuntimeBusinessException if the rule does not exist.
     * @return the compensation rule.
     * @see CompensationRulesEntity
     */
    @Override
    public CompensationRulesEntity getRule(long id) {
        return ruleRepository.findByIdAndOrganization(id,loggedInUserOrg()).orElseThrow(()->new RuntimeBusinessException(HttpStatus.NOT_FOUND,COMPEN$002,id));
    }

    /**
     * Get all the compensation rules available in the system based on current logged-in user organization.
     * @param start Page number to be retrieved.
     * @param count Number of rules to be retrieved.
     * @return Pagination of all the rules available in the system.
     * @see CompensationRulesEntity
     */
    @Override
    public PageImpl<CompensationRulesEntity> getAllRules(int start, int count) {
        CustomPaginationPageRequest pageRequest = new CustomPaginationPageRequest(start,count);
        return ruleRepository.findAllByOrganization(loggedInUserOrg(),pageRequest);
    }

    /**
     * Get the logged-in user organization.
     * This is used to retrieve the compensation rules available in the system based on the logged-in user organization.
     * @see OrganizationEntity
     * @return the logged-in user organization.
     */
    private OrganizationEntity loggedInUserOrg(){
        return security.getCurrentUserOrganization();
    }

    /**
     * Build a compensation rule from a compensation rule DTO.
     * @see CompensationRule
     * @see CompensationRulesEntity
     * @see CompensationRuleTierEntity
     * @see CompensationActionsEntity
     * @see CompensationActions
     * @see RuleTier
     * @see CompensationAction
     * @see CompensationActions#values()
     * @see CompensationActions#valueOf(String)
     * @see CompensationActions#valueOf(String)
     * @see CompensationActions#valueOf(String)
     * @param ruleDto Compensation rule DTO to be converted to a compensation rule.
     * @throws RuntimeBusinessException if the action does not exist.
     * @return the compensation rule.
     */
    private CompensationRulesEntity buildRule (CompensationRule ruleDto){
        CompensationRulesEntity entity = new CompensationRulesEntity();
        entity.setName(ruleDto.name());
        entity.setActive(ruleDto.isActive());
        entity.setAction(getAction(ruleDto.action()));
        addTiers(entity, ruleDto.tiers());
        entity.setOrganization(loggedInUserOrg());
        return entity;
    }

    /**
     * Add tiers to a compensation rule.
     * @see CompensationRuleTierEntity
     * @see CompensationRulesEntity
     * @see RuleTier
     * @see CompensationAction
     * @see CompensationActions#values()
     * @see CompensationActions#valueOf(String)
     * @see CompensationActions#valueOf(String)
     * @param rule Compensation rule to which the tiers will be added.
     * @param tiers Tiers to be added to the compensation rule.
     */
    private void addTiers (CompensationRulesEntity rule, Set<RuleTier> tiers){
        tiers.forEach(tier -> rule.addTier(buildTier(tier)));
    }

    /**
     * Build a compensation rule tier from a rule tier DTO.
     * @see CompensationRuleTierEntity
     * @see CompensationRulesEntity
     * @see RuleTier
     * @see CompensationAction
     * @see CompensationActions#values()
     * @see CompensationActions#valueOf(String)
     * @see CompensationActions#valueOf(String)
     * @param tierDto Rule tier DTO to be converted to a rule tier.
     * @return the rule tier.
     */
    private CompensationRuleTierEntity buildTier (RuleTier tierDto){
        CompensationRuleTierEntity entity = new CompensationRuleTierEntity();
        entity.setCondition(tierDto.condition());
        entity.setReward(tierDto.reward());
        entity.setActive(tierDto.isActive());
        return entity;
    }
    /**
     * Build a compensation action from a compensation action DTO.
     * @see CompensationActionsEntity
     * @see CompensationAction
     * @see CompensationActions#values()
     * @see CompensationActions#valueOf(String)
     * @see CompensationActions#valueOf(String)
     * @param actionDto Compensation action DTO to be converted to a compensation action.
     * @return the compensation action.
     **/
    private CompensationActionsEntity buildAction (CompensationAction actionDto){
        CompensationActionsEntity entity = new CompensationActionsEntity();
        entity.setName(actionDto.name());
        entity.setDescription(actionDto.description());
       return entity;
    }
}
