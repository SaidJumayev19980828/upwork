package com.nasnav.test;

import com.nasnav.dao.SubscriptionRepository;
import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.enumerations.SubscriptionMethod;
import com.nasnav.enumerations.SubscriptionStatus;
import com.nasnav.persistence.SubscriptionEntity;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;

@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Wert_Subscription_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
@TestPropertySource(properties = "nasnav.orgid=99003")
public class WertSubscriptionTest extends AbstractTestWithTempBaseDir {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Test
    @Ignore
    public void wertSubscriptionSuccessTest() {
        registerPackage("123456","99002");
        HttpEntity<?> json = getHttpEntity("","123456");
        ResponseEntity<SubscriptionDTO> response = template.postForEntity("/subscription/wert/create", json, SubscriptionDTO.class);
        assertEquals(200, response.getStatusCode().value());
        SubscriptionEntity subscription = subscriptionRepository.findById(response.getBody().getId()).get();
        assertEquals(subscription.getType(), SubscriptionMethod.WERT.getValue());
        assertEquals(subscription.getOrganization().getId().longValue(), 99002);
        assertEquals(subscription.getPackageEntity().getId().longValue(), 99002);
        assertEquals(subscription.getStatus(), SubscriptionStatus.ACTIVE.getValue());
        assertEquals(((Timestamp) subscription.getStartDate()).toLocalDateTime().toLocalDate(), LocalDate.now());
        assertEquals(((Timestamp) subscription.getExpirationDate()).toLocalDateTime().toLocalDate(), LocalDate.now().plusDays(subscription.getPackageEntity().getPeriodInDays()));
    }

    @Test
    public void wertSubscriptionWithPackageMissingCurrencyTest() {
        registerPackage("123456","99004");
        HttpEntity<?> json = getHttpEntity("","123456");
        ResponseEntity response = template.postForEntity("/subscription/wert/create", json, Void.class);
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    @Ignore
    public void wertSubscriptionWithInvalidPackageCurrencyTest() {
        registerPackage("123456","99003");
        HttpEntity<?> json = getHttpEntity("","abcdefg");
        ResponseEntity<String> response = template.postForEntity("/subscription/wert/create", json, String.class);
        assertEquals(500, response.getStatusCode().value());
        assertEquals(response.getBody(),"{\"message\":\"Failed To Fetch Currency Price\",\"error\":\"BC$PRI$0001\"}");

    }


    @Test
    public void getSubscriptionInfoNotSubscribedTest() {
        ResponseEntity<SubscriptionInfoDTO> response = template.exchange("/subscription/info",GET, getHttpEntity("123456"), SubscriptionInfoDTO.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(response.getBody().isSubscribed(), false);
    }


    @Test
    public void subscribeAlreadySubscribedOrganizationTest() {
        HttpEntity<?> json = getHttpEntity("","124567");
        ResponseEntity<SubscriptionDTO> response = template.postForEntity("/subscription/wert/create", json, SubscriptionDTO.class);
        assertEquals(406, response.getStatusCode().value());
    }





    @Test
    @Ignore
    public void getSubscriptionInfoSubscribedTest() {

        registerPackage("123456","99002");
        HttpEntity<?> json = getHttpEntity("","123456");
        ResponseEntity<SubscriptionDTO> response = template.postForEntity("/subscription/wert/create", json, SubscriptionDTO.class);
        assertEquals(200, response.getStatusCode().value());
        SubscriptionEntity subscription = subscriptionRepository.findById(response.getBody().getId()).get();
        assertEquals(subscription.getType(), SubscriptionMethod.WERT.getValue());
        assertEquals(subscription.getOrganization().getId().longValue(), 99002);
        assertEquals(subscription.getPackageEntity().getId().longValue(), 99002);
        assertEquals(subscription.getPaymentDate().toLocalDate(), LocalDateTime.now().toLocalDate());
        assertEquals(subscription.getStatus(), SubscriptionStatus.ACTIVE.getValue());
        assertEquals(((Timestamp) subscription.getStartDate()).toLocalDateTime().toLocalDate(), LocalDate.now());
        assertEquals(((Timestamp) subscription.getExpirationDate()).toLocalDateTime().toLocalDate(), LocalDate.now().plusDays(subscription.getPackageEntity().getPeriodInDays()));



        ResponseEntity<SubscriptionInfoDTO> responseSubscriptionInfo = template.exchange("/subscription/info",GET, getHttpEntity("124567"), SubscriptionInfoDTO.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(responseSubscriptionInfo.getBody().isSubscribed(), true);
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findById(10000011l).get();
        assertEquals(responseSubscriptionInfo.getBody().getType(), SubscriptionMethod.WERT.getValue());
        assertEquals(responseSubscriptionInfo.getBody().getStatus(), SubscriptionStatus.ACTIVE.getValue());

        assertEquals(responseSubscriptionInfo.getBody().getExpirationDate(), subscriptionEntity.getExpirationDate());
        //Test other subscription that already expired is cancelled
        SubscriptionEntity cancelledSubscription = subscriptionRepository.findById(10000012l).get();
        assertEquals(cancelledSubscription.getStatus(), SubscriptionStatus.CANCELED.getValue());
    }


    private void registerPackage(String authToken, String packageId){
        String requestRegisterPackage = json().put("package_id", packageId).put("organization_id", 99002).toString();
        HttpEntity<?> jsonRegisterPackage = getHttpEntity(requestRegisterPackage, authToken);
        ResponseEntity<String> responseRegisterPackage = template.postForEntity("/package/register", jsonRegisterPackage, String.class);
        assertEquals(200, responseRegisterPackage.getStatusCode().value());
    }

    @Test
    public void getSubscriptionsByPackageTest() {
        ResponseEntity<Object> response = template.exchange("/subscription/package/99002",GET,
                getHttpEntity("abcdefg"), Object.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void getSubscriptionsByPackageForbidden() {
        ResponseEntity<Object> response = template.exchange("/subscription/package/99002",GET,
                getHttpEntity("123456"), Object.class);
        assertEquals(403, response.getStatusCode().value());
    }
}
