package com.nasnav.test;

import com.nasnav.dao.SubscriptionRepository;
import com.nasnav.dto.StripeSubscriptionDTO;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Stripe_Subscription_Service_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class StripeSubscriptionTest extends AbstractTestWithTempBaseDir {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    SubscriptionRepository subscriptionRepository;


    @Test
    public void stripeCreateSubscriptionNotRegisteredPackage() {
        HttpEntity<?> json = getHttpEntity("", "123456");
        ResponseEntity<StripeSubscriptionDTO> response = template.postForEntity("/subscription/stripe/create", json, StripeSubscriptionDTO.class);
        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void stripeChangePlanInNotSubscribedOrgTest() {
        HttpEntity<?> json = getHttpEntity("", "abcdefg");
        ResponseEntity<StripeSubscriptionDTO> response = template.postForEntity("/subscription/stripe/changePlan", json, StripeSubscriptionDTO.class);
        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void stripeChangePlanToSameRegisteredPackageTest() {
        HttpEntity<?> json = getHttpEntity("", "abcdefg");
        ResponseEntity<StripeSubscriptionDTO> response = template.postForEntity("/subscription/stripe/changePlan", json, StripeSubscriptionDTO.class);
        assertEquals(406, response.getStatusCode().value());
    }
//    @Test
//    public void stripeCreateSubscriptionWithWrongPriceId() {
//        HttpEntity<?> json = getHttpEntity("", "abcdefg");
//        ResponseEntity<StripeSubscriptionDTO> response = template.postForEntity("/subscription/stripe/create", json, StripeSubscriptionDTO.class);
//        assertEquals(500, response.getStatusCode().value());
//    }

//    @Test
//    public void stripeCreateSubscriptionSuccess() {
//        //register
//        String requestBody = json().put("package_id", 99003L).toString();
//        ResponseEntity<String> registerResponse = template.postForEntity("/package/register-package-profile", getHttpEntity(requestBody, "abcdefg"), String.class);
//        assertEquals(200, registerResponse.getStatusCode().value());
//
//        HttpEntity<?> json = getHttpEntity("", "abcdefg");
//        ResponseEntity<StripeConfirmDTO> response = template.postForEntity("/subscription/stripe/create", json, StripeConfirmDTO.class);
//        assertEquals(200, response.getStatusCode().value());
//        assertEquals("payment", response.getBody().getType());
//
//    }





}
