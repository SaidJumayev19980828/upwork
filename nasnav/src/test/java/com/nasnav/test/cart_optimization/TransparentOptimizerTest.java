package com.nasnav.test.cart_optimization;


import com.nasnav.NavBox;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.CartOptimizeResponseDTO;
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

import java.util.List;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Optimize_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class TransparentOptimizerTest {

    @Autowired
    private TestRestTemplate template;


    @Test
    public void transparentOptimizerTest(){
        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<Cart> response =
                template.exchange("/cart", GET, request, Cart.class);

        assertEquals(OK, response.getStatusCode());
        assertEquals(2, response.getBody().getItems().size());
        assertProductNamesReturned(response);


        String optimizeRequestBody = createOptimizeRequestBody();
        HttpEntity<?> optimizeRequest =  getHttpEntity(optimizeRequestBody, "123");
        ResponseEntity<CartOptimizeResponseDTO> optimizeResponse =
                template.exchange("/cart/optimize", POST, optimizeRequest, CartOptimizeResponseDTO.class);

        assertEquals(OK, optimizeResponse.getStatusCode());
        assertEquals(2, optimizeResponse.getBody().getCart().getItems().size());
        assertProductNamesReturned(response);

        assertEquals("Transparent optimizer doesn't change the cart"
                , response.getBody()
                , optimizeResponse.getBody().getCart());
    }




    private String createOptimizeRequestBody() {
        return json()
                .put("customer_address", 12300001L)
                .put("shipping_service_id", "TEST")
                .put("additional_data", json())
                .toString();
    }


    private void assertProductNamesReturned(ResponseEntity<Cart> response) {
        List<String> expectedProductNames = asList("product_1", "product_4");
        boolean isProductNamesReturned =
                response
                        .getBody()
                        .getItems()
                        .stream()
                        .map(CartItem::getName)
                        .allMatch(expectedProductNames::contains);
        assertTrue(isProductNamesReturned);
    }
}
