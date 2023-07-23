package com.nasnav.yeshtery.test.controllers.yeshtery;

import com.nasnav.dto.response.LoyaltyPointTransactionDTO;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import javax.annotation.concurrent.NotThreadSafe;
import java.math.BigDecimal;
import java.util.List;

import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

@NotThreadSafe
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Loyalty_point_Test_Data_Insert.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
class LoyaltyPointTest extends AbstractTestWithTempBaseDir {
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
        assertEquals(3, spendablePoints.get(0).getId());
        Assert.assertEquals(BigDecimal.valueOf(20) ,spendablePoints.get(0).getPoints());


    }
    @Test
    void SharePoint() {
        HttpEntity<?> request = getHttpEntity("123");

        ResponseEntity<Void> response = template.exchange("/v1/loyalty/share_points?point_id=2&email=test2@nasnav.com&points=9",
                POST,
                request,Void.class);
        Assert.assertEquals(OK, response.getStatusCode());

    }

    @Test
    void getSpendablePointsAndSharePoint() {
        HttpEntity<?> request = getHttpEntity("456");
        long orgId = 99002;

        ResponseEntity<List<LoyaltyPointTransactionDTO>> response = template.exchange("/v1/loyalty/spendable_points/" + orgId, GET, request, new ParameterizedTypeReference<List<LoyaltyPointTransactionDTO>>() {
        });
        Assert.assertEquals(OK, response.getStatusCode());
        Assert.assertEquals(BigDecimal.valueOf(30), response.getBody().get(0).getPoints());

        List<LoyaltyPointTransactionDTO> spendablePoints = response.getBody().stream().collect(toList());
        Long point_id = spendablePoints.get(0).getId();
        String email = "test4@nasnav.com";
        BigDecimal points = BigDecimal.valueOf(5);
        ResponseEntity<Void> res = template.exchange(
                "/v1/loyalty/share_points?point_id="+point_id+"&email="+email+"&points="+points,
                POST,
                request,Void.class);
        Assert.assertEquals(OK, res.getStatusCode());
        ResponseEntity<List<LoyaltyPointTransactionDTO>> resAfterShare = template.exchange("/v1/loyalty/spendable_points/" + orgId, GET, request, new ParameterizedTypeReference<List<LoyaltyPointTransactionDTO>>() {
        });
        Assert.assertEquals(BigDecimal.valueOf(25), resAfterShare.getBody().get(0).getPoints());


        HttpEntity<?> request2 = getHttpEntity("258");
        ResponseEntity<List<LoyaltyPointTransactionDTO>> resAfterShareToSharedUser = template.exchange("/v1/loyalty/spendable_points/" + orgId, GET, request2, new ParameterizedTypeReference<List<LoyaltyPointTransactionDTO>>() {
        });
        Assert.assertEquals(BigDecimal.valueOf(5), resAfterShareToSharedUser.getBody().get(0).getPoints());

        assertEquals(4, spendablePoints.get(0).getId());
        assertEquals(1, spendablePoints.size());
    }

}
