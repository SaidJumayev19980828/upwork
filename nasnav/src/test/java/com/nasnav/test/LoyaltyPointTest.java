package com.nasnav.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.nasnav.dao.*;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.LoyaltyPointConfigDTO;
import com.nasnav.dto.request.LoyaltyTierDTO;
import com.nasnav.dto.response.LoyaltyPointTransactionDTO;
import com.nasnav.dto.response.RedeemPointsOfferDTO;
import com.nasnav.enumerations.LoyaltyPointType;
import com.nasnav.persistence.LoyaltyPointConfigEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.*;
import com.nasnav.service.LoyaltyPointsService;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.test.commons.TestCommons.*;
import static java.util.stream.Collectors.toList;
import static org.json.JSONObject.NULL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.*;
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
    private LoyaltyPointTransactionRepository transactionRepo;

    @Autowired
    private LoyaltyPointsService loyaltyPointsService;


    @Test
    public void createLoyaltyPointConfigInvalidConstraints() {

        var body = createConfigJson()
                .put("constraints", NULL)
                .toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/loyalty/config/update", request, LoyaltyPointsUpdateResponse.class);
        assertEquals(406, response.getStatusCodeValue());

        request = getHttpEntity(body, "abcdefg");

        ResponseEntity<List> responseList = template.exchange("/loyalty/config/list?org_id=99001", GET, request, List.class);

        assertEquals(200, responseList.getStatusCodeValue());
        assertEquals(1, responseList.getBody().size());

    }
    
    @Test
    public void deleteLoyaltyConfigTest() {
        var body = createConfigJson()
                .put("default_tier", json().put("id", 2))
                .toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/loyalty/config/update", request, LoyaltyPointsUpdateResponse.class);
        assertEquals(200, response.getStatusCodeValue());

        Long id1 = response.getBody().getLoyaltyPointId();
        Optional<LoyaltyPointConfigEntity> config1 = configRepository.findById(id1);
        assertTrue(config1.isPresent());

        request = getHttpEntity("abcdefg");

        ResponseEntity<LoyaltyPointDeleteResponse> deleteResponse = template.exchange("/loyalty/config/delete?id="+id1, DELETE, request, LoyaltyPointDeleteResponse.class);

        assertEquals(200,deleteResponse.getStatusCodeValue());
        assertEquals(id1 , deleteResponse.getBody().getLoyaltyId());
        assertTrue(deleteResponse.getBody().isSuccess());

        request = getHttpEntity(body, "abcdefg");

        ResponseEntity<LoyaltyPointConfigDTO> emptyResponse = template.exchange("/loyalty/config", GET, request, LoyaltyPointConfigDTO.class);

    }
    

    private LoyaltyPointConfigDTO getLoyaltyPointConfigDto() {
        JSONObject configJson = createConfigJson();
        return  new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create().fromJson(configJson.toString(), LoyaltyPointConfigDTO.class);
    }

    @Test
    public void createLoyaltyPointConfigInvalidAuthN() {
        var request = getHttpEntity("invalid");
        var response = template.postForEntity("/loyalty/config/update", request, String.class);
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
        var response = template.postForEntity("/loyalty/tier/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(tierRepository.findByTierName("tier test").isPresent());
        tierRepository.deleteByTierName("tier test");
    }

    @Test
    public void creatTierInvalidAuthZ() {
        String body = getTierJsonString();

        var request = getHttpEntity(body, "invalid");
        var response = template.postForEntity("/loyalty/tier/update", request, String.class);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    public void creatAndGetTier() throws JsonProcessingException {
        String body = getTierJsonString();

        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/loyalty/tier/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(tierRepository.findByTierName("tier test").isPresent());

        request = getHttpEntity("abcdefg");
        response = template.exchange("/loyalty/tier/list?org_id=99001", GET, request, String.class);
        List<LoyaltyTierDTO> resBody = mapper.readValue(response.getBody(), new TypeReference<>(){});
        assertEquals(3, resBody.size());

        LoyaltyTierDTO loyaltyTierDTO = resBody.get(2);

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
        var response = template.postForEntity("/loyalty/tier/update", request, LoyaltyTierUpdateResponse.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(tierRepository.findByTierName("tier test").isPresent());

        Long id = response.getBody().getTierId();
        var changeRequest = getHttpEntity("abcdefg");
        var changeResponse = template.postForEntity("/loyalty/tier/change_user_tier?org_id=99001&user_id=88&tier_id="+id, changeRequest, UserRepresentationObject.class);

        assertEquals(200, changeResponse.getStatusCodeValue());
        UserEntity user = userRepository.getOne(88L);

        assertEquals(id, changeResponse.getBody().getTierId() );

    }


    @Test
    @Ignore("api is hidden for now")
    public void updateLoyaltyPointType() {
        String body = json()
                .put("id", 31001)
                .put("name", "test type")
                .toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/loyalty/type/update", request, LoyaltyPointsUpdateResponse.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().getLoyaltyPointId() > 0L);
     }


    @Test
    public void updateLoyaltyPointConfigInvalidId() {
        String body = createConfigJson()
                .put("id", 31002)
                .toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/loyalty/config/update", request, String.class);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void getUserPointsNoUserInOrg() {
        var request = getHttpEntity("101112");
        var response = template.exchange("/loyalty/points", GET, request, String.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void getUserPoints() {
        var request = getHttpEntity("123");
        var response = template.exchange("/loyalty/points", GET, request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("points"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Loyalty_point_Test_Data_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    @Test
    public void getUserSpendablePoints() {
        HttpEntity<?> request = getHttpEntity("123");

        ResponseEntity<List<LoyaltyPointTransactionDTO>> response = template.exchange("/loyalty/spendable_points", GET, request, new ParameterizedTypeReference<List<LoyaltyPointTransactionDTO>>() {
        });
        Assert.assertEquals(OK, response.getStatusCode());

        List<LoyaltyPointTransactionDTO> spendablePoints = response.getBody().stream().collect(toList());
        assertEquals(1, spendablePoints.size());
        assertEquals(3, spendablePoints.get(0).getId());

    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Loyalty_point_Test_Data_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    @Test
    public void listPointsByUserId() {
        HttpEntity<?> request = getHttpEntity("abc");

        ResponseEntity<List<LoyaltyPointTransactionDTO>> response = template.exchange("/loyalty/points/list_by_user?user_id=88", GET, request, new ParameterizedTypeReference<List<LoyaltyPointTransactionDTO>>() {
        });
        Assert.assertEquals(OK, response.getStatusCode());

        List<LoyaltyPointTransactionDTO> spendablePoints = response.getBody().stream().collect(toList());
        assertEquals(2, spendablePoints.size());

    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Loyalty_point_Test_Data_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    @Test
    public void SharePoint() {
        HttpEntity<?> request = getHttpEntity("123");

        ResponseEntity<Void> response = template.exchange("/loyalty/share_points?org_id=99001&email=test3@nasnav.com&points=4",
                POST,
                request,Void.class);
        Assert.assertEquals(OK, response.getStatusCode());

    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Loyalty_point_Test_Data_Insert.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    @Test
    public void getSpendablePointsAndSharePoint() {
        HttpEntity<?> request = getHttpEntity("456");

        ResponseEntity<List<LoyaltyPointTransactionDTO>> response = template.exchange("/loyalty/spendable_points", GET, request, new ParameterizedTypeReference<List<LoyaltyPointTransactionDTO>>() {
        });
        Assert.assertEquals(OK, response.getStatusCode());
        Assert.assertEquals(BigDecimal.valueOf(30), response.getBody().get(0).getPoints());

        List<LoyaltyPointTransactionDTO> spendablePoints = response.getBody().stream().collect(toList());
        Long point_id = spendablePoints.get(0).getId();
        String email = "test4@nasnav.com";
        BigDecimal points = BigDecimal.valueOf(5);
        ResponseEntity<Void> res = template.exchange(
                "/loyalty/share_points?org_id="+"99002"+"&email="+email+"&points="+points,
                POST,
                request,Void.class);
        Assert.assertEquals(OK, res.getStatusCode());
        ResponseEntity<List<LoyaltyPointTransactionDTO>> resAfterShare = template.exchange("/loyalty/spendable_points", GET, request, new ParameterizedTypeReference<List<LoyaltyPointTransactionDTO>>() {
        });
        Assert.assertEquals(BigDecimal.valueOf(25), resAfterShare.getBody().get(0).getPoints());


        HttpEntity<?> request2 = getHttpEntity("258");
        ResponseEntity<List<LoyaltyPointTransactionDTO>> resAfterShareToSharedUser = template.exchange("/loyalty/spendable_points", GET, request2, new ParameterizedTypeReference<List<LoyaltyPointTransactionDTO>>() {
        });
        Assert.assertEquals(BigDecimal.valueOf(5), resAfterShareToSharedUser.getBody().get(0).getPoints());

        assertEquals(4, spendablePoints.get(0).getId());
        assertEquals(1, spendablePoints.size());
    }

}
