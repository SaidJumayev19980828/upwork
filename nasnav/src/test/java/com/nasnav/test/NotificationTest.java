package com.nasnav.test;


import com.nasnav.NavBox;
import com.nasnav.dao.*;
import com.nasnav.persistence.*;
import com.nasnav.response.UserApiResponse;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Notifications_Test_Data.sql"})
@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class NotificationTest {
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private EmployeeUserRepository employeeUserRepository;
    @Autowired
    private UserTokenRepository userTokenRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ShopsRepository shopsRepository;
    @Autowired
    private NotificationTopicsRepository notificationTopicsRepository;


    @Test
    public void subscribeToTopicTest(){
        String token = "cmplcFidnbv8xBJViEGqR3:APA91bFEHYDsEdoevXcRWy4OZ-dBfgGjf96MnA4RM-B6ILN_OWgL2mgq_vpKTWWtVQC6U04S9HUEIipI7Wvr2rz0u9Jr8WaTtRMeGbkv7bLj43XwWxzBHkkcU5V2CMD3uoYUkvshFgdx";
        String requestBody =
                json()
                        .put("topicName", "ORG99001")
                        .put("token", token)
                        .toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/notification/subscribe", json, Void.class);
        assertEquals(200,response.getStatusCodeValue());

        EmployeeUserEntity employeeUserEntity = employeeUserRepository.getById(70L);
        Set<NotificationTopicsEntity> notificationTopicsEntities = employeeUserEntity.getTopics();
        Optional<NotificationTopicsEntity> result = notificationTopicsEntities.stream().findFirst();
        assertTrue(result.isPresent());
        assertEquals("ORG99001",result.get().getTopic());

        UserTokensEntity userTokensEntity = userTokenRepository.getUserEntityByToken("abcdefg");
        assertEquals(userTokensEntity.getNotificationToken(),token);
    }

    @Test
    public void unsubscribeToTopicTest(){
        String token = "cmplcFidnbv8xBJViEGqR3:APA91bFEHYDsEdoevXcRWy4OZ-dBfgGjf96MnA4RM-B6ILN_OWgL2mgq_vpKTWWtVQC6U04S9HUEIipI7Wvr2rz0u9Jr8WaTtRMeGbkv7bLj43XwWxzBHkkcU5V2CMD3uoYUkvshFgdx";

        String requestBody =
                json()
                        .put("topicName", "ORG99001")
                        .put("token", token)
                        .toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/notification/unsubscribe", json, Void.class);
        assertEquals(200,response.getStatusCodeValue());

        EmployeeUserEntity employeeUserEntity = employeeUserRepository.getById(70L);
        Set<NotificationTopicsEntity> notificationTopicsEntities = employeeUserEntity.getTopics();
        Optional<NotificationTopicsEntity> result = notificationTopicsEntities.stream().findFirst();
        if(notificationTopicsEntities.size() == 0){
            assertFalse(result.isPresent());
        }
        else {
            assertNotEquals("ORG99001",result.get().getTopic());
        }
    }


    @Test
    public void updateEmployeeTokenTest(){
        String token = "cmplcFidnbv8xBJViEGqR3:APA91bFEHYDsEdoevXcRWy4OZ-dBfgGjf96MnA4RM-B6ILN_OWgL2mgq_vpKTWWtVQC6U04S9HUEIipI7Wvr2rz0u9Jr8WaTtRMeGbkv7bLj43XwWxzBHkkcU5V2CMD3uoYUkvshFgdd";


        HttpEntity<?> json = getHttpEntity(token, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/notification/updateToken", json, Void.class);
        assertEquals(200,response.getStatusCodeValue());

        UserTokensEntity userTokensEntity = userTokenRepository.getUserEntityByToken("abcdefg");
        assertEquals(token,userTokensEntity.getNotificationToken());
    }

    @Test
    public void createOrUpdateUserToken(){
        String token = "cecrvZZ1VbO3jKyhMTPxBW:APA91bGXxLfjL-pEhAWympWeGUgWPXcs1kijXIAUyhNHo_sh2zmRiGYsDJHtdtFPawCf3rUgL1YT2rsESuAfD4JrdrjoQATTivb6ZLDaAol5uPrKnBsbJlslNoEsqtiNCBmRhRcR2YPj";

        HttpEntity<?> json = getHttpEntity(token, "123");
        ResponseEntity<Void> response = template.postForEntity("/notification/user/updateToken", json, Void.class);
        assertEquals(200,response.getStatusCodeValue());

        assertEquals(token,userTokenRepository.getUserEntityByToken("123").getNotificationToken());
    }

    @Test
    public void refreshNotificationTopics(){

        HttpEntity<?> json = getHttpEntity("abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/notification/refreshTopics", json, Void.class);
        assertEquals(200,response.getStatusCodeValue());

        List<OrganizationEntity> organizationEntities = organizationRepository.findAllOrganizations();
        for(OrganizationEntity org : organizationEntities){
            assertTrue(notificationTopicsRepository.existsByTopic(org.getTopic().getTopic()));
        }

        List<ShopsEntity> shopsEntities = shopsRepository.findAllShops();
        for (ShopsEntity shop : shopsEntities){
            assertTrue(notificationTopicsRepository.existsByTopic(shop.getTopic().getTopic()));
        }

        assertEquals(organizationEntities.size()+shopsEntities.size(),notificationTopicsRepository.count());
    }

    @Test
    public void loginTest(){
        String email = "user1@nasnav.com";
        String password = "12345678";

        String request = new JSONObject()
                .put("password", password)
                .put("email", email)
                .put("org_id", 99001L)
                .put("employee", true)
                .put("notificationToken","cecrvZZ1VbO3jKyhMTPxBW:APA91bGXxLfjL-pEhAWympWeGUgWPXcs1kijXIAUyhNHo_sh2zmRiGYsDJHtdtFPawCf3rUgL1YT2rsESuAfD4JrdrjoQATTivb6ZLDaAol5uPrKnBsbJlslNoEsqtiNCBmRhRcR2YPj")
                .toString();

        HttpEntity<Object> userJson = getHttpEntity(request, "DOESNOT-NEED-TOKEN");
        ResponseEntity<UserApiResponse> response =
                template.postForEntity("/user/login", userJson,	UserApiResponse.class);

        assertEquals(200, response.getStatusCode().value());

        EmployeeUserEntity employeeUserEntity = employeeUserRepository.getById(71L);
        assertEquals(1,employeeUserEntity.getTopics().size());
    }

    /*
    if you would like to use the following test you have to get two real tokens from two different ui machines and
    replace them with token, token1
    PS: i tested it on my two pcs and it bring the notifications correctly
     */
//    @Test
//    public void realTest(){
//        String token = "cecrvZZ1VbO3jKyhMTPxBW:APA91bGXxLfjL-pEhAWympWeGUgWPXcs1kijXIAUyhNHo_sh2zmRiGYsDJHtdtFPawCf3rUgL1YT2rsESuAfD4JrdrjoQATTivb6ZLDaAol5uPrKnBsbJlslNoEsqtiNCBmRhRcR2YPj";
//        String token1 = "cmplcFidnbv8xBJViEGqR3:APA91bFEHYDsEdoevXcRWy4OZ-dBfgGjf96MnA4RM-B6ILN_OWgL2mgq_vpKTWWtVQC6U04S9HUEIipI7Wvr2rz0u9Jr8WaTtRMeGbkv7bLj43XwWxzBHkkcU5V2CMD3uoYUkvshFgdx";
//
//        String requestBody =
//                json()
//                        .put("topicName", "topic1")
//                        .put("token", token)
//                        .toString();
//        HttpEntity<?> json = getHttpEntity(requestBody, "hijkllm");
//        ResponseEntity<Void> response = template.postForEntity("/notification/subscribe", json, Void.class);
//        assertEquals(200,response.getStatusCodeValue());
//
//        String requestBody1 =
//                json()
//                        .put("topicName", "topic1")
//                        .put("token", token1)
//                        .toString();
//        HttpEntity<?> json1 = getHttpEntity(requestBody1, "hijkllm");
//        ResponseEntity<Void> response1 = template.postForEntity("/notification/subscribe", json1, Void.class);
//        assertEquals(200,response1.getStatusCodeValue());
//
//        String requestBody3 =
//                json()
//                        .put("target", "topic1")
//                        .put("title", "tit msg")
//                        .put("body", "body msg")
//                        .toString();
//        HttpEntity<?> json3 = getHttpEntity(requestBody3, "hijkllm");
//        ResponseEntity<Void> response3 = template.postForEntity("/notification/topic", json3, Void.class);
//        assertEquals(200,response3.getStatusCodeValue());
//    }
}
