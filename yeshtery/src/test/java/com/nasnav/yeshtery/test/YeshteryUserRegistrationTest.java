package com.nasnav.yeshtery.test;

import com.nasnav.AppConfig;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.*;
import com.nasnav.dao.yeshtery.YeshteryUserOtpRepository;
import com.nasnav.dao.yeshtery.YeshteryUserRepository;
import com.nasnav.dto.request.ActivateOtpDto;
import com.nasnav.persistence.*;
import com.nasnav.persistence.yeshtery.YeshteryUserEntity;
import com.nasnav.response.RecoveryUserResponse;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.AdminService;
import com.nasnav.service.MailService;
import com.nasnav.service.UserService;
import com.nasnav.service.otp.OtpType;
import com.nasnav.yeshtery.test.commons.TestCommons;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.PreDestroy;
import javax.mail.MessagingException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.nasnav.constatnts.EmailConstants.ACTIVATION_ACCOUNT_EMAIL_SUBJECT;
import static com.nasnav.enumerations.UserStatus.ACTIVATED;
import static com.nasnav.enumerations.UserStatus.NOT_ACTIVATED;
import static com.nasnav.response.ResponseStatus.EMAIL_EXISTS;
import static com.nasnav.response.ResponseStatus.INVALID_PARAMETERS;
import static com.nasnav.commons.YeshteryConstants.API_PATH;
import static com.nasnav.yeshtery.test.commons.TestCommons.*;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;


@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/User_Test_Data.sql"})
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"}) //FIXME temporarly
public class YeshteryUserRegistrationTest extends AbstractTestWithTempBaseDir {

    private static final String YESHTERY_SUSPEND_API_PATH = API_PATH + "/user/suspend";
    private final String YESHTERY_LOGIN_API_PATH = API_PATH + "/user/login";
    private final String YESHTERY_SUBSCRIBE_API_PATH = API_PATH + "/user/subscribe";
    private final String YESHTERY_RECOVER_API_PATH = API_PATH + "/user/recover";

    private UserEntity persistentUser;
    private OrganizationEntity organization;

    
	@MockBean
	private MailService mailService;

    @Autowired
    private TestRestTemplate template;
    @Autowired
    private UserService userService;
    @Autowired
    private YeshteryUserOtpRepository yeshteryUserOtpRepository;
    @Autowired
    private UserOtpRepository userOtpRepository;
    @Autowired
    private YeshteryUserRepository yeshteryUserRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmployeeUserRepository employeeUserRepo;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private UserTokenRepository userTokenRepo;
    @Autowired
    private UserSubscriptionRepository subsRepo;
    @Autowired
    private AdminService adminService;
    @Autowired
    private AppConfig config;

    @Before
    public void clearCache() {
        adminService.invalidateCaches();
    }


    @Before
    public void setupLoginUser() {
        if (organization == null) {
            organization = organizationRepository.findOneById(99001L);
        }

        persistentUser = userRepository.getByEmailAndOrganizationId("unavailable@nasnav.com", organization.getId());
        if (persistentUser == null) {
            persistentUser = createUser();
            persistentUser = userRepository.save(persistentUser);
        }

    }

    private UserEntity createUser() {
        UserEntity persistentUser = new UserEntity();
        persistentUser.setName("John Smith");
        persistentUser.setEmail("unavailable@nasnav.com");
        persistentUser.setEncryptedPassword("---");
        persistentUser.setOrganizationId(organization.getId());
        persistentUser.setUserStatus(ACTIVATED.getValue());

        return persistentUser;
    }

    @PreDestroy
    public void removeLoginUser() {
        if (persistentUser != null) {
            userRepository.delete(persistentUser);
        }

        if (organization != null) {
            organizationRepository.delete(organization);
        }
    }

