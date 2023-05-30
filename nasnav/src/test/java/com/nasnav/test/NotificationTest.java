package com.nasnav.test;

import com.google.firebase.FirebaseApp;
import com.nasnav.NavBox;
import com.nasnav.dto.request.notification.NotificationRequestDto;
import com.nasnav.service.notification.NotificationServiceImpl;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Notifications_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class NotificationTest {

    @Mock
    private NotificationServiceImpl notificationService;


    @Test
    public void firebaseInitializationTest() {
        notificationService.initialize();
        System.out.println(FirebaseApp.getApps());
        assertFalse(FirebaseApp.getApps().isEmpty());
    }

    @Test
    public void sendTestMessage() {
        String token ="cmplcFidnbv8xBJViEGqR3:APA91bFEHYDsEdoevXcRWy4OZ-dBfgGjf96MnA4RM-B6ILN_OWgL2mgq_vpKTWWtVQC6U04S9HUEIipI7Wvr2rz0u9Jr8WaTtRMeGbkv7bLj43XwWxzBHkkcU5V2CMD3uoYUkvshFgdx";
        NotificationRequestDto notifications = new NotificationRequestDto(token, "testMessage", "testTopic");
        try {
            notificationService.sendMessage(notifications);
        } catch (Exception e) {
            fail();
        }

    }
}
