package com.nasnav.yeshtery.test.controllers.yeshtery;

import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dto.response.LoyaltyPointTransactionDTO;
import com.nasnav.dto.response.PromotionDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.LoyaltyPointTransactionEntity;
import com.nasnav.persistence.LoyaltySpentTransactionEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.yeshtery.Yeshtery;
import junit.framework.TestCase;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import javax.annotation.concurrent.NotThreadSafe;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(classes = Yeshtery.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Loyalty_point_Test_Data_Insert.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
class LoyaltyPointTest {
    @Autowired
    private TestRestTemplate template;

    @Test
    void getUserSpendablePoints() {
        HttpEntity<?> request = getHttpEntity("123");
        long orgId = 99001;

        ResponseEntity<List<LoyaltyPointTransactionDTO>> response = template.exchange("/v1/loyalty/spendable_points/" + orgId, GET, request, new ParameterizedTypeReference<List<LoyaltyPointTransactionDTO>>() {
        });
        Assert.assertEquals(OK, response.getStatusCode());

        List<LoyaltyPointTransactionDTO> spendablePoints = response.getBody().stream().collect(toList());
        assertEquals(1, spendablePoints.size());
        assertEquals(2, spendablePoints.get(0).getId());

    }
}
