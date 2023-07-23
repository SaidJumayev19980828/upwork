package com.nasnav.test;

import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;

@RunWith(SpringRunner.class)
@NotThreadSafe
public class StatisticsTest extends AbstractTestWithTempBaseDir {

    @Autowired
    private TestRestTemplate template;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_13.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOrdersStatisticsTest() {
        HttpEntity request = getHttpEntity("131415");
        ResponseEntity<List> res =
                template.exchange("/statistics/orders?" +
                    "statuses=STORE_CONFIRMED" +
                        "&type=COUNT", GET, request, List.class);

        assertEquals(200, res.getStatusCodeValue());
        assertFalse(res.getBody().isEmpty());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_10.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getOpenCartsTest() {
        HttpEntity request = getHttpEntity("101112");
        ResponseEntity<List> res = template.exchange("/statistics/carts", GET, request, List.class);

        assertEquals(200, res.getStatusCodeValue());
        assertFalse(res.getBody().isEmpty());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_13.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getSoldProductsTest() {
        HttpEntity request = getHttpEntity("131415");
        ResponseEntity<Map> res = template.exchange("/statistics/sold_products", GET, request, Map.class);

        assertEquals(200, res.getStatusCodeValue());
        assertFalse(res.getBody().isEmpty());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Orders_Test_Data_Insert_13.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getSalesTest() {
        HttpEntity request = getHttpEntity("131415");
        ResponseEntity<Map> res = template.exchange("/statistics/sales", GET, request, Map.class);

        assertEquals(200, res.getStatusCodeValue());
        assertFalse(res.getBody().isEmpty());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/UserRegisterTest.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getUsersStaticticsTest() {
        HttpEntity request = getHttpEntity("131415");
        ResponseEntity<Map> res = template.exchange("/statistics/users", GET, request, Map.class);

        assertEquals(200, res.getStatusCodeValue());
        assertFalse(res.getBody().isEmpty());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Cart_Test_Data_10.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getUsersCartsTest() {
        HttpEntity request = getHttpEntity("101112");
        ResponseEntity<List> res = template.exchange("/statistics/users/carts", GET, request, List.class);

        assertEquals(200, res.getStatusCodeValue());
        assertFalse(res.getBody().isEmpty());
        assertEquals(1, res.getBody().size());
    }

}
