package com.nasnav.test;

import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.dto.response.PackageResponse;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Subscription_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class SubscriptionTest extends AbstractTestWithTempBaseDir {

    @Autowired
    private TestRestTemplate template;

    @Test
    public void completeSubscriptionSuccess() {
        String requestBody = json()
                .put("type", "stripe")
                .put("paid_amount", "2000")
                .put("start_date", LocalDate.now())
                .toString();
        HttpEntity<?> json = getHttpEntity(requestBody,"123456");
        ResponseEntity<Long> response = template.postForEntity("/subscription/completeSubscription", json, Long.class);
        assertEquals(200, response.getStatusCode().value());

    }


}
