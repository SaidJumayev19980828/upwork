package com.nasnav.yeshtery.test.controllers.yeshtery;

import com.nasnav.dao.UserRepository;
import com.nasnav.dto.response.LoyaltyPointTransactionDTO;
import com.nasnav.dto.response.navbox.Wishlist;
import com.nasnav.dto.response.navbox.WishlistItem;
import com.nasnav.persistence.UserEntity;
import com.nasnav.yeshtery.Yeshtery;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import javax.annotation.concurrent.NotThreadSafe;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(classes = Yeshtery.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Wishlist_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
class YeshteryWishlistControllerTest {
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private UserRepository userRepository;

    @Test
    public void getWishlist() {
        HttpEntity<?> request =  getHttpEntity("123");
        ResponseEntity<Wishlist> response =
                template.exchange("/v1/wishlist", GET, request, Wishlist.class);

        Wishlist wishlist = response.getBody();
        Set<Long> ids = getIds(wishlist);
        Assert.assertEquals(OK, response.getStatusCode());
        Assert.assertEquals(2, wishlist.getItems().size());
        assertTrue(setOf(111602L, 111604L).stream().allMatch(ids::contains));
    }

    @Test
    public void getWishlistWithUserId() {
        UserEntity user = userRepository.findById(88L).get();
        String authtoken = user.getAuthenticationToken();
        HttpEntity<?> request =  getHttpEntity(authtoken);
        ResponseEntity<Wishlist> response =
                template.exchange("/v1/wishlist/"+88L, GET, request, Wishlist.class);

        Wishlist wishlist = response.getBody();
        Set<Long> ids = getIds(wishlist);
        Assert.assertEquals(OK, response.getStatusCode());
        Assert.assertEquals(2, wishlist.getItems().size());
        assertTrue(setOf(111602L, 111604L).stream().allMatch(ids::contains));
    }

    private Set<Long> getIds(Wishlist wishlist) {
        return wishlist
                .getItems()
                .stream()
                .map(WishlistItem::getId)
                .collect(toSet());
    }
}
