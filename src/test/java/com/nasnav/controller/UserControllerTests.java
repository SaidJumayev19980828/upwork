package com.nasnav.controller;

import com.nasnav.NavBox;
import com.nasnav.dao.UserRepository;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
public class UserControllerTests {

    private MockMvc mockMvc;

    @Mock
    private UserController userController;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    UserRepository userRepository;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testUserShouldBeRegistered()  {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed\",\n" +
                        "\t\"email\":\"Foo.Bar@Foo.Bar.com\"\n" +
                        "}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);
        //Delete this user
        new UserService(userRepository).deleteUser(response.getBody().getEntityId());
        Assert.assertTrue(response.getBody().isSuccess());
        Assert.assertEquals(200,response.getStatusCode().value());
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
        new UserService(userRepository).deleteUser(userId);
        Assert.assertEquals(200,response.getStatusCode().value());
    }

    @Test
    public void testInvalidEmailRegistration()  {
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
        Assert.assertEquals(200,response.getStatusCode().value());
    }

    @Test
    public void testInvalidNameRegistration()  {
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
        Assert.assertEquals(200,response.getStatusCode().value());
    }

    @Test
    public void testInvalidJsonForUserRegisteration()  {
        HttpEntity<Object> userJson = getHttpEntity(
                "{}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);
        // success should be false
        Assert.assertFalse(response.getBody().isSuccess());
        // response status should contain INVALID_NAME
        Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_NAME));
        Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_EMAIL));
        Assert.assertEquals(200,response.getStatusCode().value());
    }


    private HttpEntity<Object> getHttpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
