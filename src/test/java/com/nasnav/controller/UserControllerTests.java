package com.nasnav.controller;

import com.nasnav.response.ApiResponse;
import com.nasnav.response.ResponseStatus;
import com.nasnav.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTests {

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
    public void testUserShouldBeRegistered()  {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed.Ragab\",\n" +
                        "\t\"email\":\"Foo.Bar@Foo.Bar.com\"\n" +
                        "}");
        ResponseEntity<ApiResponse> response = template.postForEntity(
                "/user/register", userJson, ApiResponse.class);
        //Delete this user
        userService.deleteUser(response.getBody().getEntityId());
        Assert.assertTrue(response.getBody().isSuccess());
        Assert.assertEquals(200,response.getStatusCode().value());
    }

    @Test
    public void testEmailExistence() {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed.Ragab\",\n" +
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
        Assert.assertEquals(200,response.getStatusCode().value());
    }

    @Test
    public void testInvalidEmailRegistration()  {
        HttpEntity<Object> userJson = getHttpEntity(
                "{\t\n" +
                        "\t\"name\":\"Ahmed.Ragab\",\n" +
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
