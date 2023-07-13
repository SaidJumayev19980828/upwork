package com.nasnav.yeshtery.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.nasnav.dao.*;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.*;
import com.nasnav.dto.response.RedeemPointsOfferDTO;
import com.nasnav.enumerations.LoyaltyPointType;
import com.nasnav.persistence.LoyaltyFamilyEntity;
import com.nasnav.persistence.LoyaltyPointConfigEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.*;
import com.nasnav.service.LoyaltyPointsService;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.nasnav.exceptions.ErrorCodes.ORG$LOY$0014;
import static com.nasnav.yeshtery.test.commons.TestCommons.*;
import static org.json.JSONObject.NULL;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
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
    private LoyaltyPointTypeRepository typeRepository;
    @Autowired
    private LoyaltyTierRepository tierRepository;
    @Autowired
    private LoyaltyBoosterRepository loyaltyBoosterRepository;
    @Autowired
    private LoyaltyFamilyRepository loyaltyFamilyRepository;
    @Autowired
    private UserCharityRepository userCharityRepository;
    @Autowired
    private LoyaltyGiftRepository loyaltyGiftRepository;
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
        var response = template.postForEntity("/v1/loyalty/config/update", request, LoyaltyPointsUpdateResponse.class);
        assertEquals(406, response.getStatusCodeValue());

        request = getHttpEntity(body, "abcdefg");

        ResponseEntity<List> responseList = template.exchange("/v1/loyalty/config/list?org_id=99001", GET, request, List.class);

        assertEquals(200, responseList.getStatusCodeValue());
        assertEquals(1, responseList.getBody().size());

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
    

    private LoyaltyPointConfigDTO getLoyaltyPointConfigDto() {
        JSONObject configJson = createConfigJson();
        return  new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create().fromJson(configJson.toString(), LoyaltyPointConfigDTO.class);
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

    /**
     * Type
     */


    @Test
    @Ignore("api is hidden for now")
    public void createLoyaltyPointType() {
        String body = json().put("name", "test type").toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/v1/loyalty/type/update", request, LoyaltyPointsUpdateResponse.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().getLoyaltyPointId() > 0L);
        typeRepository.deleteByName("test type");
    }

    @Test
    public void createLoyaltyPointTypeInvalidAuthZ() {
        String body = json().put("name", "test type").toString();
        var request = getHttpEntity(body, "invalid");
        var response = template.postForEntity("/v1/loyalty/type/update", request, String.class);
        assertEquals(401, response.getStatusCodeValue());
        typeRepository.deleteByName("test type");

    }

    @Test
    @Ignore("api is hidden for now")
    public void updateLoyaltyPointType() {
        String body = json()
                .put("id", 31001)
                .put("name", "test type")
                .toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/v1/loyalty/type/update", request, LoyaltyPointsUpdateResponse.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().getLoyaltyPointId() > 0L);
     }

    @Test
    @Ignore("api is hidden for now")
    public void getLoyaltyPointType() throws JsonProcessingException {
        var request = getHttpEntity("abcdefg");
        var response = template.exchange("/v1/loyalty/type/list", GET, request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        List<LoyaltyPointTypeDTO> body = mapper.readValue(response.getBody(), new TypeReference<List<LoyaltyPointTypeDTO>>() {});
        assertEquals(1, body.size());
        assertEquals(31001, body.get(0).getId());
        assertEquals("old name", body.get(0).getName());
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


    private JSONObject createLoyaltyPointJson() {
        return json()
                .put("description", "this is a loyalty point")
                .put("type_id", 31001)
                .put("amount", 10000)
                .put("points", 100)
                .put("org_id", 99001)
                .put("start_date", LocalDateTime.now())
                .put("end_date", LocalDateTime.now().plusMonths(1));
    }

    @Test
    @Ignore("api is hidden for now")
    public void testUserObtainPoints() throws JsonProcessingException {
        // confirming order
        var request = getHttpEntity("abcdefg");
        var response = template.postForEntity("/v1/order/confirm?order_id=33001", request, String.class);
        assertEquals(200, response.getStatusCodeValue());

        //fetch available points
        request = getHttpEntity("123");
        response = template.exchange("/v1/loyalty/points/check?code=code1", GET, request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        List<RedeemPointsOfferDTO> resBody = mapper.readValue(response.getBody(), new TypeReference<>(){});
        assertFalse(resBody.isEmpty());
        Long pointId = resBody.get(0).getPointId();
        Long userId = 88L;

        //obtain user points
        request = getHttpEntity("192021");
        response = template.postForEntity("/v1/loyalty/points/redeem?point_id="+pointId+"&user_id="+userId, request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, transactionRepo.findOrgRedeemablePoints(userId, 99001L));
    }

    @Test
    public void getUserPointsNoUserInOrg() {
        var request = getHttpEntity("123");
        var response = template.exchange("/v1/loyalty/points?org_id=99001", GET, request, String.class);
        assertEquals(404, response.getStatusCodeValue());
        assertTrue(response.getBody().contains(ORG$LOY$0014.name()));
    }


    // family

    @Test
    @Ignore("api is hidden for now")
    public void createFamily(){
        String body = json().put("family_name", "family 1")
                .put("parent_id", "0")
                .put("booster_id", 199001)
                .put("org_id", 99001)
                .put("is_active", true)
                .toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/v1/loyalty/family/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(loyaltyFamilyRepository.findByFamilyName("family 1").isPresent());
        loyaltyFamilyRepository.deleteByFamilyName("family 1");
    }


    @Test
    public void creatFamilyInvalidAuthZ() {
        String body = json().put("family_name", "family 1")
                .put("parent_id", "0")
                .put("booster_id", 199001)
                .put("org_id", 99001)
                .put("is_active", true)
                .toString();

        var request = getHttpEntity(body, "invalid");
        var response = template.postForEntity("/v1/loyalty/family/update", request, String.class);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    @Ignore("family apis are hidden for now")
    public void creatAndGetFamily() throws JsonProcessingException {
        String body = json().put("family_name", "family 1")
                .put("parent_id", "0")
                .put("booster_id", 199001)
                .put("org_id", 99001)
                .put("is_active", true)
                .toString();

        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/v1/loyalty/family/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(loyaltyFamilyRepository.findByFamilyName("family 1").isPresent());

        request = getHttpEntity("abcdefg");
        response = template.exchange("/v1/loyalty/family/list", GET, request, String.class);
        List<LoyaltyFamilyEntity> resBody = mapper.readValue(response.getBody(), new TypeReference<>(){});
        LoyaltyFamilyEntity loyaltyFamilyEntity = resBody.get(0);
        assertEquals(1, resBody.size());
        assertEquals("family 1", loyaltyFamilyEntity.getFamilyName());

        loyaltyFamilyRepository.deleteByFamilyName("family 1");
    }


    //Booster
    @Test
    @Ignore("api is hidden for now")
    public void creatBooster() {
        String body = json().put("booster_name", "booster 1")
                .put("linked_family_member", "0")
                .put("is_active", true)
                .put("number_family_children", "0")
                .put("purchase_size", "0")
                .put("review_products", "0")
                .put("social_media_reviews", "0")
                .put("number_purchase_offline", "0")
                .put("org_id", 99001)
                .put("level_booster", "0")
                .put("activation_months", "0")
                .toString();

        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/v1/loyalty/booster/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(loyaltyBoosterRepository.findByBoosterName("booster 1").isPresent());
        loyaltyBoosterRepository.deleteByBoosterName("booster 1");
    }

    @Test
    public void creatBoosterInvalidAuthZ() {
        String body = json().put("booster_name", "booster 1")
                .put("linked_family_member", "0")
                .put("is_active", true)
                .put("number_family_children", "0")
                .put("purchase_size", "0")
                .put("review_products", "0")
                .put("social_media_reviews", "0")
                .put("number_purchase_offline", "0")
                .put("org_id", 99001)
                .put("level_booster", "0")
                .put("activation_months", "0")
                .toString();

        var request = getHttpEntity(body, "invalid");
        var response = template.postForEntity("/v1/loyalty/booster/update", request, String.class);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    @Ignore("api is hidden for now")
    public void creatAndGetBooster() throws JsonProcessingException {
        String body = json().put("booster_name", "booster 1")
                .put("linked_family_member", "0")
                .put("is_active", true)
                .put("number_family_children", "0")
                .put("purchase_size", "0")
                .put("review_products", "0")
                .put("social_media_reviews", "0")
                .put("number_purchase_offline", "0")
                .put("org_id", 99001)
                .put("level_booster", "0")
                .put("activation_months", "0")
                .toString();

        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/v1/loyalty/booster/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(loyaltyBoosterRepository.findByBoosterName("booster 1").isPresent());


        request = getHttpEntity("abcdefg");
        response = template.exchange("/v1/loyalty/booster/list", GET, request, String.class);
        List<LoyaltyBoosterDTO> resBody = mapper.readValue(response.getBody(), new TypeReference<>(){});
        LoyaltyBoosterDTO booster = resBody.get(1);
        assertEquals(2, resBody.size());
        assertEquals("booster 1", booster.getBoosterName());

        loyaltyBoosterRepository.deleteByBoosterName("booster 1");
    }

    //Charity
    @Test
    @Ignore("api is hidden for now")
    public void creatCharity() {
        String body = json().put("donation_percentage", 10)
                .put("is_active", true)
                .put("user_id", 88)
                .toString();

        var request = getHttpEntity(body, "123");
        var response = template.postForEntity("/v1/loyalty/charity/user/update", request, LoyaltyCharityUpdateResponse.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(userCharityRepository.findById(response.getBody().getCharityId()).isPresent());
        userCharityRepository.deleteById( response.getBody().getCharityId());
    }

    //Gift

    @Test
    @Ignore("api is hidden for now")
    public void creatGift() {

         String body = json().put("user_from_id", 424)
                .put("user_to_id", 333)
                .put("is_active", true)
                .put("points", 10)
                .put("phone_number", "4564644")
                .put("user_to_email", "user2@nasnav.com")
                .put("is_redeem", true)
                .put("org_id", 99001)
                .toString();

        var request = getHttpEntity(body, "456");
        var response = template.postForEntity("/v1/loyalty/gift/send", request, LoyaltyGiftUpdateResponse.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().getGiftId() > 0L);
        loyaltyGiftRepository.deleteById(response.getBody().getGiftId());
    }
}
