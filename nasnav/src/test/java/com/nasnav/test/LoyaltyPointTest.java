package com.nasnav.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.*;
import com.nasnav.dto.request.*;
import com.nasnav.dto.response.RedeemPointsOfferDTO;
import com.nasnav.persistence.LoyaltyFamilyEntity;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.json.JSONObject.NULL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@NotThreadSafe
@PropertySource("classpath:test.database.properties")
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Loyalty_Point_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class LoyaltyPointTest {

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
    UserCharityRepository userCharityRepository;
    @Autowired
    LoyaltyGiftRepository loyaltyGiftRepository;

    @Autowired
    private LoyaltyPointTransactionRepository transactionRepo;

    @Test
    public void createLoyaltyPointType() {
        String body = json().put("name", "type 1").toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/loyalty/type/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(typeRepository.findByName("type 1").isPresent());
        typeRepository.deleteByName("type 1");
    }

    @Test
    public void createLoyaltyPointTypeInvalidAuthZ() {
        String body = json().put("name", "type 1").toString();
        var request = getHttpEntity(body, "invalid");
        var response = template.postForEntity("/loyalty/type/update", request, String.class);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    public void createLoyaltyPointTypeInvalidAuthN() {
        String body = json().put("name", "type 1").toString();
        var request = getHttpEntity(body, "101112");
        var response = template.postForEntity("/loyalty/type/update", request, String.class);
        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    public void updateLoyaltyPointType() {
        String body = json()
                .put("id", 31001)
                .put("name", "type 1")
                .toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/loyalty/type/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(typeRepository.findByName("type 1").isPresent());
        typeRepository.deleteByName("type 1");
    }

    @Test
    public void getLoyaltyPointType() throws JsonProcessingException {
        var request = getHttpEntity("abcdefg");
        var response = template.exchange("/loyalty/type/list", GET, request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        List<LoyaltyPointTypeDTO> body = mapper.readValue(response.getBody(), new TypeReference<List<LoyaltyPointTypeDTO>>() {});
        assertEquals(1, body.size());
        assertEquals(31001, body.get(0).getId());
        assertEquals("old name", body.get(0).getName());
    }


    @Test
    @Ignore
    public void createLoyaltyPointConfigMissingParams() {
        String body = createConfigJson()
                .put("shop_id", NULL)
                .toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/loyalty/config/update", request, String.class);
        assertEquals(406, response.getStatusCodeValue());

        body = createConfigJson()
                .put("amount_from", NULL)
                .toString();
        request = getHttpEntity(body, "abcdefg");
        response = template.postForEntity("/loyalty/config/update", request, String.class);
        assertEquals(406, response.getStatusCodeValue());

        body = createConfigJson()
                .put("amount_to", NULL)
                .toString();
        request = getHttpEntity(body, "abcdefg");
        response = template.postForEntity("/loyalty/config/update", request, String.class);
        assertEquals(406, response.getStatusCodeValue());

        body = createConfigJson()
                .put("points", NULL)
                .toString();
        request = getHttpEntity(body, "abcdefg");
        response = template.postForEntity("/loyalty/config/update", request, String.class);
        assertEquals(406, response.getStatusCodeValue());
    }

    @Test
    public void createLoyaltyPointConfigInvalidAuthN() {
        var request = getHttpEntity("invalid");
        var response = template.postForEntity("/loyalty/config/update", request, String.class);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    public void createLoyaltyPointConfigInvalidAuthZ() {
        var request = getHttpEntity("101112");
        var response = template.postForEntity("/loyalty/config/update", request, String.class);
        assertEquals(403, response.getStatusCodeValue());
    }

    private JSONObject createConfigJson() {
        return json()
                .put("description", "this is a configuration")
                .put("shop_id", 501)
                .put("amount_from", 100)
                .put("amount_to", 1000)
                .put("points", 10);
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
    public void createAndGetLoyaltyPoint() throws JsonProcessingException {
        String body = createLoyaltyPointJson().toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/loyalty/points/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());

        request = getHttpEntity("abcdefg");
        response = template.exchange("/loyalty/points/list", GET, request, String.class);
        List<LoyaltyPointDTO> resBody = mapper.readValue(response.getBody(), new TypeReference<>(){});
        LoyaltyPointDTO loyaltyPoint = resBody.get(0);
        assertEquals(3, resBody.size());
        assertEquals("this is a loyalty point", loyaltyPoint.getDescription());
        assertEquals(31001, loyaltyPoint.getTypeId());
        assertEquals(10000, loyaltyPoint.getAmount());
        assertEquals(100, loyaltyPoint.getPoints());
        assertTrue(loyaltyPoint.getStartDate() != null);
        assertTrue( loyaltyPoint.getEndDate() != null);
    }

    private JSONObject createLoyaltyPointJson() {
        return json()
                .put("description", "this is a loyalty point")
                .put("type_id", 31001)
                .put("amount", 10000)
                .put("points", 100)
                .put("start_date", LocalDateTime.now())
                .put("end_date", LocalDateTime.now().plusMonths(1));
    }

    @Test
    public void testUserObtainPoints() throws JsonProcessingException {
        // confirming order
        var request = getHttpEntity("abcdefg");
        var response = template.postForEntity("/order/confirm?order_id=33001", request, String.class);
        assertEquals(200, response.getStatusCodeValue());

        //fetch available points
        request = getHttpEntity("123");
        response = template.exchange("/loyalty/points/check?code=code1", GET, request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        List<RedeemPointsOfferDTO> resBody = mapper.readValue(response.getBody(), new TypeReference<>(){});
        assertFalse(resBody.isEmpty());
        Long pointId = resBody.get(0).getPointId();
        Long userId = 88L;

        //obtain user points
        request = getHttpEntity("192021");
        response = template.postForEntity("/loyalty/points/redeem?point_id="+pointId+"&user_id="+userId, request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, transactionRepo.findOrgRedeemablePoints(userId, 501L));
    }


    // family

    @Test
    public void createFamily(){
        String body = json().put("family_name", "family 1")
                .put("parent_id", "0")
                .put("booster_id", 199001)
                .put("org_id", 99001)
                .put("is_active", true)
                .toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/loyalty/family/update", request, String.class);
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
        var response = template.postForEntity("/loyalty/family/update", request, String.class);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    public void creatAndGetFamily() throws JsonProcessingException {
        String body = json().put("family_name", "family 1")
                .put("parent_id", "0")
                .put("booster_id", 199001)
                .put("org_id", 99001)
                .put("is_active", true)
                .toString();

        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/loyalty/family/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(loyaltyFamilyRepository.findByFamilyName("family 1").isPresent());

        request = getHttpEntity("abcdefg");
        response = template.exchange("/loyalty/family/list", GET, request, String.class);
        List<LoyaltyFamilyEntity> resBody = mapper.readValue(response.getBody(), new TypeReference<>(){});
        LoyaltyFamilyEntity loyaltyFamilyEntity = resBody.get(0);
        assertEquals(1, resBody.size());
        assertEquals("family 1", loyaltyFamilyEntity.getFamilyName());

        loyaltyFamilyRepository.deleteByFamilyName("family 1");
    }


    // tire
    @Test
    public void createTier(){
        String body = json().put("tier_name", "tier 1")
                .put("parent_id", "0")
                .put("booster_id", 199001)
                .put("selling_price", 90)
                .put("no_of_purchase_from", 0)
                .put("no_of_purchase_from", 0)
                .put("org_id", 99001)
                .put("booster_id", 199001)
                .put("is_active", true)
                .put("is_special", true)
                .toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/loyalty/tier/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(tierRepository.findByTierName("tier 1").isPresent());
        tierRepository.deleteByTierName("tier 1");
    }

    @Test
    public void creatTierInvalidAuthZ() {
        String body = json().put("tier_name", "tier 1")
                .put("parent_id", "0")
                .put("booster_id", 199001)
                .put("selling_price", 90)
                .put("no_of_purchase_from", 0)
                .put("no_of_purchase_from", 0)
                .put("org_id", 99001)
                .put("booster_id", 199001)
                .put("is_active", true)
                .put("is_special", true)
                .toString();

        var request = getHttpEntity(body, "invalid");
        var response = template.postForEntity("/loyalty/tier/update", request, String.class);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    public void creatAndGetTier() throws JsonProcessingException {
        String body = json().put("tier_name", "tier 1")
                .put("parent_id", "0")
                .put("booster_id", 199001)
                .put("selling_price", 90)
                .put("no_of_purchase_from", 0)
                .put("no_of_purchase_from", 0)
                .put("org_id", 99001)
                .put("booster_id", 199001)
                .put("is_active", true)
                .put("is_special", true)
                .toString();
        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/loyalty/tier/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(tierRepository.findByTierName("tier 1").isPresent());

        request = getHttpEntity("abcdefg");
        response = template.exchange("/loyalty/tier/list", GET, request, String.class);
        List<LoyaltyTierDTO> resBody = mapper.readValue(response.getBody(), new TypeReference<>(){});
        LoyaltyTierDTO loyaltyTierDTO = resBody.get(0);
        assertEquals(1, resBody.size());
        assertEquals("tier 1", loyaltyTierDTO.getTierName());

        tierRepository.deleteByTierName("tier 1");
    }

    //Booster
    @Test
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
        var response = template.postForEntity("/loyalty/booster/update", request, String.class);
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
        var response = template.postForEntity("/loyalty/booster/update", request, String.class);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
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
        var response = template.postForEntity("/loyalty/booster/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(loyaltyBoosterRepository.findByBoosterName("booster 1").isPresent());


        request = getHttpEntity("abcdefg");
        response = template.exchange("/loyalty/booster/list", GET, request, String.class);
        List<LoyaltyBoosterDTO> resBody = mapper.readValue(response.getBody(), new TypeReference<>(){});
        LoyaltyBoosterDTO booster = resBody.get(1);
        assertEquals(2, resBody.size());
        assertEquals("booster 1", booster.getBoosterName());

        loyaltyBoosterRepository.deleteByBoosterName("booster 1");
    }

    //Charity
    @Test
    public void creatCharity() {
        String body = json().put("donation_percentage", 10)
                .put("charity_id", 1)
                .put("is_active", true)
                .put("user_id", 424)
                .toString();

        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/loyalty/charity/user/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(userCharityRepository.findByUser_IdAndCharity_Id(424L, 1L).isPresent());
        userCharityRepository.deleteByUser_IdAndCharity_Id(424L, 1L);
    }

    //Gift

    @Test
    public void creatGift() {

         String body = json().put("user_from_id", 424)
                .put("user_to_id", 333)
                .put("is_active", true)
                .put("points", 10)
                .put("phone_number", "4564644")
                .put("email", "mail@mail.com")
                .put("is_redeem", true)
                .toString();

        var request = getHttpEntity(body, "abcdefg");
        var response = template.postForEntity("/loyalty/gift/send", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(loyaltyGiftRepository.findByEmail("mail@mail.com").isPresent());
        loyaltyGiftRepository.deleteByEmail("email@email.com");
    }
}
