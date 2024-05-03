package com.nasnav.service;

import com.nasnav.dto.CompensationAction;
import com.nasnav.dto.CompensationRule;
import com.nasnav.enumerations.CompensationActions;
import com.nasnav.persistence.CompensationActionsEntity;
import com.nasnav.persistence.CompensationRulesEntity;
import com.nasnav.persistence.EligibleNotReceivedEntity;
import com.nasnav.persistence.SubPostEntity;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Set;

public interface CompensationService {

    List<CompensationActions> getCompensationActions ();

    CompensationActionsEntity createAction (CompensationAction dto);
    CompensationActionsEntity getAction (long id);
    List<CompensationActionsEntity> getAllActions ();


    CompensationRulesEntity createRule (CompensationRule dto);
    CompensationRulesEntity getRule (long id);
    PageImpl<CompensationRulesEntity> getAllRules(int start, int count) ;

    List<CompensationRulesEntity> getAllActiveRules();

    boolean checkAndProcessReward (Set<CompensationRulesEntity> rules , CompensationActions action , long actionCount , SubPostEntity subPost );

    PageImpl<EligibleNotReceivedEntity> getAllEligible(int start, int count) ;
}
