package com.nasnav.test;

import com.nasnav.NavBox;
import com.nasnav.dao.PickupItemRepository;
import com.nasnav.persistence.PickupItemEntity;
import net.jcip.annotations.NotThreadSafe;
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

import java.util.Set;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class PickupItemTest {

    @Autowired
    private TestRestTemplate template;
    @Autowired
    private PickupItemRepository pickupItemRepository;

    @Test
    public void moveCartItemToPickupItem() {
        HttpEntity entity = getHttpEntity("[99000, 99001]","123");
        ResponseEntity<String> response = template.postForEntity("/pickup/into_pickup", entity, String.class);
        assertEquals(200, response.getStatusCodeValue());

        Set<PickupItemEntity> items = pickupItemRepository.findCurrentPickupItemsByUser_Id(88L);
        assertEquals(2, items.size());
    }
}