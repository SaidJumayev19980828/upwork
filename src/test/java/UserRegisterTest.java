package com.nasnav.controller;

import com.nasnav.NavBox;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.ApiResponse;
import com.nasnav.response.ResponseStatus;
import com.nasnav.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.w3c.dom.Entity;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
public class UserRegisterTest {

    private MockMvc mockMvc;

    @Mock
    private UserController userController;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    UserService userService;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testUserShouldBeRegistered() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed\",\n" +
                        "\t\"email\":\"Foo.Bar@Foo.Bar.com\"\n" +
                        "}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);
        //Delete this user
        userService.deleteUser(response.getBody().getEntityId());
        Assert.assertTrue(response.getBody().isSuccess());
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testEmailExistence() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed\",\n" +
                        "\t\"email\":\"Foo.Bar@Foo.Bar.com\"\n" +
                        "}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);
        // get userId for deletion after test
        Long userId = response.getBody().getEntityId();

        //try to re register with the same email
        response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);
        Assert.assertFalse(response.getBody().isSuccess());
        // response status should contain INVALID_EMAIL
        Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.EMAIL_EXISTS));
        //Delete this user
        userService.deleteUser(userId);
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testInvalidEmailRegistration() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed\",\n" +
                        "\t\"email\":\"Foo.Bar.com\"\n" +
                        "}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);
        // success should be false
        Assert.assertFalse(response.getBody().isSuccess());
        // response status should contain INVALID_EMAIL
        Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_EMAIL));
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testInvalidNameRegistration() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed123\",\n" +
                        "\t\"email\":\"Foo.Bar@Foo.Bar.com\"\n" +
                        "}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);
        // success should be false
        Assert.assertFalse(response.getBody().isSuccess());
        // response status should contain INVALID_NAME
        Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_NAME));
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testInvalidJsonForUserRegisteration() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);
        // success should be false
        Assert.assertFalse(response.getBody().isSuccess());
        // response status should contain INVALID_NAME
        Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_NAME));
        Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_EMAIL));
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testSendResetPasswordTokenEmail() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed\",\n" +
                        "\t\"email\":\"ahmed.elbastawesy@nasnav.com\"\n" +
                        "}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);

        long userId = response.getBody().getEntityId();
        response = getResponseFromGet("/user/recover?email=ahmed.elbastawesy@nasnav.com", ApiResponse.class);
        //Delete this user
        userService.deleteUser(userId);
        Assert.assertTrue(response.getBody().isSuccess());
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
    }

    @Test
    public void testSendResetPasswordTokenForInvalidMail() {

        ResponseEntity<ApiResponse> response = getResponseFromGet("/user/recover?email=foo", ApiResponse.class);
        Assert.assertTrue(response.getBody().getMessages().contains(ResponseStatus.INVALID_EMAIL.name()));
        Assert.assertFalse(response.getBody().isSuccess());
        Assert.assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.getStatusCode().value());
    }

    @Test
    public void testSendResetPasswordTokenForValidButFakeMail() {
        ResponseEntity<ApiResponse> response = getResponseFromGet("/user/recover?email=foo@foo.foo", ApiResponse.class);
        Assert.assertTrue(response.getBody().getMessages().contains(ResponseStatus.EMAIL_NOT_EXIST.name()));
        Assert.assertFalse(response.getBody().isSuccess());
        Assert.assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.getStatusCode().value());
    }

    @Test
    public void testSendResetPasswordTokenForNoPassedMail() {
        ResponseEntity<ApiResponse> response = getResponseFromGet("/user/recover?email=", ApiResponse.class);
        Assert.assertTrue(response.getBody().getMessages().contains(ResponseStatus.INVALID_EMAIL.name()));
        Assert.assertFalse(response.getBody().isSuccess());
        Assert.assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.getStatusCode().value());
    }

    @Test
    public void testPasswordShouldBeReset() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed\",\n" +
                        "\t\"email\":\"ahmed.elbastawesy@nasnav.com\"\n" +
                        "}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);

        long userId = response.getBody().getEntityId();

        getResponseFromGet("/user/recover?email=ahmed.elbastawesy@nasnav.com", ApiResponse.class);

        String token = userService.getUserById(userId).getResetPasswordToken();
        userJson = getHttpEntity("{\t\n" +
                "\t\"token\":\"" + token + "\",\n" +
                "\t\"password\":\"password\"\n" +
                "}");

        response = template.postForEntity(
                "/user/recover", userJson, ApiResponse.class);

        //Delete this user
        userService.deleteUser(userId);
        Assert.assertTrue(response.getBody().isSuccess());
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
    }


    @Test
    public void testInvalidJsonForPasswordRecovery() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed\",\n" +
                        "\t\"email\":\"ahmed.elbastawesy@nasnav.com\"\n" +
                        "}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);

        long userId = response.getBody().getEntityId();

        getResponseFromGet("/user/recover?email=ahmed.elbastawesy@nasnav.com", ApiResponse.class);

        String token = userService.getUserById(userId).getResetPasswordToken();
        userJson = getHttpEntity("{\t\n" +
                "\t\"token\": ,\n" +
                "\t\"password\"" +
                "}");

        response = template.postForEntity(
                "/user/recover", userJson, ApiResponse.class);

        //Delete this user
        userService.deleteUser(userId);
        Assert.assertFalse(response.getBody().isSuccess());
        Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_PARAMETERS));
        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
    }


    @Test
    public void testInvalidPassword() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed\",\n" +
                        "\t\"email\":\"ahmed.elbastawesy@nasnav.com\"\n" +
                        "}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);

        long userId = response.getBody().getEntityId();

        getResponseFromGet("/user/recover?email=ahmed.elbastawesy@nasnav.com", ApiResponse.class);

        String token = userService.getUserById(userId).getResetPasswordToken();
        userJson = getHttpEntity("{\t\n" +
                "\t\"token\":\"" + token + "\",\n" +
                "\t\"password\":\"123\"\n" +
                "}");

        response = template.postForEntity(
                "/user/recover", userJson, ApiResponse.class);

        //Delete this user
        userService.deleteUser(userId);
        Assert.assertFalse(response.getBody().isSuccess());
        Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_PASSWORD));
        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
    }


    @Test
    public void testExpiredToken() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed\",\n" +
                        "\t\"email\":\"ahmed.elbastawesy@nasnav.com\"\n" +
                        "}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);

        long userId = response.getBody().getEntityId();

        getResponseFromGet("/user/recover?email=ahmed.elbastawesy@nasnav.com", ApiResponse.class);

        UserEntity userEntity = userService.getUserById(userId);
        userEntity.setResetPasswordSentAt(userEntity.getResetPasswordSentAt().minusHours(EntityConstants.TOKEN_VALIDITY + 1));
        userService.update(userEntity);

        String token = userEntity.getResetPasswordToken();
        userJson = getHttpEntity("{\t\n" +
                "\t\"token\":\"" + token + "\",\n" +
                "\t\"password\":\"password\"\n" +
                "}");

        response = template.postForEntity(
                "/user/recover", userJson, ApiResponse.class);

        //Delete this user
        userService.deleteUser(userId);
        Assert.assertFalse(response.getBody().isSuccess());
        Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.EXPIRED_TOKEN));
        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
    }



    @Test
    public void testUserShouldLogin() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed\",\n" +
                        "\t\"email\":\"ahmed.elbastawesy@nasnav.com\"\n" +
                        "}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);
        Long userId = response.getBody().getEntityId();

        // send token to user
        getResponseFromGet("/user/recover?email=ahmed.elbastawesy@nasnav.com", ApiResponse.class);

        // use token to change password
        String token = userService.getUserById(userId).getResetPasswordToken();
        userJson = getHttpEntity("{\t\n" +
                "\t\"token\":\"" + token + "\",\n" +
                "\t\"password\":\"password\"\n" +
                "}");

        template.postForEntity(
                "/user/recover", userJson, ApiResponse.class);

        // login using the new password
        userJson = getHttpEntity("{\t\n" +
                "\t\"password\":\"password\",\n" +
                "\t\"email\":\"ahmed.elbastawesy@nasnav.com\"\n" +
                "}");

        response = template.postForEntity(
                "/user/login", userJson, ApiResponse.class);
        //Delete this user
        userService.deleteUser(userId);
        Assert.assertTrue(response.getBody().isSuccess());
        Assert.assertEquals(200, response.getStatusCode().value());
    }


    @Test
    public void testInvalidCredentialsLogin() {
        // register new user
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed\",\n" +
                        "\t\"email\":\"ahmed.elbastawesy@nasnav.com\"\n" +
                        "}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);
        Long userId = response.getBody().getEntityId();

        // try to login with another emial not existed
        String password = userService.getUserById(userId).getResetPasswordToken();
        userJson = getHttpEntity("{\t\n" +
                "\t\"password\":\"" + password + "\",\n" +
                "\t\"email\":\"ahmed.bastawesy@foo.com\"\n" +
                "}");

        response = template.postForEntity(
                "/user/login", userJson, ApiResponse.class);

        //Delete this user
        userService.deleteUser(userId);
        Assert.assertFalse(response.getBody().isSuccess());
        Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_CREDENTIALS));
        Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
    }


    @Test
    public void testInvalidJsonForLogin() {
        // register new user
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed\",\n" +
                        "\t\"email\":\"ahmed.elbastawesy@nasnav.com\"\n" +
                        "}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);
        Long userId = response.getBody().getEntityId();

        // try to login using invalid json
        userJson = getHttpEntity("{\t\n" +
                "\t\"password" +
                "\t\"email\":\"\"\n" +
                "}");

        response = template.postForEntity(
                "/user/login", userJson, ApiResponse.class);
        //Delete this user
        userService.deleteUser(userId);
        Assert.assertFalse(response.getBody().isSuccess());
        Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_PARAMETERS));
        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
    }


    @Test
    public void testLoginForNeedActivationUser() {
        // registe new user
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed\",\n" +
                        "\t\"email\":\"ahmed.elbastawesy@nasnav.com\"\n" +
                        "}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);
        Long userId = response.getBody().getEntityId();

        // directly login without changing his passwrod
        userJson = getHttpEntity("{\t\n" +
                "\t\"password\":\"" + EntityConstants.INITIAL_PASSWORD + "\",\n" +
                "\t\"email\":\"ahmed.elbastawesy@nasnav.com\"\n" +
                "}");

        response = template.postForEntity(
                "/user/login", userJson, ApiResponse.class);
        //Delete this user
        userService.deleteUser(userId);
        Assert.assertFalse(response.getBody().isSuccess());
        Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.NEED_ACTIVATION));
        Assert.assertEquals(HttpStatus.LOCKED.value(), response.getStatusCode().value());
    }


    private HttpEntity<Object> getHttpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private <T> ResponseEntity<T> getResponseFromGet(String URL, Class<T> classRef) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return template.exchange(URL,
                HttpMethod.GET, new HttpEntity<>(headers), classRef);
    }
}
