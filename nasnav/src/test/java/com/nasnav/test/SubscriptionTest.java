package com.nasnav.test;

import com.nasnav.dao.SubscriptionRepository;
import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.enumerations.SubscriptionMethod;
import com.nasnav.persistence.SubscriptionEntity;
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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;

@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Subscription_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
@TestPropertySource(properties = "nasnav.orgid=99003")
public class SubscriptionTest extends AbstractTestWithTempBaseDir {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Test
    public void wertSubscriptionSuccess() {
        String requestBody = json()
                .put("package_id", "99002")
                .toString();
        HttpEntity<?> json = getHttpEntity(requestBody,"123456");
        ResponseEntity<SubscriptionDTO> response = template.postForEntity("/subscription/wertSubscription", json, SubscriptionDTO.class);
        assertEquals(200, response.getStatusCode().value());
        SubscriptionEntity subscription = subscriptionRepository.findById(response.getBody().getId()).get();
        assertEquals(subscription.getType(), SubscriptionMethod.WERT.getValue());
        assertEquals(subscription.getOrganization().getId().longValue(), 99002);
        assertEquals(subscription.getPackageEntity().getId().longValue(), 99002);
        assertEquals(subscription.getPaymentDate().toLocalDate(), LocalDateTime.now().toLocalDate());
        assertEquals(((Timestamp) subscription.getStartDate()).toLocalDateTime().toLocalDate(), LocalDate.now());
        assertEquals(((Timestamp) subscription.getExpirationDate()).toLocalDateTime().toLocalDate(), LocalDate.now().plusDays(subscription.getPackageEntity().getPeriodInDays()));
    }
    @Test
    public void wertSubscriptionInvalidPackage() {
        String requestBody = json()
                .put("package_id", "99002154")
                .toString();
        HttpEntity<?> json = getHttpEntity(requestBody,"123456");
        ResponseEntity response = template.postForEntity("/subscription/wertSubscription", json, Void.class);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void wertSubscriptionWithPackageMissingCurrencyTest() {
        String requestBody = json()
                .put("package_id", "99004")
                .toString();
        HttpEntity<?> json = getHttpEntity(requestBody,"123456");
        ResponseEntity response = template.postForEntity("/subscription/wertSubscription", json, Void.class);
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    public void wertSubscriptionWithInvalidPackageCurrency() {
        String requestBody = json()
                .put("package_id", "99003")
                .toString();
        HttpEntity<?> json = getHttpEntity(requestBody,"abcdefg");
        ResponseEntity<String> response = template.postForEntity("/subscription/wertSubscription", json, String.class);
        assertEquals(500, response.getStatusCode().value());
        assertEquals(response.getBody(),"{\"message\":\"Failed To Fetch Currency Price\",\"error\":\"BC$PRI$0001\"}");

    }


    @Test
    public void getSubscriptionInfoNotSubscribed() {
        ResponseEntity<SubscriptionInfoDTO> response = template.exchange("/subscription/info",GET, getHttpEntity("123456"), SubscriptionInfoDTO.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(response.getBody().isSubscribed(), false);
    }

    @Test
    public void getSubscriptionInfoSubscribed() {
        ResponseEntity<SubscriptionInfoDTO> response = template.exchange("/subscription/info",GET, getHttpEntity("124567"), SubscriptionInfoDTO.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(response.getBody().isSubscribed(), true);
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findById(10000011l).get();

        assertEquals(response.getBody().getExpirationDate(), subscriptionEntity.getExpirationDate());
    }

}
