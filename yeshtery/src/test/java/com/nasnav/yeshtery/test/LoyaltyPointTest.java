package com.nasnav.yeshtery.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.nasnav.dao.*;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.*;
import com.nasnav.enumerations.LoyaltyPointType;
import com.nasnav.persistence.LoyaltyPointConfigEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.*;
import com.nasnav.service.LoyaltyPointsService;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.nasnav.exceptions.ErrorCodes.ORG$LOY$0014;
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
                .toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/v1/loyalty/config/update", request, LoyaltyPointsUpdateResponse.class);
        assertEquals(200, response.getStatusCodeValue());

        Long id1 = response.getBody().getLoyaltyPointId();
        Optional<LoyaltyPointConfigEntity> config1 = configRepository.findById(id1);
        assertTrue(config1.isPresent());

        request = getHttpEntity("abcdefg");

        ResponseEntity<LoyaltyPointDeleteResponse> deleteResponse = template.exchange("/v1/loyalty/config/delete?id="+id1, DELETE, request, LoyaltyPointDeleteResponse.class);

        assertEquals(200,deleteResponse.getStatusCodeValue());
        assertEquals(id1 , deleteResponse.getBody().getLoyaltyId());
        assertTrue(deleteResponse.getBody().isSuccess());

        request = getHttpEntity(body, "abcdefg");

        ResponseEntity<LoyaltyPointConfigDTO> emptyResponse = template.exchange("/v1/loyalty/config", GET, request, LoyaltyPointConfigDTO.class);

        assertEquals(404, emptyResponse.getStatusCodeValue()); // no active config found
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
    public void createTier(){
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
        List<LoyaltyTierDTO> resBody = mapper.readValue(response.getBody(), new TypeReference<>(){});
        assertEquals(2, resBody.size());

        LoyaltyTierDTO loyaltyTierDTO = resBody.get(1);

        assertEquals("tier test", loyaltyTierDTO.getTierName());
        assertEquals(loyaltyTierDTO.getConstraints().get(LoyaltyPointType.ORDER_ONLINE).doubleValue(), 0.8);

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
                .put("constraints", constraints )
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
        var changeResponse = template.postForEntity("/v1/loyalty/tier/change_user_tier?org_id=99001&user_id=88&tier_id="+id, changeRequest, UserRepresentationObject.class);

        assertEquals(200, changeResponse.getStatusCodeValue());
        UserEntity user = userRepository.getOne(88L);

        assertEquals(id, changeResponse.getBody().getTierId() );

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
}
