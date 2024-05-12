package com.nasnav.service.impl;

import com.google.common.base.Objects;
import com.nasnav.commons.utils.CustomPaginationPageRequest;
import com.nasnav.dao.CompensationActionsEntityRepository;
import com.nasnav.dao.CompensationRulesRepository;
import com.nasnav.dao.EligibleNotReceivedRepository;
import com.nasnav.dao.ReceivedAwardRepository;
import com.nasnav.dto.CompensationAction;
import com.nasnav.dto.CompensationRule;
import com.nasnav.dto.RuleTier;
import com.nasnav.enumerations.CompensationActions;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BankAccountEntity;
import com.nasnav.persistence.CompensationActionsEntity;
import com.nasnav.persistence.CompensationRuleTierEntity;
import com.nasnav.persistence.CompensationRulesEntity;
import com.nasnav.persistence.EligibleNotReceivedEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ReceivedAwardEntity;
import com.nasnav.persistence.SubPostEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.BankAccountActivityService;
import com.nasnav.service.BankInsideTransactionService;
import com.nasnav.service.CompensationService;
import com.nasnav.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.nasnav.exceptions.ErrorCodes.BANK$ACC$0009;
import static com.nasnav.exceptions.ErrorCodes.COMPEN$001;
import static com.nasnav.exceptions.ErrorCodes.COMPEN$002;
import static com.nasnav.exceptions.ErrorCodes.COMPEN$003;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Log4j2
public class CompensationServiceImpl implements CompensationService {
    private final CompensationActionsEntityRepository actionRepository;
    private final CompensationRulesRepository ruleRepository;
    private final SecurityService security;
    private final BankAccountActivityService bankAccountActivityService;
    private final ReceivedAwardRepository receivedAwardRepository;
    private final EligibleNotReceivedRepository eligibleNotReceivedRepository;
    private final BankInsideTransactionService bankInsideTransactionService;
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
       return ruleRepository.save(buildRule(dto , new CompensationRulesEntity()));
    }

    @Override
    public CompensationRulesEntity updateRule(CompensationRule dto, Long ruleId) {
        return ruleRepository.save(buildRule(dto , getRuleForUpdate(ruleId)));
    }

    @Override
    public void deleteRule(long id) {
        CompensationRulesEntity rule = getRuleForUpdate(id);
        rule.setActive(false);
        ruleRepository.save(rule);
    }

    private CompensationRulesEntity getRuleForUpdate(Long ruleId){
        CompensationRulesEntity rule = getRule(ruleId);
        checkIfRuleInUseNow(rule);
        return rule;
    }
    private void checkIfRuleInUseNow(CompensationRulesEntity rule){
        boolean inUse = ruleRepository.checkIfRuleInUseNow(rule);
        if (inUse){
            throw new RuntimeBusinessException(BAD_REQUEST, COMPEN$003, rule.getId());
        }
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


    @Override
    public List<CompensationRulesEntity> getAllActiveRules() {
        return ruleRepository.findAllByOrganizationAndIsActiveTrue(loggedInUserOrg());
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

    @Override
    @Transactional
    public boolean checkAndProcessReward (Set<CompensationRulesEntity> rules , CompensationActions action , long actionCount , SubPostEntity subPost ) {
            Set<CompensationRulesEntity> filtered =  filterRules(rules, action);
            if (!filtered.isEmpty()) {
               return processReward(rules, actionCount,subPost);
            }
            return true;
    }

    @Override
    public PageImpl<EligibleNotReceivedEntity> getAllEligible(int start, int count) {
        CustomPaginationPageRequest pageRequest = new CustomPaginationPageRequest(start,count);
        return eligibleNotReceivedRepository.findAllByOrganization(loggedInUserOrg(),pageRequest);
    }


    private Set<CompensationRulesEntity> filterRules(Set<CompensationRulesEntity> rules, CompensationActions action) {
        rules.removeIf(rule-> !Objects.equal(rule.getAction().getName() , action));
        return rules;
    }

    private boolean processReward(Set<CompensationRulesEntity> rules, long actionCount , SubPostEntity subPost ) {
        AtomicBoolean showButton = new AtomicBoolean(true);
        rules.forEach(rule -> rule.getTiers().forEach(tier -> {
            if (actionCount >= tier.getCondition()) {
                UserEntity user = subPost.getPost().getUser();
                OrganizationEntity organization = rule.getOrganization();
                if (orgSufficientBalance(organization, tier.getReward())) {
                    rewardUser(tier,subPost,user,organization);
                } else {
                    recordUserAsEligible(tier,subPost,user,organization);
                    showButton.set(false);
                }
            }
        }));
        return showButton.get();
    }

    private boolean orgSufficientBalance( OrganizationEntity organization, BigDecimal reward ){
        BigDecimal balance = organizationBalance(organization);
        return balance.compareTo(reward) >= 0;
    }

    private BigDecimal organizationBalance(OrganizationEntity organization){
       return BigDecimal.valueOf(bankAccountActivityService.getTotalBalance(getOrganizationBankAddressId(organization)));
    }

    private Long getOrganizationBankAddressId( OrganizationEntity organization) {
        BankAccountEntity orgBankAccount = organization.getBankAccount();
        if (orgBankAccount == null) {
            throw new RuntimeBusinessException(NOT_FOUND, BANK$ACC$0009);
        }
        return orgBankAccount.getId();
    }
    private boolean isUserAlreadyRewarded(CompensationRuleTierEntity tier , SubPostEntity subPost , UserEntity user){
        Optional<ReceivedAwardEntity> award = receivedAwardRepository.findByUserAndSubPostAndCompensationTier(user ,subPost, tier);
        return award.isEmpty();
    }
    private void rewardUser(CompensationRuleTierEntity tier , SubPostEntity subPost , UserEntity user , OrganizationEntity organization) {
        if (isUserAlreadyRewarded(tier, subPost, user)) {
            receivedAwardRepository.save(buildReceivedAwardEntity(tier, subPost, user,organization));
            transferMoneyFromOrgToUser(organization,subPost,user,tier.getReward());
        }
    }

    private void transferMoneyFromOrgToUser(OrganizationEntity organization,SubPostEntity subPost ,UserEntity user , BigDecimal reward){
        log.info("pay {} to user {} for sub post {}", reward, user.getId(), subPost.getId());
        BankAccountEntity sender = organization.getBankAccount();
        BankAccountEntity receiver = user.getBankAccount();
        bankInsideTransactionService.transferImpl(sender, receiver, Float.parseFloat(String.valueOf(reward)) );
    }
    private ReceivedAwardEntity buildReceivedAwardEntity(CompensationRuleTierEntity tier, SubPostEntity subPost, UserEntity user , OrganizationEntity organization) {
        ReceivedAwardEntity award = new ReceivedAwardEntity();
        award.setCompensationTier(tier);
        award.setSubPost(subPost);
        award.setUser(user);
        award.setAwardDate(LocalDate.now());
        award.setAwardAmount(tier.getReward());
        award.setOrganization(organization);
        String awardDescription = String.format(
                "Award for achieving %s at tier id %s on %s",
                tier.getReward(), tier.getId(), LocalDate.now()
        );
        award.setAwardDescription(awardDescription);
        return award;
    }

    private void recordUserAsEligible(CompensationRuleTierEntity tier , SubPostEntity subPost , UserEntity user,OrganizationEntity organization) {
        if (isUserAlreadyRewarded(tier, subPost, user) &&
                isUserNotAlreadyEligible(tier, subPost, user) ) {
            eligibleNotReceivedRepository.save(buildEligibleEntity(tier, subPost, user,organization));
        }
    }

    private boolean isUserNotAlreadyEligible(CompensationRuleTierEntity tier, SubPostEntity subPost, UserEntity user){
        Optional<EligibleNotReceivedEntity> eligible = eligibleNotReceivedRepository.findByUserAndSubPostAndCompensationTier(user, subPost, tier);
        return eligible.isEmpty();
    }

    private EligibleNotReceivedEntity buildEligibleEntity(CompensationRuleTierEntity tier, SubPostEntity subPost, UserEntity user,OrganizationEntity organization) {
        EligibleNotReceivedEntity eligible = new EligibleNotReceivedEntity();
        eligible.setCompensationTier(tier);
        eligible.setSubPost(subPost);
        eligible.setUser(user);
        eligible.setEligibilityDate(LocalDate.now());
        eligible.setEligibleAmount(tier.getReward());
        eligible.setOrganization(organization);
        eligible.setReasonForEligibility("Eligible Award but not received organization unSufficient Balance");
        return eligible;
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
    private CompensationRulesEntity buildRule (CompensationRule ruleDto ,  CompensationRulesEntity entity ){
        entity.setName(ruleDto.name());
        entity.setDescription(ruleDto.description());
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
