package com.nasnav.test;

import com.nasnav.NavBox;
import com.nasnav.dao.CartItemRepository;
import com.nasnav.dao.WishlistItemRepository;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.Wishlist;
import com.nasnav.dto.response.navbox.WishlistItem;
import com.nasnav.persistence.CartItemEntity;
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

import java.util.HashSet;
import java.util.Set;

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
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Wishlist_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class WishlistTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private CartItemRepository cartRepo;

    @Autowired
    private WishlistItemRepository wishlistRepo;

    @Test
    public void getWishlistNoAuthz() {
        HttpEntity<?> request =  getHttpEntity("NOT FOUND");
        ResponseEntity<Wishlist> response =
                template.exchange("/wishlist", GET, request, Wishlist.class);

        assertEquals(UNAUTHORIZED, response.getStatusCode());
    }



    @Test
    public void getWishlistNoAuthN() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<Wishlist> response =
                template.exchange("/wishlist", GET, request, Wishlist.class);

        assertEquals(FORBIDDEN, response.getStatusCode());
    }



    @Test
    public void postWishlistItemNoAuthz() {
        HttpEntity<?> request =  getHttpEntity("NOT FOUND");
        ResponseEntity<Wishlist> response =
                template.exchange("/wishlist/item", POST, request, Wishlist.class);

        assertEquals(UNAUTHORIZED, response.getStatusCode());
    }



    @Test
    public void postWishlistItemNoAuthN() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<Wishlist> response =
                template.exchange("/wishlist/item", POST, request, Wishlist.class);

        assertEquals(FORBIDDEN, response.getStatusCode());
    }



    @Test
    public void deleteWishlistItemNoAuthz() {
        Long id = 111602L;
        HttpEntity<?> request =  getHttpEntity("NOT FOUND");
        ResponseEntity<Wishlist> response =
                template.exchange("/wishlist/item?item_id="+id, POST, request, Wishlist.class);

        assertEquals(UNAUTHORIZED, response.getStatusCode());
    }



    @Test
    public void deleteWishlistItemNoAuthN() {
        Long id = 111602L;
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<Wishlist> response =
                template.exchange("/wishlist/item?item_id="+id, DELETE, request, Wishlist.class);

        assertEquals(FORBIDDEN, response.getStatusCode());
    }



    @Test
    public void moveWishlistItemToCartNoAuthz() {
        Long id = 111602L;
        HttpEntity<?> request =  getHttpEntity("NOT FOUND");
        ResponseEntity<Cart> response =
                template.exchange("/wishlist/item/into_cart", POST, request, Cart.class);

        assertEquals(UNAUTHORIZED, response.getStatusCode());
    }



    @Test
    public void moveWishlistItemToCartNoAuthN() {
        Long id = 111602L;
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<Cart> response =
                template.exchange("/wishlist/item/into_cart", POST, request, Cart.class);

        assertEquals(FORBIDDEN, response.getStatusCode());
    }




    @Test
    public void getWishlist() {
        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<Wishlist> response =
                template.exchange("/wishlist", GET, request, Wishlist.class);

        Wishlist wishlist = response.getBody();
        Set<Long> ids = getIds(wishlist);
        assertEquals(OK, response.getStatusCode());
        assertEquals(2, wishlist.getItems().size());
        assertTrue(setOf(111602L, 111604L).stream().allMatch(ids::contains));
    }




    private Set<Long> getIds(Wishlist wishlist) {
        return wishlist
                .getItems()
                .stream()
                .map(WishlistItem::getId)
                .collect(toSet());
    }



    private Set<Long> getIds(Cart cart) {
        return cart
                .getItems()
                .stream()
                .map(CartItem::getId)
                .collect(toSet());
    }




    @Test
    public void postWishlistItem(){
        JSONObject requestBody =
                json()
                .put("stock_id", 605L)
                .put("cover_image", "36/good_img.jpg");
        HttpEntity<?> request =  getHttpEntity(requestBody.toString(), "123");
        ResponseEntity<Wishlist> response =
                template.exchange("/wishlist/item", POST, request, Wishlist.class);

        Wishlist wishlist = response.getBody();
        Set<Long> stocks =
                wishlist
                .getItems()
                .stream()
                .map(WishlistItem::getStockId)
                .collect(toSet());

        assertEquals(OK, response.getStatusCode());
        assertEquals(3, wishlist.getItems().size());
        assertTrue(setOf(602L, 604L, 605L).stream().allMatch(stocks::contains));
    }




    @Test
    public void deleteWishlistItem() {
        Long id = 111602L;
        assertTrue(wishlistRepo.findById(id).isPresent());

        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<Wishlist> response =
                template.exchange("/wishlist/item?item_id="+id, DELETE, request, Wishlist.class);

        assertEquals(OK, response.getStatusCode());

        Wishlist wishlist = response.getBody();
        Set<Long> ids = getIds(wishlist);
        assertEquals(1, wishlist.getItems().size());
        assertTrue(setOf(111604L).stream().allMatch(ids::contains));
        assertFalse("The item will be deleted", wishlistRepo.findById(id).isPresent());
    }





    @Test
    public void moveWishlistItemToCart() {
        Long id = 111602L;

        assertFalse(cartRepo.findById(id).isPresent());
        assertTrue(wishlistRepo.findById(id).isPresent());
        //-------------------
        JSONObject requestBody =
                json()
                .put("item_id", id)
                .put("quantity", NULL);
        HttpEntity<?> request =  getHttpEntity(requestBody.toString(), "123");
        ResponseEntity<Cart> response =
                template.exchange("/wishlist/item/into_cart", POST, request, Cart.class);
        //-------------------

        assertEquals(OK, response.getStatusCode());

        Cart cart = response.getBody();
        Set<Long> ids = getIds(cart);
        Long cartItemId = cart.getItems().get(0).getId();
        assertTrue("new cart item is created with new id", cartRepo.findById(cartItemId).isPresent());
        CartItemEntity item = cartRepo.findById(cartItemId).get();
        assertTrue("wishlist item still exists with same id", wishlistRepo.findById(id).isPresent());
        assertEquals(1, cart.getItems().size());
        assertEquals("default quantity for the new cart items is 1", 1, item.getQuantity().intValue());
    }





    @Test
    public void deleteWishlistItemOfAnotherCustomer() {
        Long id = 111602L;
        assertTrue(wishlistRepo.findById(id).isPresent());

        HttpEntity<?> request =  getHttpEntity("456");
        ResponseEntity<Wishlist> response =
                template.exchange("/wishlist/item?item_id="+id, DELETE, request, Wishlist.class);

        assertEquals(OK, response.getStatusCode());
        assertTrue("The item will not be deleted", wishlistRepo.findById(id).isPresent());
    }




    @Test
    public void moveWishlistItemToCartOfAnotherCustomer() {
        Long id = 111602L;
        JSONObject requestBody =
                json()
                .put("item_id", id)
                .put("quantity", NULL);
        HttpEntity<?> request =  getHttpEntity(requestBody.toString(), "456");
        ResponseEntity<Cart> response =
                template.exchange("/wishlist/item/into_cart", POST, request, Cart.class);

        assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }





    @Test
    public void moveWishlistItemToCartNonExistingItem() {
        Long id = -1L;
        JSONObject requestBody =
                json()
                .put("item_id", id)
                .put("quantity", NULL);
        HttpEntity<?> request =  getHttpEntity(requestBody.toString(),"123");
        ResponseEntity<Cart> response =
                template.exchange("/wishlist/item/into_cart", POST, request, Cart.class);

        assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }




    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void addWishlistItemWithAdditionalData(){
        Long stockId = 606L;
        int collectionId = 1009;
        int productType = 2;

        JSONObject additionalData = json()
                .put(ADDITIONAL_DATA_PRODUCT_ID, collectionId)
                .put(ADDITIONAL_DATA_PRODUCT_TYPE, productType);
        JSONObject itemJson =
                json()
                .put("stock_id", stockId)
                .put("additional_data", additionalData);

        HttpEntity<?> request =  getHttpEntity(itemJson.toString(),"123");
        ResponseEntity<Wishlist> response =
                template.exchange("/wishlist/item", POST, request, Wishlist.class);

        assertEquals(200, response.getStatusCodeValue());

        CartItem item = getCartItemOfStock(stockId, response);

        assertEquals(additionalData.toMap().keySet(), item.getAdditionalData().keySet());
        assertEquals(new HashSet<>(additionalData.toMap().values()), new HashSet<>(item.getAdditionalData().values()));
        assertEquals(collectionId, item.getProductId().intValue());
        assertEquals(productType, item.getProductType().intValue());
    }




    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Cart_Test_Data.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void addWishlistItemWithAdditionalDataAddedToMainJson(){
        Long stockId = 606L;
        int collectionId = 1009;
        int productType = 2;

        JSONObject additionalData = json();
        JSONObject itemJson =
                json()
                    .put("stock_id", stockId)
                    .put("additional_data", additionalData)
                    .put("product_id", collectionId)
                    .put("product_type", productType);

        HttpEntity<?> request =  getHttpEntity(itemJson.toString(),"123");
        ResponseEntity<Wishlist> response =
                template.exchange("/wishlist/item", POST, request, Wishlist.class);

        assertEquals(200, response.getStatusCodeValue());

        CartItem item = getCartItemOfStock(stockId, response);

        Set<String> expectedAdditionalDataFields = setOf(ADDITIONAL_DATA_PRODUCT_ID, ADDITIONAL_DATA_PRODUCT_TYPE);
        Set<Integer> expectedAdditionalDataValues = setOf(collectionId, productType);
        assertEquals(expectedAdditionalDataFields, item.getAdditionalData().keySet());
        assertEquals(expectedAdditionalDataValues, new HashSet<>(item.getAdditionalData().values()));
        assertEquals(collectionId, item.getProductId().intValue());
        assertEquals(productType, item.getProductType().intValue());
    }




    private WishlistItem getCartItemOfStock(Long stockId, ResponseEntity<Wishlist> response) {
        return	response
                .getBody()
                .getItems()
                .stream()
                .filter(it -> it.getStockId().equals(stockId))
                .findFirst()
                .get();
    }
}
