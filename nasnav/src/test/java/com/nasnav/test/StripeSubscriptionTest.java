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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Stripe_Subscription_Service_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
//@TestPropertySource(properties = "stripe.apikey = sk_test_51NxqlfGR4qGEOW4E6Qni6REIWcwheVdU8mf2LtTVn1BWn8dtdQSg7stf9b0cqE8CJZVja9aTuOISKg15qC52CjLf00bmLe17sU")
//@TestPropertySource(properties = "stripe.webhook.secret= whsec_c6c1772b65026654a21e1beac00f0a213eacbb56edd1bca45a8fdfd10fdb1c6c")
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
