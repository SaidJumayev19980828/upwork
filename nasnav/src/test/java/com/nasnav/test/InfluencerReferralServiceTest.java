package com.nasnav.test;


import com.nasnav.dao.InfluencerReferralRepository;
import com.nasnav.dao.ReferralCodeRepo;
import com.nasnav.dao.ReferralTransactionRepository;
import com.nasnav.dao.ReferralWalletRepository;
import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.InfluencerReferralConstraints;
import com.nasnav.dto.referral_code.InfluencerReferralDto;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.enumerations.ReferralType;
import com.nasnav.exceptions.ErrorResponseDTO;
import com.nasnav.mappers.InfluencerReferralMapper;
import com.nasnav.persistence.ReferralCodeEntity;
import com.nasnav.persistence.ReferralTransactions;
import com.nasnav.persistence.ReferralWallet;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.concurrent.NotThreadSafe;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Influencer_Referral_Code_Test_Data.sql"})
@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Influencer_Referral_Code_Test_Data.sql"})
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class InfluencerReferralServiceTest extends AbstractTestWithTempBaseDir {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ReferralWalletRepository referralWalletRepository;

    @Autowired
    private ReferralTransactionRepository referralTransactionRepository;

    @Autowired
    private ReferralCodeRepo referralCodeRepo;

    @Autowired
    private InfluencerReferralMapper influencerReferralMapper;

    @Autowired
    private InfluencerReferralRepository influencerReferralRepository;


    @Test
    public void registerInfluencerReferral() {
        HttpEntity<?> request = getRegisterInfluencerRequest("123");

        ResponseEntity<InfluencerReferralDto> res = template.postForEntity("/influencer-referral/register", request,
                InfluencerReferralDto.class);
        assertEquals(200, res.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(res.getBody()).getId() > 0);

        ReferralWallet referralWallet = referralWalletRepository
                .findByUserIdAndReferralType(res.getBody().getId(), ReferralType.INFLUENCER)
                .get();
        assertNotNull(referralWallet);

        ReferralCodeEntity referralCodeEntity = referralCodeRepo
                .findByReferralCodeAndReferralType("abcd", ReferralType.INFLUENCER)
                .get();
        assertNotNull(referralCodeEntity);

        List<ReferralTransactions> referralTransactions = referralTransactionRepository
                .findByReferralWallet_Id(referralWallet.getId());
        assertEquals(1, referralTransactions.size());
        assertEquals(ReferralTransactionsType.INFLUENCER_REGISTERATION, referralTransactions.get(0).getType());
    }


    @ParameterizedTest
    @CsvSource({
            "1234,Passwords Doesn't match!"
    })
    void registerInfluencerReferralPasswordsNotEqual(String confirmPassword, String expectedMessage) {
        HttpEntity<?> request = getRegisterInfluencerRequest(confirmPassword);

        ResponseEntity<ErrorResponseDTO> res = template.postForEntity("/influencer-referral/register", request,
                ErrorResponseDTO.class);
        assertEquals(406, res.getStatusCodeValue());
        assertEquals(expectedMessage, res.getBody().getMessage());
    }


    @ParameterizedTest
    @CsvSource({
            "123,User name already exists!"
    })
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Influencer_Referral_Code_Test_Data_1.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void registerInfluencerReferralUserAlreadyExists(String confirmPassword, String expectedMessage) {
        HttpEntity<?> request = getRegisterInfluencerRequest(confirmPassword);

        ResponseEntity<ErrorResponseDTO> res = template.postForEntity("/influencer-referral/register", request,
                ErrorResponseDTO.class);
        assertEquals(406, res.getStatusCodeValue());
        assertEquals(expectedMessage, res.getBody().getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "1234,There is already promotion code with same code!"
    })
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Influencer_Referral_Code_Test_Data_2.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void registerInfluencerReferralUserWithExistingPromotionCode(String confirmPassword, String expectedMessage) {
        HttpEntity<?> request = getRegisterInfluencerRequest(confirmPassword);

        ResponseEntity<ErrorResponseDTO> res = template.postForEntity("/influencer-referral/register", request,
                ErrorResponseDTO.class);
        assertEquals(406, res.getStatusCodeValue());
        assertEquals(expectedMessage, res.getBody().getMessage());
    }

    @NotNull
    private static HttpEntity<?> getRegisterInfluencerRequest(String confirmPassword) {
        JSONObject body = new JSONObject();
        body.put("firstName", "mohamed");
        body.put("lastName", "shaker");
        body.put("userName", "mohamedshaker");
        body.put("password", "123");
        body.put("confirmPassword", confirmPassword);
        body.put("referralCode", "abcd");

        HttpEntity<?> request = getHttpEntity(body.toString(), "101112");
        return request;
    }

    @ParameterizedTest
    @CsvSource({
            "mohamedshaker,123"
    })
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Influencer_Referral_Code_Test_Data_1.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getinfluencerCashBack(String username, String password) {
        HttpEntity<?> request = getHttpEntity("101112");

        ResponseEntity<InfluencerReferralDto> res = template.exchange("/influencer-referral/cashback?username=" + username
                        + "&password=" + password,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<InfluencerReferralDto>() {
                });

        assertEquals(200, res.getStatusCodeValue());
        assertEquals(new BigDecimal("200.0"), res.getBody().getCashback());
    }

    @ParameterizedTest
    @CsvSource({
            "mohamedshaker,1234,User name or password incorrect!"
    })
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Influencer_Referral_Code_Test_Data_1.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getinfluencerCashBackPasswordIncorrect(String username, String password, String expectedMessege) {
        HttpEntity<?> request = getHttpEntity("101112");

        ResponseEntity<ErrorResponseDTO> res = template.exchange("/influencer-referral/cashback?username=" + username
                        + "&password=" + password,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<ErrorResponseDTO>() {
                });

        assertEquals(406, res.getStatusCodeValue());
        assertEquals(expectedMessege, res.getBody().getMessage());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Influencer_Referral_Code_Test_Data_3.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getAllInfluencers() {
        HttpEntity<?> request = getHttpEntity("101112");

        ResponseEntity<PaginatedResponse<InfluencerReferralDto>> res = template.exchange("/influencer-referral",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<PaginatedResponse<InfluencerReferralDto>>() {
                });

        assertEquals(200, res.getStatusCodeValue());
        assertEquals(Long.valueOf(2), res.getBody().getTotalRecords());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Referral_Code_Test_Data_Influencer_Cash_Back.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void influencerGetCashbackWhenStatusUpdatedToDelivered() {
        updateStutsToDelivered();

        ReferralWallet referralWallet = referralWalletRepository
                .findByUserIdAndReferralType(1000L, ReferralType.INFLUENCER).get();
        assertEquals(new BigDecimal("220.00"), referralWallet.getBalance());
        List<ReferralTransactions> referralTransactions = referralTransactionRepository
                .findByReferralWallet_Id(500L);
        assertEquals(1, referralTransactions.size());
        assertEquals(ReferralTransactionsType.INFLUENCER_CASHBACK, referralTransactions.get(0).getType());
        assertEquals(new BigDecimal("20.0"), referralTransactions.get(0).getAmount());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Referral_Code_Test_Data_Influencer_Cash_Back_3.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void influencerGetCashbackPercentageWhenStatusUpdatedToDelivered() {
        updateStutsToDelivered();

        ReferralWallet referralWallet = referralWalletRepository
                .findByUserIdAndReferralType(1000L, ReferralType.INFLUENCER).get();
        assertEquals(new BigDecimal("236.00"), referralWallet.getBalance());
        List<ReferralTransactions> referralTransactions = referralTransactionRepository
                .findByReferralWallet_Id(500L);
        assertEquals(1, referralTransactions.size());
        assertEquals(ReferralTransactionsType.INFLUENCER_CASHBACK, referralTransactions.get(0).getType());
        assertEquals(new BigDecimal("36.00"), referralTransactions.get(0).getAmount());
    }

    @Test
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Referral_Code_Test_Data_Influencer_Cash_Back_1.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void influencerGetCashbackWhenStatusUpdatedToDeliveredNotDepositedAsNoInfluencerReferral() {
        updateStutsToDelivered();

        ReferralWallet referralWallet = referralWalletRepository
                .findByUserIdAndReferralType(1000L, ReferralType.INFLUENCER).get();
        assertEquals(new BigDecimal("200.00"), referralWallet.getBalance());
        List<ReferralTransactions> referralTransactions = referralTransactionRepository
                .findByReferralWallet_Id(500L);
        assertEquals(0, referralTransactions.size());
    }


    private void updateStutsToDelivered() {
        Long orderId = 330033L;
        String userToken = "252627";

        JSONObject request = new JSONObject();
        request.put("status", OrderStatus.DELIVERED.name());
        request.put("order_id", orderId);

        ResponseEntity<String> updateResponse =
                template.postForEntity("/order/status/update"
                        , getHttpEntity(request.toString(), userToken)
                        , String.class);

        assertEquals(OK, updateResponse.getStatusCode());
    }

    @ParameterizedTest
    @CsvSource({
            "20.0,0,10.0,0,20.0,0,10.0,0",
            "20.0,0,0,0.01,20.0,0,0,0.01",
            "0,0.02,10.0,0,0,0.02,10.0,0",
            "0,0.02,0,.01,0,0.02,0,.01",

    })
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Influencer_Referral_Code_Test_Data_1.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void updateReferralOrganizationSettings(BigDecimal expectedDiscountValue, BigDecimal expectedDiscountPercentage,
                                                   BigDecimal expectedCashbackValue, BigDecimal expectedCashbackPercentage,
                                                   BigDecimal discountValue, BigDecimal discountPercentage,
                                                   BigDecimal cashbackValue, BigDecimal cashbackPercentage) throws IOException {

        String bodyJsonRequest = influencerReferralMapper.map(getInfluencerReferralConstraintsSettings(
                discountValue, discountPercentage, cashbackValue, cashbackPercentage
        ));

        HttpEntity<?> request = getHttpEntity(bodyJsonRequest, "131415");

        ResponseEntity<Void> res = template.exchange("/influencer-referral/" + "mohamedshaker" + "/settings", HttpMethod.PUT,
                request, Void.class);
        assertEquals(200, res.getStatusCodeValue());

        InfluencerReferralConstraints influencerReferralConstraints =
                influencerReferralMapper.map(influencerReferralRepository.findByUserName("mohamedshaker")
                        .get().getReferralSettings().getConstraints());

        assertEquals(expectedDiscountValue, influencerReferralConstraints.getDiscountValue());
        assertEquals(expectedDiscountPercentage, influencerReferralConstraints.getDiscountPercentage());
        assertEquals(expectedCashbackValue, influencerReferralConstraints.getCashbackValue());
        assertEquals(expectedCashbackPercentage, influencerReferralConstraints.getCashbackPercentage());
    }

    @ParameterizedTest
    @CsvSource({
            "20.0,0,10.0,0.01,Choose only one of cashback strategy!",

    })
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Influencer_Referral_Code_Test_Data_1.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void updateReferralOrganizationSettingsThrowException(BigDecimal discountValue, BigDecimal discountPercentage,
                                                   BigDecimal cashbackValue, BigDecimal cashbackPercentage, String message) throws IOException {

        String bodyJsonRequest = influencerReferralMapper.map(getInfluencerReferralConstraintsSettings(
                discountValue, discountPercentage, cashbackValue, cashbackPercentage
        ));

        HttpEntity<?> request = getHttpEntity(bodyJsonRequest, "131415");

        ResponseEntity<ErrorResponseDTO> res = template.exchange("/influencer-referral/" + "mohamedshaker" + "/settings", HttpMethod.PUT,
                request, ErrorResponseDTO.class);

        assertEquals(406, res.getStatusCodeValue());
        assertEquals(message, res.getBody().getMessage());
    }

    private InfluencerReferralConstraints getInfluencerReferralConstraintsSettings(BigDecimal discountValue, BigDecimal discountPercentage,
                                                                                   BigDecimal cashbackValue, BigDecimal cashbackPercentage) {
        return InfluencerReferralConstraints.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .discountValue(discountValue)
                .discountPercentage(discountPercentage)
                .cashbackValue(cashbackValue)
                .cashbackPercentage(cashbackPercentage)
                .products(List.of(1004L, 1003L, 1002L, 1001L))
                .build();
    }

}
