package com.nasnav.yeshtery.test.controllers.yeshtery;

import com.nasnav.dao.CartItemRepository;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.service.CartService;
import com.nasnav.service.OrderService;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.concurrent.NotThreadSafe;

import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class CartTest extends AbstractTestWithTempBaseDir {


    @Autowired
    private TestRestTemplate template;

    @Autowired
    private CartItemRepository cartItemRepo;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;


    @Test
    public void deleteCartItemSuccess() {
        //Test
        Long itemsCountBefore = cartItemRepo.countByUser_Id(88L);
        Long itemId = cartItemRepo.findCurrentCartItemsByUser_Id(88L).get(0).getId();
        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<Cart> response =
                template.exchange("/v1/cart/item?item_id=" + itemId, DELETE, request, Cart.class);

        assertEquals(OK, response.getStatusCode());
        assertEquals(itemsCountBefore - 1 , response.getBody().getItems().size());
    }

    @Test
    public void deleteCartItemWithEmployeeTokenAndException() {
        //Test
        Long itemsCountBefore = cartItemRepo.countByUser_Id(88L);
        Long itemId = cartItemRepo.findCurrentCartItemsByUser_Id(88L).get(0).getId();
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<Cart> response =
                template.exchange("/v1/cart/item?item_id=" + itemId, DELETE, request, Cart.class);

        assertEquals(FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void deleteCartItemSuccessWithEmployeeToken() {
        Long userId=88L;
        Long itemsCountBefore = cartItemRepo.countByUser_Id(88L);
        Long itemId = cartItemRepo.findCurrentCartItemsByUser_Id(88L).get(0).getId();
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<Cart> response =
                template.exchange("/v1/cart/item?item_id=" + itemId +"&user_id="+ userId , DELETE, request, Cart.class);

        assertEquals(OK, response.getStatusCode());
        assertEquals(itemsCountBefore - 1 , response.getBody().getItems().size());
    }

}
