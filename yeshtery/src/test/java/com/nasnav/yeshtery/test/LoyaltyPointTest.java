package com.nasnav.yeshtery.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.nasnav.dao.*;
import com.nasnav.dto.TierUsersCheck;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.*;
import com.nasnav.enumerations.LoyaltyPointType;
import com.nasnav.enumerations.LoyaltyTransactions;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.LoyaltyPointConfigEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.*;
import com.nasnav.service.LoyaltyPointsService;
import com.nasnav.service.LoyaltyTierService;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.nasnav.exceptions.ErrorCodes.ORG$LOY$0014;
import static com.nasnav.exceptions.ErrorCodes.TIERS$PARAM$0005;
import static com.nasnav.yeshtery.test.commons.TestCommons.*;
import static org.json.JSONObject.NULL;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Loyalty_Point_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class LoyaltyPointTest extends AbstractTestWithTempBaseDir {

    @Autowired
    private TestRestTemplate template;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private LoyaltyTierRepository tierRepository;
    @Autowired
    private LoyaltyPointConfigRepository configRepository;
    @Autowired
    private UserRepository userRepository;


    @Autowired
    private LoyaltyTierService tierService;

    @Test
    public void createLoyaltyPointConfigInvalidConstraints() {

        var body = createConfigJson()
                .put("constraints", NULL)
                .toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/v1/loyalty/config/update", request, LoyaltyPointsUpdateResponse.class);
        assertEquals(406, response.getStatusCodeValue());

        request = getHttpEntity(body, "abcdefg");

        ResponseEntity<List> responseList = template.exchange("/v1/loyalty/config/list?org_id=99001", GET, request, List.class);

        assertEquals(200, responseList.getStatusCodeValue());
        assertEquals(1, responseList.getBody().size());

    }

    @Test
    public void getAllConfig() {
        ResponseEntity<List> responseList = template.getForEntity("/v1/loyalty/config/all", List.class);

        assertEquals(OK, responseList.getStatusCode());
        assertEquals(2, responseList.getBody().size());
    }

    @Test
    public void deleteLoyaltyConfigTest() {
        var body = createConfigJson()
                .put("default_tier", json().put("id", 3))
                .toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/v1/loyalty/config/update", request, LoyaltyPointsUpdateResponse.class);
        assertEquals(200, response.getStatusCodeValue());

        Long id1 = response.getBody().getLoyaltyPointId();
        Optional<LoyaltyPointConfigEntity> config1 = configRepository.findById(id1);
        assertTrue(config1.isPresent());

        request = getHttpEntity("abcdefg");

        ResponseEntity<LoyaltyPointDeleteResponse> deleteResponse = template.exchange("/v1/loyalty/config/delete?id=" + id1, DELETE, request, LoyaltyPointDeleteResponse.class);

        assertEquals(200, deleteResponse.getStatusCodeValue());
        assertEquals(id1, deleteResponse.getBody().getLoyaltyId());
        assertTrue(deleteResponse.getBody().isSuccess());

        request = getHttpEntity(body, "abcdefg");

        ResponseEntity<LoyaltyPointConfigDTO> emptyResponse = template.exchange("/v1/loyalty/config", GET, request, LoyaltyPointConfigDTO.class);

    }

    @Test
    public void createLoyaltyPointConfigInvalidAuthN() {
        var request = getHttpEntity("invalid");
        var response = template.postForEntity("/v1/loyalty/config/update", request, String.class);
        assertEquals(401, response.getStatusCodeValue());
    }

    private JSONObject createConfigJson() {
        JSONObject constraints = json()
                .put("ORDER_ONLINE",
                        json()
                                .put("ratio_from", new BigDecimal("7.00"))
                                .put("ratio_to", new BigDecimal("1.00")));
        return json()
                .put("description", "this is a configuration")
                .put("constraints", constraints)
                .put("default_tier", json().put("id", 1));
    }


    /**
     * Tier
     */

    // tire
    @Test
    public void createTier() {
        String body = getTierJsonString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/v1/loyalty/tier/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(tierRepository.findByTierName("tier test").isPresent());
        tierRepository.deleteByTierName("tier test");
    }

    @Test
    public void creatTierInvalidAuthZ() {
        String body = getTierJsonString();

        var request = getHttpEntity(body, "invalid");
        var response = template.postForEntity("/v1/loyalty/tier/update", request, String.class);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    public void creatAndGetTier() throws JsonProcessingException {
        String body = getTierJsonString();

        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/v1/loyalty/tier/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(tierRepository.findByTierName("tier test").isPresent());

        request = getHttpEntity("abcdefg");
        response = template.exchange("/v1/loyalty/tier/list?org_id=99001", GET, request, String.class);
        List<LoyaltyTierDTO> resBody = mapper.readValue(response.getBody(), new TypeReference<>() {
        });
        assertEquals(4, resBody.size());

        LoyaltyTierDTO loyaltyTierDTO = resBody.get(3);

        assertEquals("tier test", loyaltyTierDTO.getTierName());
        assertEquals(loyaltyTierDTO.getConstraints().get(LoyaltyTransactions.ORDER_ONLINE).doubleValue(), 0.8);

        tierRepository.deleteById(loyaltyTierDTO.getId());
    }


    private String getTierJsonString() {
        JSONObject constraints = json()
                .put("ORDER_ONLINE", "0.8");
        return json()
                .put("operation", "create")
                .put("tier_name", "tier test")
                .put("selling_price", 10)
                .put("org_id", 99001)
                .put("constraints", constraints)
                .put("is_active", true)
                .put("is_special", false)
                .toString();
    }


    @Test
    public void changeUserTierTest() {
        String body = getTierJsonString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/v1/loyalty/tier/update", request, LoyaltyTierUpdateResponse.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(tierRepository.findByTierName("tier test").isPresent());

        Long id = response.getBody().getTierId();
        var changeRequest = getHttpEntity("abcdefg");
        var changeResponse = template.postForEntity("/v1/loyalty/tier/change_user_tier?org_id=99001&user_id=88&tier_id=" + id, changeRequest, UserRepresentationObject.class);

        assertEquals(200, changeResponse.getStatusCodeValue());
        UserEntity user = userRepository.getOne(88L);

        assertEquals(id, changeResponse.getBody().getTierId());

    }

    @Test
    public void updateLoyaltyPointConfigInvalidId() {
        String body = createConfigJson()
                .put("id", 31002)
                .toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/v1/loyalty/config/update", request, String.class);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void getUserPointsNoUserInOrg() {
        var request = getHttpEntity("123");
        var response = template.exchange("/v1/loyalty/points?org_id=99001", GET, request, String.class);
        assertEquals(404, response.getStatusCodeValue());
        assertTrue(response.getBody().contains(ORG$LOY$0014.name()));
    }


    @Test
    public void test_throw_exception_invalid_key_type() {
        String jsonStr = "{\"TRANSACTION_A\": \"invalid_value\"}";
        assertThrows(RuntimeBusinessException.class, () -> tierService.readTierJson(jsonStr));
    }

    @Test
    public void test_throw_exception_invalid_value_type() {
        String jsonStr = "{\"TRANSACTION_A\": \"invalid_value\"}";
        assertThrows(RuntimeBusinessException.class, () -> tierService.readTierJson(jsonStr));
    }

    @Test
    public void test_parse_valid_json_single_key_value_pair() {
        String jsonStr = "{\"PICKUP_FROM_SHOP\": 0.5}";
        HashMap<LoyaltyTransactions, BigDecimal> result = tierService.readTierJson(jsonStr);
        HashMap<LoyaltyTransactions, BigDecimal> expected = new HashMap<>();
        expected.put(LoyaltyTransactions.PICKUP_FROM_SHOP, new BigDecimal("0.5"));
        assertEquals(expected, result);
    }

    @Test
    public void TierUserCheckSuccess() {
        var request = getHttpEntity("161718");

        ResponseEntity<TierUsersCheck> response = template.exchange("/v1/loyalty/tier/usersCheck?tier_id=4", GET, request, TierUsersCheck.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).getSoftDelete());

        ResponseEntity<TierUsersCheck> response2 = template.exchange("/v1/loyalty/tier/usersCheck?tier_id=3", GET, request, TierUsersCheck.class);
        assertEquals(200, response2.getStatusCodeValue());
        assertFalse(Objects.requireNonNull(response2.getBody()).getSoftDelete());

    }

    @Test
    public void TierUserCheckUnAuthorize() {
        var request = getHttpEntity("123");
        ResponseEntity<TierUsersCheck> response = template.exchange("/v1/loyalty/tier/usersCheck?tier_id=1", GET, request, TierUsersCheck.class);
        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    public void TierUserCheckInvalidTierId() {
        var request = getHttpEntity("161718");
        ResponseEntity<TierUsersCheck> response = template.exchange("/v1/loyalty/tier/usersCheck?tier_id=100", GET, request, TierUsersCheck.class);
        assertEquals(406, response.getStatusCodeValue());
    }

    @Test
    public void removeTierSuccess() {
        var request = getHttpEntity("161718");
        var deleteResponse = template.exchange("/v1/loyalty/tier/remove?tier_id=3", DELETE, request, Void.class);
        assertEquals(200, deleteResponse.getStatusCodeValue());


        var deleteResponse2 = template.exchange("/v1/loyalty/tier/remove?tier_id=4", DELETE, request, Void.class);
        assertEquals(200, deleteResponse2.getStatusCodeValue());
       }

    @Test
    public void removeTierFailedBadRequest() {
        var request = getHttpEntity("161718");
        var deleteResponse = template.exchange("/v1/loyalty/tier/remove?id=3", DELETE, request, Void.class);
        assertEquals(400, deleteResponse.getStatusCodeValue());
    }

    @Test
    public void removeTierFailed() {
        var request = getHttpEntity("161718");
        var deleteResponse = template.exchange("/v1/loyalty/tier/remove?tier_id=1", DELETE, request, String.class);
        assertEquals(406, deleteResponse.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(deleteResponse.getBody()).contains(TIERS$PARAM$0005.name()));
    }
}