    public void testSameEmailAndOrgId() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 5}", null);
        ResponseEntity<UserApiResponse> response = template.postForEntity(API_PATH + "/user/register", userJson,
                UserApiResponse.class);
        // get userId for deletion after test
        Long userId = response.getBody().getEntityId();

        // try to re register with the same email and org_id
        response = template.postForEntity(API_PATH + "/user/register", userJson, UserApiResponse.class);

        // response status should contain EMAIL_EXISTS
        System.out.println(response.getBody());
        Assert.assertTrue(response.getBody().getStatus().contains(EMAIL_EXISTS));
        Assert.assertEquals(406, response.getStatusCode().value());
        // Delete this user
        userService.deleteUser(userId);
    }

    @Test
    public void testRegisterSuccess() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\"activation_method\":\"OTP\",\"name\":\"Ahmed\",\"email\":\"new_email@nasnav.com\",\"password\":\"123456\",\"confirmation_flag\":true,\"org_id\":"
                        + organization.getId() + ",\"redirect_url\":\"https://www.tooawsome.com/activate\"}",
                null);
        ResponseEntity<UserApiResponse> response = template.postForEntity(API_PATH + "/user/register", userJson,
                UserApiResponse.class);
        // get userId for deletion after test
        Long userId = response.getBody().getEntityId();

        System.out.println(response.getBody());
        Assert.assertEquals(201, response.getStatusCode().value());

        Set<Long> yeshteryOrgIds = organizationRepository.findByYeshteryState(1)
                .stream()
                .map(OrganizationEntity::getId)
                .collect(Collectors.toSet());
        Set<Long> userOrgIds = userRepository.findByYeshteryUserId(userId)
                .stream()
                .map(UserEntity::getOrganizationId)
                .collect(Collectors.toSet());
        assertEquals(yeshteryOrgIds, userOrgIds);
    }

    @Test
    public void testRegisterWithoutActivationMethod() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\"name\":\"Ahmed\",\"email\":\"new_email@nasnav.com\",\"password\":\"123456\",\"confirmation_flag\":true,\"org_id\":"
                        + organization.getId() + ",\"redirect_url\":\"https://www.tooawsome.com/activate\"}",
                null);
        ResponseEntity<UserApiResponse> response = template.postForEntity(API_PATH + "/user/register", userJson,
                UserApiResponse.class);

        Assert.assertEquals(CREATED, response.getStatusCode());
        UserEntity createdUser = userRepository.getByEmailAndOrganizationId("new_email@nasnav.com", organization.getId());
        assertNotNull(createdUser);
    }

    @Test
    public void testRegisterWithoutActivationMethodAndNoRedirectUrl() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\"name\":\"Ahmed\",\"email\":\"new_email@nasnav.com\",\"password\":\"123456\",\"confirmation_flag\":true,\"org_id\":"
                        + organization.getId() + "}",
                null);
        ResponseEntity<UserApiResponse> response = template.postForEntity(API_PATH + "/user/register", userJson,
                UserApiResponse.class);

        Assert.assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
        UserEntity createdUser = userRepository.getByEmailAndOrganizationId("new_email@nasnav.com", organization.getId());
        assertNull(createdUser);
    }

    @Test
    public void testRegisterWithoutConfirmationFlag() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\"activation_method\":\"OTP\",\"name\":\"Ahmed\",\"email\":\"new_email@nasnav.com\",\"password\":\"123456\",\"org_id\":"
                        + organization.getId() + ",\"redirect_url\":\"https://www.tooawsome.com/activate\"}",
                null);
        ResponseEntity<UserApiResponse> response = template.postForEntity(API_PATH + "/user/register", userJson,
                UserApiResponse.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    private JSONObject createUserRegisterRequest(String redirectUrl) {
        return json()
                .put("name", "Ahmad")
                .put("email", "test@nasnav.com")
                .put("password", "password")
                .put("org_id", 99001)
                .put("confirmation_flag", true)
                .put("redirect_url", redirectUrl);
    }

	private JSONObject registerWithOtpAndAssert() throws MessagingException, IOException {
		JSONObject jsonBody = createUserRegisterRequest(null).put("activation_method", "OTP");
		String body = jsonBody.toString();
		HttpEntity<Object> request = getHttpEntity((Object)body);
		ResponseEntity<String> response = template.postForEntity(API_PATH + "/user/register", request, String.class);

		Assert.assertEquals(201, response.getStatusCodeValue());
		Mockito
			.verify(mailService)
			.send(
				  Mockito.eq("organization_1")
				, Mockito.eq("test@nasnav.com")
				, Mockito.eq("organization_1"+ACTIVATION_ACCOUNT_EMAIL_SUBJECT)
				, Mockito.anyString()
				, Mockito.anyMap());
		return jsonBody;
	}

	@Test
	public void newUserRegisterWithOtpTest() throws MessagingException, IOException {
		JSONObject userJson = registerWithOtpAndAssert();
		String email = userJson.getString("email");
		Long orgId = userJson.getLong("org_id");
		YeshteryUserEntity newUser = yeshteryUserRepository.getByEmailIgnoreCaseAndOrganizationId(email, orgId);
		String otp = yeshteryUserOtpRepository.findByUserAndType(newUser, OtpType.REGISTER).map(BaseUserOtpEntity::getOtp).orElse(null);
		ActivateOtpDto activationBody = new ActivateOtpDto(email, otp, orgId);
		HttpEntity<ActivateOtpDto> request = new HttpEntity<ActivateOtpDto>(activationBody);
		ResponseEntity<String> response = template.postForEntity(API_PATH + "/user/register/otp/activate", request, String.class);
		Assert.assertEquals(200, response.getStatusCodeValue());
		// otp is deleted request should fail
		response = template.postForEntity(API_PATH + "/user/register/otp/activate", request, String.class);
		Assert.assertEquals(400, response.getStatusCodeValue());
	}

    @Test
    public void testChangePassword() throws MessagingException, IOException{
        String body =
                json()
                        .put("current_password", "PaSSworD")
                        .put("new_password", "newPassword")
                        .put("confirm_password", "newPassword")
                        .toString();
        HttpEntity<Object> userJson = getHttpEntity(body, "123");
        ResponseEntity<String> response = template.postForEntity(API_PATH +"/user/change/password", userJson, String.class);
        assertEquals(200, response.getStatusCode().value());

    }
	@Test
	public void newUserRegisterWithInvalidOtpTest() throws MessagingException, IOException {
		JSONObject userJson = registerWithOtpAndAssert();
		String email = userJson.getString("email");
		Long orgId = userJson.getLong("org_id");
		YeshteryUserEntity newUser = yeshteryUserRepository.getByEmailIgnoreCaseAndOrganizationId(email, orgId);
		String otp = yeshteryUserOtpRepository.findByUserAndType(newUser, OtpType.REGISTER).map(BaseUserOtpEntity::getOtp).orElse(null);
		ActivateOtpDto activationBody = new ActivateOtpDto(email, "invalid otp", orgId);
		HttpEntity<ActivateOtpDto> request = new HttpEntity<ActivateOtpDto>(activationBody);
		ResponseEntity<String> response;

		for (int i = 0; i < config.otpMaxRetries; i++) {
			response = template.postForEntity(API_PATH + "/user/register/otp/activate", request, String.class);
			Assert.assertEquals(400, response.getStatusCodeValue());
		}

		// next request will fail as otp is deleted after max retries
		activationBody = new ActivateOtpDto("test@nasnav.com", otp, 99001L);
		request = new HttpEntity<ActivateOtpDto>(activationBody);
		response = template.postForEntity(API_PATH + "/user/register/otp/activate", request, String.class);
		Assert.assertEquals(400, response.getStatusCodeValue());
	}

    private void assertResetTocken(String token) {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" + "\t\"token\":\"" + token + "\",\n" + "\t\"password\":\"NewPassword\"\n" + "}", null);

        ResponseEntity<UserApiResponse> response = template.postForEntity(API_PATH + "/user/recover", userJson,
                UserApiResponse.class);

        
        Assert.assertEquals(OK.value(), response.getStatusCode().value());

        userJson = getHttpEntity(
                "{\"password\":\"" + "NewPassword" + "\"," + "\"email\":\""
                        + persistentUser.getEmail() + "\", \"org_id\": " +  organization.getId() + " }", null);

        response = template.postForEntity(API_PATH + "/user/login", userJson, UserApiResponse.class);

        // Delete this user
        
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testPasswordResetWithOtp() {

        getResponseFromGet(API_PATH + "/user/recover?email=" + persistentUser.getEmail() + "&org_id=" + organization.getId()
                + "&employee=false&activation_method=OTP", UserApiResponse.class);
        String otp = userOtpRepository.findByUserAndType(persistentUser, OtpType.RESET_PASSWORD).map(BaseUserOtpEntity::getOtp).orElse(null);
        ActivateOtpDto activationBody = new ActivateOtpDto(persistentUser.getEmail(), otp, persistentUser.getOrganizationId());
        HttpEntity<ActivateOtpDto> request = new HttpEntity<ActivateOtpDto>(activationBody);
        
        ResponseEntity<RecoveryUserResponse> recoveryResponse = template.postForEntity(API_PATH + "/user/recovery/otp-verify", request, RecoveryUserResponse.class);
        Assert.assertEquals(200, recoveryResponse.getStatusCodeValue());
        String token = recoveryResponse.getBody().getResetToken();

        assertResetTocken(token);
        recoveryResponse = template.postForEntity(API_PATH + "/user/recovery/otp-verify", request, RecoveryUserResponse.class);
        Assert.assertEquals(400, recoveryResponse.getStatusCodeValue());
    }

    @Test
    public void testPasswordResetWithInvalidOtp() {

        getResponseFromGet("/user/recover?email=" + persistentUser.getEmail() + "&org_id=" + organization.getId()
                + "&employee=false&activation_method=OTP", UserApiResponse.class);
        String otp = userOtpRepository.findByUserAndType(persistentUser, OtpType.RESET_PASSWORD).map(BaseUserOtpEntity::getOtp).orElse(null);
        ActivateOtpDto activationBody = new ActivateOtpDto(persistentUser.getEmail(), "invalid otp", persistentUser.getOrganizationId());
        HttpEntity<ActivateOtpDto> request = new HttpEntity<ActivateOtpDto>(activationBody);
        
        ResponseEntity<RecoveryUserResponse> recoveryResponse = template.postForEntity(API_PATH + "/user/recovery/otp-verify", request, RecoveryUserResponse.class);
        Assert.assertEquals(400, recoveryResponse.getStatusCodeValue());

        for (int i = 0; i < config.otpMaxRetries; i++) {
            recoveryResponse = template.postForEntity(API_PATH + "/user/recovery/otp-verify", request, RecoveryUserResponse.class);
            Assert.assertEquals(400, recoveryResponse.getStatusCodeValue());
        }

        activationBody = new ActivateOtpDto(persistentUser.getEmail(), otp, persistentUser.getOrganizationId());
        request = new HttpEntity<ActivateOtpDto>(activationBody);
        // otp is now deleted after max retries
        recoveryResponse = template.postForEntity(API_PATH + "/user/recovery/otp-verify", request, RecoveryUserResponse.class);
        Assert.assertEquals(400, recoveryResponse.getStatusCodeValue());
    }

    @Test
    public void testSendResetPasswordTokenForInvalidMail() {
        ResponseEntity<String> response = getResponseFromGet(API_PATH + "/user/recover?email=foo&org_id=" +
                organization.getId() + "&employee=false", String.class);
        System.out.println("###############" + response.getBody());
        Assert.assertTrue(response.getBody().contains("U$EMP$0004"));

        Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
    }

    @Test
    public void testSendResetPasswordTokenForValidButFakeMail() {
        ResponseEntity<String> response = getResponseFromGet(API_PATH + "/user/recover?email=foo@foo.foo&org_id=" +
                organization.getId() + "&employee=false", String.class);
        Assert.assertTrue(response.getBody().contains("UXACTVX0001"));

        Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
    }

    @Test
    public void testSendResetPasswordTokenForNoPassedMail() {
        ResponseEntity<String> response = getResponseFromGet(API_PATH + "/user/recover?email=&org_id=12&employee=false", String.class);
        Assert.assertTrue(response.getBody().contains("U$EMP$0004"));

        Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
    }

    @Test
    public void testPasswordShouldBeReset() {

        persistentUser.setResetPasswordToken("ABCX");
        userService.update(persistentUser);

        getResponseFromGet(API_PATH + "/user/recover?email=" + persistentUser.getEmail() + "&org_id=" + organization.getId()
                + "&employee=false", UserApiResponse.class);
        // refresh the entity
        persistentUser = userRepository.findById((Long) persistentUser.getId()).get();
        String token = persistentUser.getResetPasswordToken();
        Assert.assertNotEquals("ABCX", token);

        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" + "\t\"token\":\"" + token + "\",\n" + "\t\"password\":\"NewPassword\"\n" + "}", null);

        ResponseEntity<UserApiResponse> response = template.postForEntity(API_PATH + "/user/recover", userJson,
                UserApiResponse.class);


        Assert.assertEquals(OK.value(), response.getStatusCode().value());

        userJson = getHttpEntity(
                "{\"password\":\"" + "NewPassword" + "\"," + "\"email\":\""
                        + persistentUser.getEmail() + "\", \"org_id\": " + organization.getId() + " }", null);

        response = template.postForEntity(API_PATH + "/user/login", userJson, UserApiResponse.class);

        // Delete this user

        Assert.assertEquals(200, response.getStatusCode().value());

    }

    @Test
    public void testInvalidJsonForPasswordRecovery() {

        getResponseFromGet("/user/recover?email=" + persistentUser.getEmail() + "&org_id=" + organization.getId(), UserApiResponse.class);

        HttpEntity<Object> userJson = getHttpEntity("{\t\n" + "\t\"token\": \"QWER\",, \"password\"}", null);

        ResponseEntity<UserApiResponse> response = template.postForEntity(API_PATH + "/user/recover", userJson,
                UserApiResponse.class);


        Assert.assertTrue(response.getBody().getStatus().contains(INVALID_PARAMETERS));
        Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
    }

    @Test
    public void testSetInvalidPassword() {

        getResponseFromGet(API_PATH + "/user/recover?email=" + persistentUser.getEmail() + "&org_id=" + organization.getId(), UserApiResponse.class);

        // refresh user
        persistentUser = (UserEntity) userService.getUserById((long) persistentUser.getId());
        HttpEntity<Object> userJson = getHttpEntity(
                "{\"token\":\"" + persistentUser.getResetPasswordToken() + "\"," + "\"password\":\"123\"}", null);

        ResponseEntity<String> response = template.postForEntity(API_PATH + "/user/recover", userJson, String.class);


        Assert.assertTrue(response.getBody().contains("U$LOG$0005"));
        Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
    }


    @Test
    public void testExpiredToken() throws IOException {
        persistentUser.setResetPasswordSentAt(LocalDateTime.now().minusHours(EntityConstants.TOKEN_VALIDITY + 1));
        String token = "ABC123XYZ";
        persistentUser.setResetPasswordToken(token);
        userService.update(persistentUser);

        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" + "\t\"employee\":false," + "\n\t\"token\":\"" + token + "\",\n" +
                        "\t\"password\":\"password\"\n" + "}", null);

        ResponseEntity<String> response = template.postForEntity(API_PATH + "/user/recover", userJson,
                String.class);

        Assert.assertTrue(response.getBody().contains("U$LOG$0006"));
        Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
    }

    @Test
    public void testUserShouldLogin() {
        //try to get new password to use it for login
        String newPassword = "New_Password";
        resetUserPassword(newPassword);

        String request = createLoginJson(newPassword);
        HttpEntity<Object> userJson = getHttpEntity(request, null);

        //---------------------------------------------------------------------
        ResponseEntity<UserApiResponse> response = template.postForEntity(API_PATH + "/user/login", userJson, UserApiResponse.class);

        Optional<String> login1Token = extractAuthTokenFromCookies(response);

        Assert.assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getHeaders().get("Set-Cookie").get(0) != null);
        assertTrue(login1Token.isPresent());

        //---------------------------------------------------------------------
        ResponseEntity<UserApiResponse> response2 = template.postForEntity(API_PATH + "/user/login", userJson, UserApiResponse.class);

        Optional<String> login2Token = extractAuthTokenFromCookies(response2);

        Assert.assertEquals(200, response2.getStatusCode().value());
        assertTrue(response2.getHeaders().get("Set-Cookie").get(0) != null);
        assertTrue(login2Token.isPresent());

        //---------------------------------------------------------------------
        assertNotEquals(login1Token.get(), login2Token.get());
    }

    @Test
    public void testUsingExpiredToken() {
        ResponseEntity<String> response = template.exchange("/admin/list_organizations", GET, getHttpEntity("", "889966"), String.class);
        Assert.assertEquals(401, response.getStatusCodeValue());
    }


    private String createLoginJson(String newPassword) {
        return json()
                .put("password", newPassword)
                .put("email", persistentUser.getEmail())
                .put("org_id", organization.getId())
                .toString();
    }


    @Test
    public void testUserLogout() {
        String token = "77";
        assertTrue(userTokenRepo.existsByToken(token));
        long userTokensCountBefore = userTokenRepo.countByUserEntity_Id(88005L);
        assertTrue("we assume the user has multiple tokens in the test", userTokensCountBefore > 1);

        //--------------------------------------------------
        HttpEntity<?> entity = getHttpEntity(token);
        ResponseEntity<UserApiResponse> response = template.postForEntity(API_PATH + "/user/logout", entity, UserApiResponse.class);
        long userTokensCountAfter = userTokenRepo.countByUserEntity_Id(88005L);

        //--------------------------------------------------
        Assert.assertEquals(200, response.getStatusCode().value());
        assertFalse(userTokenRepo.existsByToken(token));
        assertEquals("other tokens should remain intact", 1L, userTokensCountBefore - userTokensCountAfter);
        System.out.println(response.getHeaders().get("Set-Cookie").get(0));
    }

    @Ignore("Yeshtery module doesn't has product controller")
    @Test
    public void testUsingSemiExpiredToken() {
        String token = "875488";
        UserTokensEntity tokenEntityBefore = userTokenRepo.findByToken(token);
        assertNotNull(tokenEntityBefore);
        LocalDateTime tokenUpdateTimeBefore = tokenEntityBefore.getUpdateTime();

        ResponseEntity<String> response = template.exchange(API_PATH + "/product/images" + "?product_id=1234", GET, getHttpEntity("", token), String.class);
        Assert.assertEquals(NOT_ACCEPTABLE, response.getStatusCodeValue());

        UserTokensEntity tokenEntityAfter = userTokenRepo.findByToken(token);
        assertNotNull(tokenEntityAfter);
        LocalDateTime tokenUpdateTimeAfter = tokenEntityAfter.getUpdateTime();

        assertTrue("After using a token, and if it is nearly expired, its expiration should be renewed", tokenUpdateTimeAfter.isAfter(tokenUpdateTimeBefore));
    }

    private void resetUserPassword(String newPassword) {
        // send token to user
        getResponseFromGet(API_PATH + "/user/recover?email=" + persistentUser.getEmail() + "&employee=false&org_id=" + organization.getId()
                , UserApiResponse.class);

        // refresh the user entity
        persistentUser = (UserEntity) userService.getUserById((long) persistentUser.getId());
        // use token to change password
        String token = persistentUser.getResetPasswordToken();
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" + "\t\"token\":\"" + token + "\",\n" + "\t\"password\":\"New_Password\",\n" +
                        "\"employee\": false" + "}", null);
        System.out.println(userJson);
        template.postForEntity(API_PATH + "/user/recover", userJson, UserApiResponse.class);

        // login using the new password
        userJson = getHttpEntity(
                "{\"password\":\"" + "New_Password" + "\", \"email\":\"" + persistentUser.getEmail() + "\", \"org_id\": " +
                        organization.getId() + "}", null);
        ResponseEntity<UserApiResponse> response = template.postForEntity(API_PATH + "/user/login", userJson,
                UserApiResponse.class);


        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testUserShouldLoginUppercaseEmail() {
        //try to get new password to use it for login
        String newPassword = "New_Password";

        resetUserPassword(newPassword);

        // login using the new password and email with different character case
        String email = StringUtils.swapCase(persistentUser.getEmail());
        HttpEntity<Object> userJson = getHttpEntity(
                "{\"password\":\"" + newPassword + "\", \"email\":\"" + email + "\", \"org_id\": " + organization.getId() + " }", null);
        ResponseEntity<UserApiResponse> response = template.postForEntity(API_PATH + "/user/login", userJson,
                UserApiResponse.class);


        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testUserSameEmailDifferentOrgShouldLogin() {

        // login using the new password
        JSONObject request = new JSONObject();
        request.put("email", "user1@nasnav.com");
        request.put("password", "12345678");
        request.put("org_id", 99001L);

        ResponseEntity<UserApiResponse> response = template.postForEntity(API_PATH + "/user/login"
                , getHttpEntity(request.toString(), null)
                , UserApiResponse.class);


        Assert.assertEquals(200, response.getStatusCode().value());
    }


    @Test
    public void testInvalidCredentialsLogin() {

        HttpEntity<Object> userJson = getHttpEntity("{\t\n" + "\t\"password\":\"" + "Invalid_Password" + "\",\n"
                + "\t\"email\":\"" + persistentUser.getEmail() + "\", \"org_id\": " + organization.getId() + "}", null);

        ResponseEntity<String> response = template.postForEntity(API_PATH + "/user/login", userJson, String.class);

        Assert.assertTrue(response.getBody().contains("U$LOG$0002"));
        Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
    }

    @Test
    public void testInvalidOrgIdLogin() {
        //try to get new password to use it for login
        String newPassword = "New_Password";
        resetUserPassword(newPassword);

        // login using the new password
        HttpEntity<Object> userJson = getHttpEntity(
                "{\"password\":\"" + newPassword + "\", \"email\":\"" + persistentUser.getEmail() + "\", \"org_id\":null }", null);
        ResponseEntity<String> response = template.postForEntity(API_PATH + "/user/login", userJson, String.class);

        Assert.assertTrue(response.getBody().contains("U$LOG$0002"));
        Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
    }

    @Test
    public void testInvalidJsonForLogin() {
        // try to login using invalid json
        HttpEntity<Object> userJson = getHttpEntity("{\t\n" + "\t\"password" + "\t\"email\":\"\"\n" + "}", "");

        ResponseEntity<UserApiResponse> response = template.postForEntity(API_PATH + "/user/login", userJson,
                UserApiResponse.class);


        Assert.assertTrue(response.getBody().getStatus().contains(ResponseStatus.INVALID_PARAMETERS));
        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
    }


    private <T> ResponseEntity<T> getResponseFromGet(String URL, Class<T> classRef) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        return template.exchange(URL, HttpMethod.GET, new HttpEntity<>(headers), classRef);
    }

    @Test
    public void testCustomerLoginByEmailUsedByCustomerAndEmployee() {
        //try to get new password to use it for login
        String email = "user2@nasnav.com";
        String password = "12345678";

        String request = new JSONObject()
                .put("password", password)
                .put("email", email)
                .put("org_id", 99001L)
                .put("employee", false)
                .toString();

        // login using the new password
        HttpEntity<Object> userJson = getHttpEntity(request, "DOESNOT-NEED-TOKEN");
        ResponseEntity<UserApiResponse> response =
                template.postForEntity(API_PATH + "/user/login", userJson, UserApiResponse.class);

        //-------------------------------------------------------------------
        Assert.assertEquals(200, response.getStatusCode().value());

        String token = response.getBody().getToken();
        boolean userLoggedIn = userRepository.existsByAuthenticationToken(token);
        assertTrue("the logged in user should be the customer user, "
                + "and its token should exists in USERS table", userLoggedIn);
    }

    @Test
    public void updateSelfUserTestSuccess() {
        // update self data test success
        String body = "{\"name\":\"John Doe\"}";
        HttpEntity<Object> userJson = getHttpEntity(body, "123");
        ResponseEntity<UserApiResponse> response = template.postForEntity(API_PATH + "/user/update", userJson, UserApiResponse.class);

        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void updateSelfUserInvalidDataTest() {
        // update self data test success
        String body = "{\"name\":\"123\", \"email\":\"gds\"}";
        HttpEntity<Object> userJson = getHttpEntity(body, "123");
        ResponseEntity<String> response = template.postForEntity(API_PATH + "/user/update", userJson, String.class);

        System.out.println(response.toString());
        Assert.assertEquals(406, response.getStatusCodeValue());
    }

    @Test
    public void logoutUserTest() {
        Long userTokensCount = userTokenRepo.countByUserEntity_Id(88005L);
        assertEquals(4, userTokensCount.intValue());

        HttpEntity req = getHttpEntity("77");
        template.postForEntity(API_PATH + "/user/logout_all", req, UserApiResponse.class);

        Long userTokensCountAfter = userTokenRepo.countByUserEntity_Id(88005L);
        assertEquals(0, userTokensCountAfter.intValue());
    }

    @Test
    public void logoutEmployeeUserTest() {
        Long userTokensCount = userTokenRepo.countByEmployeeUserEntity_Id(159l);
        assertEquals(4, userTokensCount.intValue());

        HttpEntity req = getHttpEntity("101112");
        template.postForEntity(API_PATH + "/user/logout_all", req, UserApiResponse.class);

        Long userTokensCountAfter = userTokenRepo.countByEmployeeUserEntity_Id(159l);
        assertEquals(0, userTokensCountAfter.intValue());
    }

    @Ignore("Suspend not exist in Yeshtery module")
    @Test
    public void suspendUserInvalidAuthZ() {
        HttpEntity req = getHttpEntity("invalid token");
        ResponseEntity<String> res = template.postForEntity(API_PATH + "/user/suspend?user_id=88001&suspend=true", req, String.class);

        assertEquals(401, res.getStatusCodeValue());
    }

    @Ignore("Suspend not exist in Yeshtery module")
    @Test
    public void suspendUserInvalidAuthN() {
        HttpEntity req = getHttpEntity("192021");
        ResponseEntity<String> res = template.postForEntity(API_PATH + "/user/suspend?user_id=88001&suspend=true", req, String.class);

        assertEquals(403, res.getStatusCodeValue());
    }

    @Ignore("Suspend not exist in Yeshtery module")
    @Test
    public void suspendUserInAnotherOrg() {
        HttpEntity req = getHttpEntity("101112");
        ResponseEntity<String> res = template.postForEntity(API_PATH + "/user/suspend?user_id=88002&suspend=true", req, String.class);

        assertEquals(404, res.getStatusCodeValue());
    }

    @Ignore("Suspend not exist in Yeshtery module")
    @Test
    public void suspendUserTest() {
        HttpEntity req = getHttpEntity("101112");
        ResponseEntity<String> res = template.postForEntity(API_PATH + "/user/suspend?user_id=88001&suspend=true", req, String.class);

        assertEquals(200, res.getStatusCodeValue());
        UserEntity user = userRepository.findById(88001L).get();
        assertEquals(202, user.getUserStatus().intValue());

        Long userTokensCountAfter = userTokenRepo.countByUserEntity_Id(88001L);
        assertEquals(0, userTokensCountAfter.intValue());
    }

    @Ignore("Suspend not exist in Yeshtery module")
    @Test
    public void unsuspendUserTest() {
        HttpEntity req = getHttpEntity("101112");
        ResponseEntity<String> res = template.postForEntity(API_PATH + "/user/suspend?user_id=88006&suspend=false", req, String.class);

        assertEquals(200, res.getStatusCodeValue());
        UserEntity user = userRepository.findById(88006L).get();
        assertEquals(201, user.getUserStatus().intValue());
    }

    @Ignore("Suspend not exist in Yeshtery module")
    @Test
    public void suspendUserNotActivatedAccount() {
        HttpEntity req = getHttpEntity("101112");
        ResponseEntity<String> res = template.postForEntity(API_PATH + "/user/suspend?user_id=88004&suspend=true", req, String.class);

        assertEquals(406, res.getStatusCodeValue());
        UserEntity user = userRepository.findById(88004L).get();
        assertEquals(200, user.getUserStatus().intValue());
    }

    @Ignore("Suspend not exist in Yeshtery module")
    @Test
    public void unsuspendUserNotActivatedAccount() {
        HttpEntity req = getHttpEntity("101112");
        ResponseEntity<String> res = template.postForEntity(YESHTERY_SUSPEND_API_PATH + "?user_id=88004&suspend=false", req, String.class);

        assertEquals(NOT_ACCEPTABLE, res.getStatusCodeValue());
        UserEntity user = userRepository.findById(88004L).get();
        assertEquals(OK, user.getUserStatus().intValue());
    }


    @Test
    public void loginSuspendedUser() {
        String email = "suspended.man@nasnav.com";
        String password = "963";

        String request = new JSONObject()
                .put("password", password)
                .put("email", email)
                .put("org_id", 99001L)
                .put("employee", false)
                .toString();

        HttpEntity<Object> userJson = getHttpEntity(request, null);
        ResponseEntity<UserApiResponse> response =
                template.postForEntity(YESHTERY_LOGIN_API_PATH, userJson, UserApiResponse.class);
        assertEquals(LOCKED, response.getStatusCode());
    }


    @Test
    public void userSubscribeTest() {
        ResponseEntity<String> response =
                template.exchange(YESHTERY_SUBSCRIBE_API_PATH + "?email=email@nasnav.com&org_id=99001", HttpMethod.POST, null, String.class);
        assertEquals(200, response.getStatusCodeValue());
        UserSubscriptionEntity userSub = subsRepo.findByEmailAndOrganization_Id("email@nasnav.com", 99001L);
        assertNotNull(userSub.getToken());


        ResponseEntity<RedirectView> res =
                template.getForEntity(API_PATH + "/user/subscribe/activate?org_id=99001&token=" + userSub.getToken(), RedirectView.class);
        assertEquals(302, res.getStatusCodeValue());
        userSub = subsRepo.findByEmailAndOrganization_Id("email@nasnav.com", 99001L);
        assertTrue(Objects.isNull(userSub.getToken()));
    }

    @Test
    public void recoverEmployeeUser() {
        Long userId = 162L;
        EmployeeUserEntity user = employeeUserRepo.findById(userId).get();
        assertEquals(NOT_ACTIVATED.getValue(), user.getUserStatus());

        String body = json()
                .put("token", "d67438ac-f3a5-4939-9686-a1fc096f3f4e")
                .put("password", "password")
                .put("org_id", 99001)
                .put("employee", true)
                .toString();
        HttpEntity request = getHttpEntity(body, null);
        ResponseEntity<String> res = template.postForEntity(YESHTERY_RECOVER_API_PATH, request, String.class);
        assertEquals(200, res.getStatusCodeValue());
        user = employeeUserRepo.findById(userId).get();
        assertEquals(ACTIVATED.getValue(), user.getUserStatus());
    }
}
