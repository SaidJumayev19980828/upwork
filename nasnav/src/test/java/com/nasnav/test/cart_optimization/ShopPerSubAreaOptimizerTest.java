package com.nasnav.test.cart_optimization;

import com.nasnav.dao.StockRepository;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.CartOptimizeResponseDTO;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;
import java.util.Optional;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Optimize_Test_Data_2.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class ShopPerSubAreaOptimizerTest extends AbstractTestWithTempBaseDir {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private StockRepository stockRepo;



    @Test
    public void subAreaOptimizerTest(){
        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<Cart> response =
                template.exchange("/cart", GET, request, Cart.class);

        assertEquals(OK, response.getStatusCode());
        assertEquals(2, response.getBody().getItems().size());

        String optimizeRequestBody = createOptimizeRequestBody().toString();
        HttpEntity<?> optimizeRequest =  getHttpEntity(optimizeRequestBody, "123");
        ResponseEntity<CartOptimizeResponseDTO> optimizeResponse =
                template.exchange("/cart/optimize", POST, optimizeRequest, CartOptimizeResponseDTO.class);

        assertEquals(OK, optimizeResponse.getStatusCode());
        assertEquals(2, optimizeResponse.getBody().getCart().getItems().size());
        assertSubareaShopSelected(optimizeResponse.getBody().getCart(), 501L);
    }



    @Test
    public void subAreaOptimizerNotSupportedAreaTest(){
        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<Cart> response =
                template.exchange("/cart", GET, request, Cart.class);

        assertEquals(OK, response.getStatusCode());
        assertEquals(2, response.getBody().getItems().size());

        String optimizeRequestBody =
                createOptimizeRequestBody()
                        .put("customer_address", 12300003L)
                        .toString();
        HttpEntity<?> optimizeRequest =  getHttpEntity(optimizeRequestBody, "123");
        ResponseEntity<CartOptimizeResponseDTO> optimizeResponse =
                template.exchange("/cart/optimize", POST, optimizeRequest, CartOptimizeResponseDTO.class);

        assertEquals(NOT_ACCEPTABLE, optimizeResponse.getStatusCode());
    }



    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Optimize_Test_Data_4.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void subAreaShopHasNoEnoughStockTest(){
        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<Cart> response =
                template.exchange("/cart", GET, request, Cart.class);

        assertEquals(OK, response.getStatusCode());
        assertEquals(2, response.getBody().getItems().size());

        String optimizeRequestBody = createOptimizeRequestBody().toString();
        HttpEntity<?> optimizeRequest =  getHttpEntity(optimizeRequestBody, "123");
        ResponseEntity<CartOptimizeResponseDTO> optimizeResponse =
                template.exchange("/cart/optimize", POST, optimizeRequest, CartOptimizeResponseDTO.class);

        assertEquals(OK, optimizeResponse.getStatusCode());
    }



    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Optimize_Test_Data_3.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void subAreaOptimizerNotSupportedAreaButHasDefaultShopTest(){
        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<Cart> response =
                template.exchange("/cart", GET, request, Cart.class);

        assertEquals(OK, response.getStatusCode());
        assertEquals(2, response.getBody().getItems().size());

        String optimizeRequestBody =
                createOptimizeRequestBody()
                        .put("customer_address", 12300003L)
                        .toString();
        HttpEntity<?> optimizeRequest =  getHttpEntity(optimizeRequestBody, "123");
        ResponseEntity<CartOptimizeResponseDTO> optimizeResponse =
                template.exchange("/cart/optimize", POST, optimizeRequest, CartOptimizeResponseDTO.class);

        assertEquals(OK, optimizeResponse.getStatusCode());
        assertEquals(2, optimizeResponse.getBody().getCart().getItems().size());
        assertSubareaShopSelected(optimizeResponse.getBody().getCart(), 502L);
    }




    private JSONObject createOptimizeRequestBody() {
        return json()
                .put("customer_address", 12300001L)
                .put("shipping_service_id", "TEST")
                .put("additional_data", json());
    }


    private void assertSubareaShopSelected(Cart cart, Long selectedShop) {
        boolean isSubAreaShopSelected =
                cart
                    .getItems()
                    .stream()
                    .map(CartItem::getStockId)
                        .map(stockRepo::findById)
                        .map(Optional::get)
                        .map(StocksEntity::getShopsEntity)
                        .map(ShopsEntity::getId)
                    .allMatch(id -> Objects.equals(id, selectedShop));
        assertTrue(isSubAreaShopSelected);
    }
}
