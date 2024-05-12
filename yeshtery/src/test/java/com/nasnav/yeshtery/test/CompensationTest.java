package com.nasnav.yeshtery.test;

import com.nasnav.dto.CompensationAction;
import com.nasnav.dto.CompensationRule;
import com.nasnav.dto.RuleTier;
import com.nasnav.dto.response.RestResponsePage;
import com.nasnav.enumerations.CompensationActions;
import com.nasnav.persistence.CompensationActionsEntity;
import com.nasnav.persistence.CompensationRulesEntity;
import com.nasnav.persistence.EligibleNotReceivedEntity;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Compensation_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class CompensationTest extends AbstractTestWithTempBaseDir {
@Autowired
private TestRestTemplate restTemplate;


    @Test
    public void createActionTest(){
        CompensationAction action = new CompensationAction(CompensationActions.LIKE,"Description Test for LIKE");
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<CompensationAction> request = new HttpEntity<>(action,headers);
        ResponseEntity<Void> response = restTemplate.postForEntity("/v1/compensation/action", request, Void.class);
        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    public void createDuplicateActionTest(){
        CompensationAction action = new CompensationAction(CompensationActions.SHARE,"Description Test for Share");
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<CompensationAction> request = new HttpEntity<>(action,headers);
        ResponseEntity<Void> response = restTemplate.postForEntity("/v1/compensation/action", request, Void.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void createActionWithInvalidTokenTest(){
        CompensationAction action = new CompensationAction(CompensationActions.LIKE, "Description Test for LIKE");
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "54333");
        HttpEntity<CompensationAction> request = new HttpEntity<>(action, headers);
        ResponseEntity<Void> response = restTemplate.postForEntity("/v1/compensation/action", request, Void.class);
        assertEquals(401, response.getStatusCode().value());
    }
    @Test
    public void getAllTypesTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ParameterizedTypeReference<List<CompensationActions>> responseType =
                new ParameterizedTypeReference<List<CompensationActions>>() {};
        ResponseEntity<List<CompensationActions>> response =
                restTemplate.exchange("/v1/compensation/action/types", HttpMethod.GET, request, responseType);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, Objects.requireNonNull(response.getBody()).size());
    }

    @Test
    public void getActionsTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ParameterizedTypeReference<List<CompensationActionsEntity>> responseType =
                new ParameterizedTypeReference<List<CompensationActionsEntity>>() {};
        ResponseEntity<List<CompensationActionsEntity>> response =
                restTemplate.exchange("/v1/compensation/action", HttpMethod.GET, request, responseType);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<CompensationActionsEntity> actions =  Objects.requireNonNull(response.getBody());
        assertEquals(2, actions.size());
        assertEquals(CompensationActions.SHARE, actions.get(0).getName());
    }
    @Test
    public void getActionByIdTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ParameterizedTypeReference<CompensationActionsEntity> responseType =
                new ParameterizedTypeReference<CompensationActionsEntity>() {};
        ResponseEntity<CompensationActionsEntity> response =
                restTemplate.exchange("/v1/compensation/action/1", HttpMethod.GET, request, responseType);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        CompensationActionsEntity action =  Objects.requireNonNull(response.getBody());
        assertEquals(CompensationActions.SHARE, action.getName());
    }

    @Test
    public void getActionByInValidIdTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ParameterizedTypeReference<CompensationActionsEntity> responseType =
                new ParameterizedTypeReference<CompensationActionsEntity>() {};
        ResponseEntity<CompensationActionsEntity> response =
                restTemplate.exchange("/v1/compensation/action/100", HttpMethod.GET, request, responseType);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void createRule(){
        RuleTier ruleTier = new RuleTier(10, BigDecimal.valueOf(10),true);
        RuleTier ruleTier2 = new RuleTier(10, BigDecimal.valueOf(10),false);
        RuleTier ruleTier3 = new RuleTier(10, BigDecimal.valueOf(10),true);
        Set<RuleTier> ruleTiers = new HashSet<>(Arrays.asList(ruleTier, ruleTier2, ruleTier3));
        CompensationRule rule = new CompensationRule(2,"rule Test","description",true, ruleTiers );
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<CompensationRule> request = new HttpEntity<>(rule, headers);
        ResponseEntity<Void> response = restTemplate.postForEntity("/v1/compensation/rule", request, Void.class);
        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    public void createDuplicateRule(){
        RuleTier ruleTier = new RuleTier(10, BigDecimal.valueOf(10), true);
        RuleTier ruleTier2 = new RuleTier(10, BigDecimal.valueOf(10), false);
        RuleTier ruleTier3 = new RuleTier(10, BigDecimal.valueOf(10), true);
        Set<RuleTier> ruleTiers = new HashSet<>(Arrays.asList(ruleTier, ruleTier2, ruleTier3));
        CompensationRule rule = new CompensationRule(1, "rule Test", "description",true, ruleTiers);
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<CompensationRule> request = new HttpEntity<>(rule, headers);
        ResponseEntity<Void> response = restTemplate.postForEntity("/v1/compensation/rule", request, Void.class);
        assertEquals(201, response.getStatusCode().value());
    }




    @Test
    public void updateRule(){
        RuleTier ruleTier = new RuleTier(10, BigDecimal.valueOf(10), true);
        RuleTier ruleTier2 = new RuleTier(10, BigDecimal.valueOf(10), false);
        RuleTier ruleTier3 = new RuleTier(10, BigDecimal.valueOf(10), true);
        Set<RuleTier> ruleTiers = new HashSet<>(Arrays.asList(ruleTier, ruleTier2, ruleTier3));
        CompensationRule rule = new CompensationRule(1, "rule Test", "description",true, ruleTiers);
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<CompensationRule> request = new HttpEntity<>(rule, headers);
        ResponseEntity<String> response = restTemplate.exchange("/v1/compensation/rule/1",
                HttpMethod.PUT,request, String.class);
        assertEquals(200, response.getStatusCode().value());
    }


    @Test
    public void updateRuleException(){
        RuleTier ruleTier = new RuleTier(10, BigDecimal.valueOf(10), true);
        RuleTier ruleTier2 = new RuleTier(10, BigDecimal.valueOf(10), false);
        RuleTier ruleTier3 = new RuleTier(10, BigDecimal.valueOf(10), true);
        Set<RuleTier> ruleTiers = new HashSet<>(Arrays.asList(ruleTier, ruleTier2, ruleTier3));
        CompensationRule rule = new CompensationRule(1, "rule Test", "description",true, ruleTiers);
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<CompensationRule> request = new HttpEntity<>(rule, headers);
        ResponseEntity<String> response = restTemplate.exchange("/v1/compensation/rule/12",
                HttpMethod.PUT,request, String.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void deleteRule(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange("/v1/compensation/rule/1",
                HttpMethod.DELETE,request, String.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void deleteRuleException(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange("/v1/compensation/rule/12",
                HttpMethod.DELETE,request, String.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void getRulesTest() {
        int start= 0;
        int count = 10;
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ParameterizedTypeReference<RestResponsePage<CompensationRulesEntity>> responseType =
                new ParameterizedTypeReference<RestResponsePage<CompensationRulesEntity>>() {};
        ResponseEntity<RestResponsePage<CompensationRulesEntity>> response =
                restTemplate.exchange("/v1/compensation/rule/all?start"+ start + "&count"+count, HttpMethod.GET, request, responseType);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        RestResponsePage<CompensationRulesEntity> actions =  Objects.requireNonNull(response.getBody());
        assertEquals(2, actions.getNumberOfElements());
        int tiersCount = actions.getContent().get(0).getTiers().size();
        assertEquals(2, tiersCount);
    }

    @Test
    public void getRuleTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ParameterizedTypeReference<List<CompensationRulesEntity>> responseType =
                new ParameterizedTypeReference<List<CompensationRulesEntity>>() {};
        ResponseEntity<List<CompensationRulesEntity>> response =
                restTemplate.exchange("/v1/compensation/rule/list", HttpMethod.GET, request, responseType);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getEligibleTest() {
        int start= 0;
        int count = 10;
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ParameterizedTypeReference<RestResponsePage<EligibleNotReceivedEntity>> responseType =
                new ParameterizedTypeReference<RestResponsePage<EligibleNotReceivedEntity>>() {};
        ResponseEntity<RestResponsePage<EligibleNotReceivedEntity>> response =
                restTemplate.exchange("/v1/compensation/eligible/all?start"+ start + "&count"+count, HttpMethod.GET, request, responseType);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getRuleByIdTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ParameterizedTypeReference<CompensationRulesEntity> responseType =
                new ParameterizedTypeReference<CompensationRulesEntity>() {};
        ResponseEntity<CompensationRulesEntity> response =
                restTemplate.exchange("/v1/compensation/rule/1", HttpMethod.GET, request, responseType);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        CompensationRulesEntity rule = response.getBody();
        assertNotNull(rule);
    }

    @Test
    public void getRuleOtherLoggedInUserOrgTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "456");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ParameterizedTypeReference<CompensationRulesEntity> responseType =
                new ParameterizedTypeReference<CompensationRulesEntity>() {};
        ResponseEntity<CompensationRulesEntity> response =
                restTemplate.exchange("/v1/compensation/rule/1", HttpMethod.GET, request, responseType);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testToString() {
        assertEquals("Like", CompensationActions.LIKE.toString());
        assertEquals("Share", CompensationActions.SHARE.toString());
        assertEquals("Join Event", CompensationActions.JOIN_EVENT.toString());
    }

}
