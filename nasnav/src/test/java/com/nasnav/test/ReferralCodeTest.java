package com.nasnav.test;


import com.nasnav.dao.*;
import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.ReferralCodeDto;
import com.nasnav.dto.referral_code.ReferralConstraints;
import com.nasnav.dto.referral_code.ReferralStatsDto;
import com.nasnav.dto.referral_code.ReferralTransactionsDto;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.enumerations.ReferralCodeStatus;
import com.nasnav.enumerations.ReferralCodeType;
import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.integration.MobileOTPService;
import com.nasnav.integration.smsmisr.dto.OTPDto;
import com.nasnav.persistence.*;
import com.nasnav.service.ReferralCodeService;
import com.nasnav.service.ReferralWalletService;
import com.nasnav.service.UserService;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.concurrent.NotThreadSafe;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class ReferralCodeTest  extends AbstractTestWithTempBaseDir {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ReferralCodeService referralCodeService;

    @Autowired
    private ReferralWalletService referralWalletService;

    @Autowired
    private ReferralTransactionRepository referralTransactionRepository;

    @MockBean
    private MobileOTPService mobileOTPService;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ReferralCodeRepo referralCodeRepo;

    @Autowired
    private ReferralWalletRepository referralWalletRepo;

    @Autowired
    private ReferralSettingsRepo referralSettingsRepo;

    @Autowired
    private UserService userService;

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_Settings.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void createReferralOrganizationSettings(){
        JSONObject body = new JSONObject();
        body.put("name", "Kasbeny We Eksab");
        body.put("constraints", getReferralCodeTypeReferralConstraintsMap());

        HttpEntity<?> request = getHttpEntity(body.toString(),"131415");

        ResponseEntity<String> res = template.postForEntity("/referral/organization/settings",
                request, String.class);
        assertEquals(200, res.getStatusCodeValue());

        ReferralSettings referralSettings = referralSettingsRepo.findByOrganization_Id(99001L).get();
        assertNotNull(referralSettings);
    }

    private static Map<ReferralCodeType, ReferralConstraints> getReferralCodeTypeReferralConstraintsMap() {
        Map<ReferralCodeType, ReferralConstraints> constraints = new HashMap<>();
        constraints.put(ReferralCodeType.REFERRAL_ACCEPT_REVENUE,
                ReferralConstraints.builder()
                        .value(new BigDecimal("5.0"))
                        .validFrom(LocalDate.now())
                        .validTo(LocalDate.now().plusDays(5))
                        .build()
    );
        constraints.put(ReferralCodeType.ORDER_DISCOUNT_PERCENTAGE,
                ReferralConstraints.builder()
                        .value(new BigDecimal("0.04"))
                        .validFrom(LocalDate.now())
                        .validTo(LocalDate.now().plusDays(5))
                        .build());
        constraints.put(ReferralCodeType.SHARE_REVENUE_PERCENTAGE,
                ReferralConstraints.builder()
                        .value(new BigDecimal("0.04"))
                        .validFrom(LocalDate.now())
                        .validTo(LocalDate.now().plusDays(5))
                        .build());
        constraints.put(ReferralCodeType.PARENT_REGISTRATION,
                ReferralConstraints.builder()
                        .value(new BigDecimal("0.0"))
                        .validFrom(LocalDate.now())
                        .validTo(LocalDate.now().plusDays(5))
                        .build());
        constraints.put(ReferralCodeType.CHILD_REGISTRATION,
                ReferralConstraints.builder()
                        .value(new BigDecimal("0.0"))
                        .validFrom(LocalDate.now())
                        .validTo(LocalDate.now().plusDays(5))
                        .build());
        return constraints;
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_share_revenue.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void shareRevenueForOrderWhenOrderStatusUpdatedToDelivered(){
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

        ReferralWallet referralWallet = referralWalletService.getWalletByUserId(88L);
        assertEquals(new BigDecimal("27.20"), referralWallet.getBalance());

        List<ReferralTransactions> referralTransactions = referralTransactionRepository.findByReferralWallet_Id(500L);
        assertEquals(1, referralTransactions.size());
        assertEquals(ReferralTransactionsType.ORDER_SHARE_REVENUE, referralTransactions.get(0).getType());
        assertEquals(new BigDecimal("7.20"), referralTransactions.get(0).getAmount());
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_share_revenue_1.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void shareRevenueForOrderWhenOrderStatusUpdatedToDeliveredNotSharedBecauseDateIntervalOut(){
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

        ReferralWallet referralWallet = referralWalletService.getWalletByUserId(88L);
        assertEquals(new BigDecimal("20.00"), referralWallet.getBalance());

        List<ReferralTransactions> referralTransactions = referralTransactionRepository.findByReferralWallet_Id(500L);
        assertEquals(0, referralTransactions.size());
    }

    @Test
    @Transactional
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void saveOrderDiscountTransaction(){
        Long orderId = 330033L;
        OrdersEntity ordersEntity = ordersRepository.findById(orderId).get();

        referralCodeService.saveReferralTransactionForOrderDiscount(ordersEntity);

        List<ReferralTransactions> referralTransactions = referralTransactionRepository.findByOrderId(orderId);
        assertEquals(1, referralTransactions.size());
        assertEquals(ReferralTransactionsType.ORDER_DISCOUNT, referralTransactions.get(0).getType());
        assertEquals(null, referralTransactions.get(0).getReferralWallet());
        assertEquals(new BigDecimal("6.00"), referralTransactions.get(0).getAmount());
    }

    @Test
    @Transactional
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_8.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void saveOrderDiscountTransactionNotAddedBecauseOutOfDateRange(){
        Long orderId = 330033L;
        OrdersEntity ordersEntity = ordersRepository.findById(orderId).get();

        referralCodeService.saveReferralTransactionForOrderDiscount(ordersEntity);

        List<ReferralTransactions> referralTransactions = referralTransactionRepository.findByOrderId(orderId);
        assertEquals(0, referralTransactions.size());
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_1.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void sendOtpAndReferralCreatedButNotActive(){
        when(mobileOTPService.send(any(OTPDto.class))).thenReturn("Success");

        HttpEntity<?> request = getHttpEntity("456");

        ResponseEntity<String> res = template.postForEntity("/referral/sendOtp/?phoneNumber=01234567891&parentReferralCode=abcdfg",
                 request, String.class);
        assertEquals(200, res.getStatusCodeValue());

        ReferralCodeEntity referralCodeEntity = referralCodeRepo.findByUser_IdAndOrganization_Id(89L, 99001L).get();

        assertNotNull(referralCodeEntity.getAcceptReferralToken());
        assertNotNull(referralCodeEntity.getParentReferralCode());
        assertNotNull(referralCodeEntity.getReferralCode());
        assertEquals(Integer.valueOf(ReferralCodeStatus.IN_ACTIVE.getValue()), referralCodeEntity.getStatus());
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_Parent_7.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void sendOtpForParentRegistrationButOutOfDateRange(){
        when(mobileOTPService.send(any(OTPDto.class))).thenReturn("Success");

        HttpEntity<?> request = getHttpEntity("456");

        ResponseEntity<String> res = template.postForEntity("/referral/sendOtp/?phoneNumber=01234567891",
                request, String.class);

        assertEquals(406, res.getStatusCodeValue());

        JSONObject jsonObject = new JSONObject(res.getBody());
        String message = jsonObject.getString("message");
        assertEquals("Parent Registration ended!", message);
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_Parent_7.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void sendOtpForChildRegistrationButOutOfDateRange(){
        when(mobileOTPService.send(any(OTPDto.class))).thenReturn("Success");

        HttpEntity<?> request = getHttpEntity("456");

        ResponseEntity<String> res = template.postForEntity("/referral/sendOtp/?phoneNumber=01234567891&parentReferralCode=abcdfg",
                request, String.class);

        assertEquals(406, res.getStatusCodeValue());

        JSONObject jsonObject = new JSONObject(res.getBody());
        String message = jsonObject.getString("message");
        assertEquals("Referral Registration ended!", message);
    }


    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_2.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void sendOtpFailForAlreadyExistReferralCodeForUser(){
        when(mobileOTPService.send(any(OTPDto.class))).thenReturn("Success");

        HttpEntity<?> request = getHttpEntity("456");

        ResponseEntity<String> res = template.postForEntity("/referral/sendOtp/?phoneNumber=01234567891&parentReferralCode=abcdfg",
                request, String.class);
        assertEquals(406, res.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(res.getBody());
        String message = jsonObject.getString("message");
        assertEquals("User Already has Referral Code!", message);
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_Edit_Phone.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void sendOtpWithAnotherPhoneForAlreadyExistNotValidatedReferralCode(){
        when(mobileOTPService.send(any(OTPDto.class))).thenReturn("Success");

        HttpEntity<?> request = getHttpEntity("456");

        ResponseEntity<String> res = template.postForEntity("/referral/sendOtp/?phoneNumber=01234567889&parentReferralCode=abcdfg",
                request, String.class);

        assertEquals(200, res.getStatusCodeValue());

        ReferralCodeEntity referralCodeEntity = referralCodeRepo.findByUser_IdAndOrganization_Id(89L, 99001L).get();

        assertEquals("01234567889", referralCodeEntity.getPhoneNumber());
        assertNotNull(referralCodeEntity.getAcceptReferralToken());
        assertNotNull(referralCodeEntity.getParentReferralCode());
        assertNotNull(referralCodeEntity.getReferralCode());
        assertEquals(Integer.valueOf(ReferralCodeStatus.IN_ACTIVE.getValue()), referralCodeEntity.getStatus());
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_1.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void sendOtpFailForNotExistParentReferralCode(){
        when(mobileOTPService.send(any(OTPDto.class))).thenReturn("Success");

        HttpEntity<?> request = getHttpEntity("456");

        ResponseEntity<String> res = template.postForEntity("/referral/sendOtp/?phoneNumber=01234567891&parentReferralCode=qweasd",
                request, String.class);
        assertEquals(406, res.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(res.getBody());
        String message = jsonObject.getString("message");
        assertEquals("Couldn't find referral code [qweasd] for user!", message);
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_2.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void validateOTPAndReferralCodeActivatedAndWalletForUserWithAwardCreated(){
        when(mobileOTPService.send(any(OTPDto.class))).thenReturn("Success");

        HttpEntity<?> request = getHttpEntity("456");

        ResponseEntity<String> res = template.postForEntity("/referral/validateOtp/rtyuiu",
                request, String.class);
        assertEquals(200, res.getStatusCodeValue());

        ReferralCodeEntity referralCodeEntity = referralCodeRepo.findByUser_IdAndOrganization_Id(89L, 99001L).get();
        assertEquals(Integer.valueOf(ReferralCodeStatus.ACTIVE.getValue()), referralCodeEntity.getStatus());

        ReferralWallet referralWallet = referralWalletRepo.findByUserId(89L).get();
        assertNotNull(referralWallet);
        assertEquals(new BigDecimal("20.00"), referralWallet.getBalance());

        List<ReferralTransactions> referralTransactions = referralTransactionRepository.findByReferralWallet_Id(referralWallet.getId());
        assertEquals(1, referralTransactions.size());
        assertEquals(ReferralTransactionsType.ACCEPT_REFERRAL_CODE, referralTransactions.get(0).getType());

        BaseUserEntity user = userService.getUserById(89L);
        assertEquals(referralCodeEntity.getPhoneNumber(), user.getPhoneNumber());
    }


    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_5.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void validateOTPAndReferralCodeFailNotExistOtp(){
        when(mobileOTPService.send(any(OTPDto.class))).thenReturn("Success");

        HttpEntity<?> request = getHttpEntity("456");

        ResponseEntity<String> res = template.postForEntity("/referral/validateOtp/qweasd",
                request, String.class);
        assertEquals(404, res.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(res.getBody());
        String message = jsonObject.getString("message");
        assertEquals("There is no referral code for user to validate!", message);
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_5.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void validateOTPFailNotEqual(){
        when(mobileOTPService.send(any(OTPDto.class))).thenReturn("Success");

        HttpEntity<?> request = getHttpEntity("123");

        ResponseEntity<String> res = template.postForEntity("/referral/validateOtp/qweasd",
                request, String.class);
        assertEquals(406, res.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(res.getBody());
        String message = jsonObject.getString("message");
        assertEquals("referral accept token not valid!", message);
    }


    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_3.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void getReferralForLoggedInUser() {
        HttpEntity<?> request = getHttpEntity("456");

        ResponseEntity<ReferralCodeDto> res = template.exchange("/referral/user", HttpMethod.GET, request,
                ReferralCodeDto.class);

        assertEquals(200, res.getStatusCodeValue());
        assertEquals("asdfgh", res.getBody().getReferralCode() );
        assertEquals("abcdfg", res.getBody().getParentReferralCode());
        assertEquals("user2", res.getBody().getUsername());
        assertEquals(ReferralCodeStatus.ACTIVE, res.getBody().getStatus());

    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_3.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void getOrderDiscountValue() {
        HttpEntity<?> request = getHttpEntity("123");

        ResponseEntity<Map<ReferralCodeType, BigDecimal>> res = template.exchange("/referral/settings/discount_percentage", HttpMethod.GET, request,
                new ParameterizedTypeReference<>(){});

        assertEquals(200, res.getStatusCodeValue());
        assertEquals(new BigDecimal("3"), res.getBody().get(ReferralCodeType.ORDER_DISCOUNT_PERCENTAGE) );
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_3.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void getOrderDiscountValueNotFoundForOrganization() {
        HttpEntity<?> request = getHttpEntity("789");

        ResponseEntity<String> res = template.exchange("/referral/settings/discount_percentage", HttpMethod.GET, request,
              String.class);

        assertEquals(404, res.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(res.getBody());
        String message = jsonObject.getString("message");
        assertEquals("There is no settings for this organization!", message);
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_4.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void activateReferralCode() {
        HttpEntity<?> request = getHttpEntity("161718");

        ResponseEntity<ReferralCodeDto> res = template.postForEntity("/referral/activate/abcdfg", request,
                ReferralCodeDto.class);
        assertEquals(200, res.getStatusCodeValue());

        ReferralCodeEntity referralCodeEntity = referralCodeRepo.findByReferralCode("abcdfg").get();
        assertNotNull(referralCodeEntity);
        assertEquals(Integer.valueOf(ReferralCodeStatus.ACTIVE.getValue()), referralCodeEntity.getStatus());

    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_4.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void deActivateReferralCode() {
        HttpEntity<?> request = getHttpEntity("161718");

        ResponseEntity<ReferralCodeDto> res = template.postForEntity("/referral/deactivate/asdfgh", request,
                ReferralCodeDto.class);

        assertEquals(200, res.getStatusCodeValue());

        ReferralCodeEntity referralCodeEntity = referralCodeRepo.findByReferralCode("asdfgh").get();
        assertNotNull(referralCodeEntity);
        assertEquals(Integer.valueOf(ReferralCodeStatus.IN_ACTIVE.getValue()), referralCodeEntity.getStatus());
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_Stats.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void getReferralStatsForUser() {
        HttpEntity<?> request = getHttpEntity("123");

        ResponseEntity<ReferralStatsDto> res = template.exchange("/referral/stats", HttpMethod.GET, request,
                ReferralStatsDto.class);

        assertEquals(200, res.getStatusCodeValue());
        assertTrue(2L == res.getBody().getNumberOfActiveChildReferrals());
        assertEquals(new BigDecimal("180.00"), res.getBody().getShareRevenueEarningsFromChildReferrals());
        assertEquals(new BigDecimal("96.00"), res.getBody().getOrderDiscountsAwarded());
        assertEquals(new BigDecimal("200.00"), res.getBody().getWalletBalance());
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_Stats.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void getChildsAcceptReferrals(){
        HttpEntity<?> request = getHttpEntity("123");

        ResponseEntity<PaginatedResponse<ReferralTransactionsDto>> res = template.exchange(
                "/referral/childs?type=ACCEPT_REFERRAL_CODE&pageSize=10&pageNo=0",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<PaginatedResponse<ReferralTransactionsDto>>(){}
        );

        assertEquals(200, res.getStatusCodeValue());
        assertEquals(Long.valueOf(2L), res.getBody().getTotalRecords());
        assertEquals(2, res.getBody().getContent().size());
        assertEquals(Integer.valueOf(1), res.getBody().getTotalPages());

    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_Stats.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void getChildsSharedRevenue(){
        HttpEntity<?> request = getHttpEntity("123");

        ResponseEntity<PaginatedResponse<ReferralTransactionsDto>> res = template.exchange(
                "/referral/childs?type=ORDER_SHARE_REVENUE&pageSize=10&pageNo=0",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<PaginatedResponse<ReferralTransactionsDto>>(){}
        );

        assertEquals(200, res.getStatusCodeValue());
        assertEquals(Long.valueOf(2L) , res.getBody().getTotalRecords());
        assertEquals(2 , res.getBody().getContent().size());
        assertEquals(Integer.valueOf(1) , res.getBody().getTotalPages());

    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_Stats.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void getChildsAcceptReferralsWithDate(){
        HttpEntity<?> request = getHttpEntity("123");
        String dateFrom = LocalDate.now().plusDays(2).toString();
        String dateTo = LocalDate.now().plusDays(2).toString();

        ResponseEntity<PaginatedResponse<ReferralTransactionsDto>> res = template.exchange(
                "/referral/childs?type=ACCEPT_REFERRAL_CODE&pageSize=10&pageNo=0&dateFrom=" + dateFrom + "&dateTo=" + dateTo,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<PaginatedResponse<ReferralTransactionsDto>>(){}
        );

        assertEquals(200, res.getStatusCodeValue());
        assertEquals(Long.valueOf(1L), res.getBody().getTotalRecords());
        assertEquals(1, res.getBody().getContent().size());
        assertEquals(Integer.valueOf(1), res.getBody().getTotalPages());

    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data_Stats.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void getChildsSharedRevenueWithDate(){
        HttpEntity<?> request = getHttpEntity("123");
        String dateFrom = LocalDate.now().plusDays(2).toString();
        String dateTo = LocalDate.now().plusDays(2).toString();

        ResponseEntity<PaginatedResponse<ReferralTransactionsDto>> res = template.exchange(
                "/referral/childs?type=ORDER_SHARE_REVENUE&pageSize=10&pageNo=0&dateFrom=" + dateFrom + "&dateTo=" + dateTo,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<PaginatedResponse<ReferralTransactionsDto>>(){}
        );

        assertEquals(200, res.getStatusCodeValue());
        assertEquals(Long.valueOf(1L), res.getBody().getTotalRecords());
        assertEquals(1, res.getBody().getContent().size());
        assertEquals(Integer.valueOf(1), res.getBody().getTotalPages());

    }
}
