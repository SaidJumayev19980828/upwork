package com.nasnav.service;

import com.nasnav.dto.CompensationAction;
import com.nasnav.dto.CompensationRule;
import com.nasnav.enumerations.CompensationActions;
import com.nasnav.persistence.CompensationActionsEntity;
import com.nasnav.persistence.CompensationRulesEntity;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface CompensationService {

    List<CompensationActions> getCompensationActions ();

    CompensationActionsEntity createAction (CompensationAction dto);
    CompensationActionsEntity getAction (long id);
    List<CompensationActionsEntity> getAllActions ();


    CompensationRulesEntity createRule (CompensationRule dto);
    CompensationRulesEntity getRule (long id);
    PageImpl<CompensationRulesEntity> getAllRules(int start, int count) ;
}
