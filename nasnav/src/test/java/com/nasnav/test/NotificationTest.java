package com.nasnav.test;

import com.google.firebase.FirebaseApp;
import com.nasnav.NavBox;
import com.nasnav.dao.CartItemRepository;
import com.nasnav.dao.WishlistItemRepository;
import com.nasnav.dto.request.notification.NotificationRequestDto;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.Wishlist;
import com.nasnav.dto.response.navbox.WishlistItem;
import com.nasnav.persistence.CartItemEntity;
import com.nasnav.service.notification.NotificationServiceImpl;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.service.helpers.CartServiceHelper.ADDITIONAL_DATA_PRODUCT_ID;
import static com.nasnav.service.helpers.CartServiceHelper.ADDITIONAL_DATA_PRODUCT_TYPE;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static java.util.stream.Collectors.toSet;
import static org.json.JSONObject.NULL;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
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

    @Autowired
    private TestRestTemplate template;

    @Autowired
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
