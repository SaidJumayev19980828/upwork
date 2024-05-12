package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.CompensationAction;
import com.nasnav.dto.CompensationRule;
import com.nasnav.enumerations.CompensationActions;
import com.nasnav.persistence.CompensationActionsEntity;
import com.nasnav.persistence.CompensationRulesEntity;
import com.nasnav.persistence.EligibleNotReceivedEntity;
import com.nasnav.service.CompensationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static com.nasnav.constatnts.DefaultValueStrings.DEFAULT_PAGING_COUNT;
import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = CompensationController.API_PATH, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
public class CompensationController {
    static final String API_PATH = YeshteryConstants.API_PATH + "/compensation";
    private final CompensationService compensationService;


    /**
     * Get all action types
     * @param userToken - user token from header to let the token input appear at Swagger
     * @return list of action types
     * @see CompensationActions
     */
    @GetMapping(value = "action/types")
    public List<CompensationActions> getAllActionTypes(
            @RequestHeader(value = TOKEN_HEADER, required = false) String userToken
    ){
        return compensationService.getCompensationActions();
    }

    /**
     * Create a new action type.
     * @param userToken - user token from header to let the token input appear at Swagger
     * @param actionDto - action type to be created in the system
     */

    @PostMapping(value ="action")
    @ResponseStatus(value = CREATED, reason = "Action created")
    public CompensationActionsEntity createAction(
            @RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @Valid @RequestBody CompensationAction actionDto){
        return compensationService.createAction(actionDto);
    }

    /**
     * Get all actions in the system.
     * @param userToken - user token from header to let the token input appear at Swagger
     * @return list of actions in the system.
     * @see CompensationActionsEntity
     */

    @GetMapping(value = "action")
    public List<CompensationActionsEntity> getAllActions(
            @RequestHeader(value = TOKEN_HEADER, required = false) String userToken
    ){
        return compensationService.getAllActions();
    }

    /**
     * Get action by id.
     * @param userToken - user token from header to let the token input appear at Swagger
     * @param id - action id.
     * @see CompensationActionsEntity
     * @return action Entity for the provided id.
     * @throws com.nasnav.exceptions.RuntimeBusinessException - if no action found for the provided id.
     */

    @GetMapping(value = "action/{id}")
    public CompensationActionsEntity getActionById(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,@PathVariable Long id){
        return compensationService.getAction(id);
    }

    /**
     * Create a new rule.
     * @param userToken - user token from header to let the token input appear at Swagger
     * @see CompensationRulesEntity
     * @see CompensationRule
     * @see CompensationActionsEntity
     * @param rule - rule to be created in the system.
     * @throws com.nasnav.exceptions.RuntimeBusinessException - if no action found for the provided id.
     */
    @PostMapping(value="rule")
    public ResponseEntity<CompensationRulesEntity> createRule(
            @RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
            @Valid @RequestBody CompensationRule rule){
        return ResponseEntity.status(CREATED).body(compensationService.createRule(rule));
    }

    @PutMapping(value = "rule/{ruleId}")
    public CompensationRulesEntity updateRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody CompensationRule rule){
        return compensationService.updateRule(rule , ruleId);
    }

    @DeleteMapping(value = "rule/{ruleId}")
    public void deleteRule(@PathVariable Long ruleId){
        compensationService.deleteRule(ruleId);
    }


    /**
     * Get rule by id.
     * @param userToken - user token from header to let the token input appear at Swagger.
     * @see CompensationRulesEntity
     * @see CompensationRule
     * @see CompensationActionsEntity
     * @throws com.nasnav.exceptions.RuntimeBusinessException - if no action found for the provided id.
     * @return rule Entity for the provided id.
     */
    @GetMapping(value = "rule/{id}")
    public CompensationRulesEntity getRuleById(
            @RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @PathVariable Long id){
        return compensationService.getRule(id);
    }

    /**
     * Get all rules in the system.
     * @param userToken - user token from header to let the token input appear at Swagger.
     * @param start - start index of the page.
     * @param count - number of records to be returned in the page.
     * @see CompensationRulesEntity
     * @see CompensationRule
     * @see CompensationActionsEntity
     * @description - Get all rules in the system For tha logged-in user Organization.
     * @return Pagination result of rules in the system.
     */

    @GetMapping(value = "rule/all")
    public PageImpl<CompensationRulesEntity> getAllRules(
            @RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
            @RequestParam(required = false, defaultValue = "0") Integer start,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count
            ){
        return compensationService.getAllRules(start,count);
    }

    @GetMapping(value = "eligible/all")
    public PageImpl<EligibleNotReceivedEntity> getAllEligible(
            @RequestParam(required = false, defaultValue = "0") Integer start,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count
    ){
        return compensationService.getAllEligible(start,count);
    }

    @GetMapping(value = "rule/list")
    public List<CompensationRulesEntity> getAllRulesByOrg(){
        return compensationService.getAllActiveRules();
    }

}
