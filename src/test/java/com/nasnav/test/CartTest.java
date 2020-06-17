package com.nasnav.test;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import com.nasnav.dao.CartItemRepository;
import com.nasnav.dto.response.navbox.CartItem;
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

import com.nasnav.NavBox;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.response.ThemeClassResponse;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class CartTest {

	@Autowired
    private TestRestTemplate template;

	@Autowired
	private CartItemRepository cartItemRepo;
	
	
	@Test
	public void getCartNoAuthz() {
        HttpEntity<?> request =  getHttpEntity("NOT FOUND");
        ResponseEntity<Cart> response =
        		template.exchange("/cart", GET, request, Cart.class);

        assertEquals(UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void getCartNoAuthN() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<Cart> response = 
        		template.exchange("/cart", GET, request, Cart.class);

        assertEquals(FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	@Test 
	public void getCartSuccess() {
        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<Cart> response = 
        		template.exchange("/cart", GET, request, Cart.class);

        assertEquals(OK, response.getStatusCode());
        assertEquals(2, response.getBody().getItems().size());
	}


	@Test
	public void addCartItemSuccess() {
		Long itemsCountBefore = cartItemRepo.countByUser_Id(88L);

		JSONObject item = createCartItem();

		HttpEntity<?> request =  getHttpEntity(item.toString(),"123");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item", POST, request, Cart.class);

		assertEquals(200, response.getStatusCodeValue());
		assertEquals(itemsCountBefore + 1 , response.getBody().getItems().size());
	}


	@Test
	public void addCartItemNoStock() {
		JSONObject item = createCartItem();
		item.remove("stock_id");

		HttpEntity<?> request =  getHttpEntity(item.toString(),"123");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item", POST, request, Cart.class);

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}


	@Test
	public void addCartItemNoQuantity() {
		JSONObject item = createCartItem();
		item.remove("quantity");

		HttpEntity<?> request =  getHttpEntity(item.toString(),"123");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item", POST, request, Cart.class);

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}


	@Test
	public void addCartItemNegativeQuantity() {
		JSONObject item = createCartItem();
		item.put("quantity", -1);

		HttpEntity<?> request =  getHttpEntity(item.toString(),"123");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item", POST, request, Cart.class);

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}


	@Test
	public void addCartNoAuthz() {
		JSONObject item = createCartItem();
		HttpEntity<?> request =  getHttpEntity(item.toString(), "NOT FOUND");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item", POST, request, Cart.class);

		assertEquals(UNAUTHORIZED, response.getStatusCode());
	}


	@Test
	public void addCartNoAuthN() {
		JSONObject item = createCartItem();
		HttpEntity<?> request =  getHttpEntity(item.toString(), "101112");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item", POST, request, Cart.class);

		assertEquals(FORBIDDEN, response.getStatusCode());
	}


	@Test
	public void deleteCartItemSuccess() {
		Long itemsCountBefore = cartItemRepo.countByUser_Id(88L);
		Long itemId = cartItemRepo.findCurrentCartItemsByUser_Id(88L).get(0).getId();
		HttpEntity<?> request =  getHttpEntity("123");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item?item_id=" + itemId, DELETE, request, Cart.class);

		assertEquals(OK, response.getStatusCode());
		assertEquals(itemsCountBefore - 1 , response.getBody().getItems().size());
	}



	@Test
	public void removeCartNoAuthz() {
		Long itemId = cartItemRepo.findCurrentCartItemsByUser_Id(88L).get(0).getId();
		HttpEntity<?> request =  getHttpEntity("NOT FOUND");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item?item_id=" + itemId, DELETE, request, Cart.class);

		assertEquals(UNAUTHORIZED, response.getStatusCode());
	}


	@Test
	public void removeCartNoAuthN() {
		Long itemId = cartItemRepo.findCurrentCartItemsByUser_Id(88L).get(0).getId();
		HttpEntity<?> request =  getHttpEntity("101112");
		ResponseEntity<Cart> response =
				template.exchange("/cart/item?item_id=" + itemId, DELETE, request, Cart.class);

		assertEquals(FORBIDDEN, response.getStatusCode());
	}


	private JSONObject createCartItem() {
		JSONObject item = new JSONObject();
		item.put("stock_id", 601);
		item.put("cover_img", "img");
		item.put("quantity", 1);

		return item;
	}
}
