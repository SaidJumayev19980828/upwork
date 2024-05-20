package com.nasnav.test;


import com.nasnav.dao.AdvertisementProductRepository;
import com.nasnav.dao.AdvertisementRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.response.AdvertisementDTO;
import com.nasnav.dto.response.RestResponsePage;
import com.nasnav.dto.response.navbox.AdvertisementProductDTO;
import com.nasnav.enumerations.CompensationActions;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AdvertisementProductCompensation;
import com.nasnav.persistence.CompensationActionsEntity;
import com.nasnav.persistence.CompensationRulesEntity;
import com.nasnav.service.impl.AdvertisementProductServiceImpl;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.nasnav.exceptions.ErrorCodes.ADVER$002;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static com.nasnav.test.commons.TestCommons.jsonArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Advertisements_Api_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
class AdvertisementTest extends AbstractTestWithTempBaseDir {
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private AdvertisementRepository advertisementRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AdvertisementProductRepository advertisementProductRepository;


    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AdvertisementProductServiceImpl advertisementProductServiceImpl;

    @Test
    void testFindAllAdvertisement() {
        ParameterizedTypeReference<RestResponsePage<AdvertisementDTO>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<RestResponsePage<AdvertisementDTO>> exchange = template.exchange("/advertisement?orgId=" + 99002, HttpMethod.GET,
                getHttpEntity("", "1"), responseType);
        assertThat(exchange.getStatusCode().is2xxSuccessful(), equalTo(true));
        RestResponsePage<AdvertisementDTO> body = exchange.getBody();
        assertThat(body, is(notNullValue()));
        assertThat(body.getContent(), is(notNullValue()));
        assertThat(body.getContent().size(), is(greaterThan(0)));
        LocalDateTime fromDate = LocalDateTime.now().minusDays(13);
        LocalDateTime toDate = LocalDateTime.now().plusDays(13);
        ResponseEntity<RestResponsePage<AdvertisementDTO>> exchange2 = template.exchange("/advertisement?fromDate=" + fromDate + "&toDate=" + toDate
                + "&name=dv", HttpMethod.GET, getHttpEntity("", "1"), responseType);
        assertThat(exchange2.getStatusCode().is2xxSuccessful(), equalTo(true));
    }

    @Test
    public void likeorDislikePostTest(){
        String requestBody =
                json().toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "99");
        ResponseEntity<Void> response = template.postForEntity("/post/like?postId=1&likeAction=true", json, Void.class);
        assertEquals(200, response.getStatusCode().value());

        ResponseEntity<Void> response2 = template.postForEntity("/post/like?postId=1&likeAction=true", json, Void.class);
        assertEquals(200, response2.getStatusCode().value());


        HttpEntity<?> json2 = getHttpEntity(requestBody, "99");
        ResponseEntity<Void> response3 = template.postForEntity("/post/like?postId=1&likeAction=true", json2, Void.class);
        assertEquals(200, response3.getStatusCode().value());

    }


    @Test
    void testCreateAdvertisement() {
        long prevCount = advertisementRepository.count();

        String requestBody =
                json()
                        .put("banner_url", "bannerUrl")
                        .put("from_date", LocalDateTime.now().minusYears(10).toString())
                        .put("to_date", LocalDateTime.now().plusYears(20).toString())
                        .put("org_id", 99001)
                        .put("name" , "advertise test")
                        .put("products",
                                jsonArray()
                                        .put(0, json()
                                                .put("coins", 100)
                                                .put("likes", 3000)
                                                .put("product_id", 1001)
                                                .put("compensation_rules", Set.of(1))
                                        )
                                        .put(1, json()
                                                .put("coins", 100)
                                                .put("likes", 3000)
                                                .put("product_id", 1002)
                                        )
                                        .put(2, json()
                                                .put("coins", 100)
                                                .put("likes", 3000)
                                                .put("product_id", 1003)
                                        )
                        )
                        .toString();

        ParameterizedTypeReference<AdvertisementDTO> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<AdvertisementDTO> exchange = template.exchange("/advertisement", HttpMethod.POST, getHttpEntity(requestBody, "1"), responseType);
        assertThat(exchange.getStatusCode().value(), equalTo(200));
        AdvertisementDTO body = exchange.getBody();
        assertThat(body, is(notNullValue()));
        long newCount = advertisementRepository.count();
        assertThat(prevCount, equalTo(newCount - 1));
        assertThat(body, is(notNullValue()));
        assertThat(body.getId(), is(notNullValue()));
        assertThat(body.getOrgId(), is(notNullValue()));
        assertThat(body.getBannerUrl(), is(notNullValue()));
        assertThat(body.getFromDate(), is(notNullValue()));
        assertThat(body.getToDate(), is(notNullValue()));
        assertThat(body.getCreationDate(), is(notNullValue()));
        assertThat(body.getProducts(), is(notNullValue()));

        assertThat(body.getProducts(), everyItem(hasProperty("id", is(notNullValue()))));


        assertThat(body.getProducts(), hasSize(3));

        assertThat(body.getProducts(), hasItem(hasProperty("productId", is(equalTo(1001L)))));
        assertThat(body.getProducts(), hasItem(hasProperty("productId", is(equalTo(1002L)))));
        assertThat(body.getProducts(), hasItem(hasProperty("productId", is(equalTo(1003L)))));

        assertThat(body.getProducts(), everyItem(hasProperty("brandsDTO", is(notNullValue()))));
        assertThat(body.getProducts(), everyItem(hasProperty("productDetailsDTO", is(notNullValue()))));

    }


    @Test
    void testUpdateAdvertisement() {
        String createRequest =
                json()
                        .put("banner_url", "bannerUrl")
                        .put("from_date", LocalDateTime.now().minusYears(10).toString())
                        .put("to_date", LocalDateTime.now().plusYears(20).toString())
                        .put("org_id", 99001)
                        .put("name" , "advertise test")
                        .put("products",
                                jsonArray()
                                        .put(0, json()
                                                .put("coins", 900)
                                                .put("likes", 3000)
                                                .put("product_id", 1001)
                                        )
                                        .put(1, json()
                                                .put("coins", 21213)
                                                .put("likes", 3000)
                                                .put("product_id", 1002)
                                        )
                                        .put(2, json()
                                                .put("coins", 80016)
                                                .put("likes", 3000)
                                                .put("product_id", 1003)
                                        )
                        )
                        .toString();

        ParameterizedTypeReference<AdvertisementDTO> responseType = new ParameterizedTypeReference<>() {
        };

        ResponseEntity<AdvertisementDTO> createExchange = template.exchange("/advertisement", HttpMethod.POST, getHttpEntity(createRequest, "1"), responseType);
        assertThat(createExchange.getStatusCode().value(), equalTo(200));
        AdvertisementDTO createBody = createExchange.getBody();
        assertThat(createBody, is(notNullValue()));
        assertThat(createBody.getProducts(), hasSize(3));


        String updateRequest =
                json()
                        .put("id", createBody.getId())
                        .put("banner_url", "bannerUrl")
                        .put("from_date", LocalDateTime.now().minusYears(10).toString())
                        .put("to_date", LocalDateTime.now().plusYears(20).toString())
                        .put("org_id", 99001)
                        .put("products",
                                jsonArray()
                                        .put(0, json()
                                                .put("coins", 60012)
                                                .put("likes", 3000)
                                                .put("product_id", 1001)
                                        )
                                        .put(1, json()
                                                .put("coins", 88771)
                                                .put("likes", 3000)
                                                .put("product_id", 1002)
                                        )
                                        .put(2, json()
                                                .put("coins", 992145)
                                                .put("likes", 3000)
                                                .put("product_id", 1003)
                                        )
                                        .put(3, json()
                                                .put("coins", 6565)
                                                .put("likes", 3000)
                                                .put("product_id", 1003)
                                        )
                                        .put(4, json()
                                                .put("coins", 3232)
                                                .put("likes", 3000)
                                                .put("product_id", 1003)
                                        )
                        )
                        .toString();

        ResponseEntity<AdvertisementDTO> updateExchange = template.exchange("/advertisement", HttpMethod.POST, getHttpEntity(updateRequest, "1"), responseType);
        assertThat(updateExchange.getStatusCode().value(), equalTo(200));
        AdvertisementDTO updateBody = updateExchange.getBody();
        assertThat(updateBody, is(notNullValue()));
        assertThat(updateBody.getProducts(), hasSize(5));

        ResponseEntity<AdvertisementDTO> findExchange = template.exchange("/advertisement/" + createBody.getId(), HttpMethod.GET, getHttpEntity("1"), responseType);

        assertThat(updateExchange.getStatusCode().value(), equalTo(200));
        AdvertisementDTO findBody = findExchange.getBody();
        assertThat(findBody, is(notNullValue()));
        assertThat(updateBody.getProducts(), hasSize(5));
    }

    @ParameterizedTest
    @ValueSource(ints = {1005})
    void testFindOneAdvertisement(int advertisementId) {
        ParameterizedTypeReference<AdvertisementDTO> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<AdvertisementDTO> exchange = template.exchange("/advertisement/" + advertisementId, HttpMethod.GET, getHttpEntity("1"), responseType);
        assertThat(exchange.getStatusCode(), equalTo(HttpStatus.OK));
        AdvertisementDTO body = exchange.getBody();
        assertThat(body, is(notNullValue()));
        assertThat(body, is(notNullValue()));
        assertThat(body.getId(), is(notNullValue()));
        assertThat(body.getOrgId(), is(notNullValue()));
        assertThat(body.getBannerUrl(), is(notNullValue()));
        assertThat(body.getFromDate(), is(notNullValue()));
        assertThat(body.getToDate(), is(notNullValue()));
        assertThat(body.getCreationDate(), is(notNullValue()));
        assertThat(body.getProducts(), is(notNullValue()));

        assertThat(body.getProducts(), everyItem(hasProperty("id", is(notNullValue()))));


        assertThat(body.getProducts(), hasSize(3));

        assertThat(body.getProducts(), hasItem(hasProperty("productId", is(equalTo(1001L)))));
        assertThat(body.getProducts(), hasItem(hasProperty("productId", is(equalTo(1002L)))));
        assertThat(body.getProducts(), hasItem(hasProperty("productId", is(equalTo(1003L)))));

        assertThat(body.getProducts(), everyItem(hasProperty("brandsDTO", is(notNullValue()))));
        assertThat(body.getProducts(), everyItem(hasProperty("productDetailsDTO", is(notNullValue()))));
    }

    @ParameterizedTest
    @ValueSource(ints = {1006})
    void testDeleteAdvertisement(int advertisementId) {

        ParameterizedTypeReference<AdvertisementDTO> responseType = new ParameterizedTypeReference<>() {
        };


        ResponseEntity<AdvertisementDTO> firstGetExchange = template.exchange("/advertisement/" + advertisementId, HttpMethod.GET, getHttpEntity("1"), responseType);
        assertThat(firstGetExchange.getStatusCode(), equalTo(HttpStatus.OK));
        AdvertisementDTO firstBodyExchange = firstGetExchange.getBody();
        assertThat(firstBodyExchange, is(notNullValue()));

        Long orgId = firstBodyExchange.getOrgId();
        List<Long> productIds = firstBodyExchange.getProducts().stream().map(AdvertisementProductDTO::getProductId).collect(Collectors.toList());
        List<Long> advertisementProductIds = firstBodyExchange.getProducts().stream().map(AdvertisementProductDTO::getId).collect(Collectors.toList());


        ResponseEntity<Void> deleteExchange = template.exchange("/advertisement/" + advertisementId, HttpMethod.DELETE, getHttpEntity("1"), Void.class);
        assertThat(deleteExchange.getStatusCode(), equalTo(HttpStatus.NO_CONTENT));


        ResponseEntity<AdvertisementDTO> secondGetExchange = template.exchange("/advertisement/" + advertisementId, HttpMethod.GET, getHttpEntity("1"), responseType);
        assertThat(secondGetExchange.getStatusCode(), equalTo(HttpStatus.NO_CONTENT));
        AdvertisementDTO secondBodyExchange = secondGetExchange.getBody();
        assertThat(secondBodyExchange, is(nullValue()));

        assertThat(organizationRepository.findOneById(orgId), is(notNullValue()));
        assertThat(productIds, hasSize(greaterThan(0)));

        // test cascade affected on delete
        advertisementProductIds.forEach(it -> {
            assertThat(advertisementProductRepository.findById(it).isPresent(), equalTo(false));
        });
        // test cascade not affected on delete
        productIds.forEach(it -> {
            assertThat(productRepository.findById(it).isPresent(), equalTo(true));
        });
    }



        // Validates a set of AdvertisementProductCompensation objects with unique CompensationRulesEntity action IDs.
        @Test
        void test_validation_unique_action_ids() {
            // Arrange
            Set<AdvertisementProductCompensation> advertisementProduct = new HashSet<>();
            CompensationActionsEntity action1 = new CompensationActionsEntity(1L, CompensationActions.LIKE, "Action 1");
            CompensationActionsEntity action2 = new CompensationActionsEntity(2L, CompensationActions.SHARE, "Action 2");
            CompensationRulesEntity rule1 = new CompensationRulesEntity(1L, "Rule 1", "Rule 1 description", action1, null, true, new HashSet<>());
            CompensationRulesEntity rule2 = new CompensationRulesEntity(2L, "Rule 2", "Rule 2 description", action2, null, true, new HashSet<>());
            AdvertisementProductCompensation compensation1 = new AdvertisementProductCompensation();
            compensation1.setCompensationRule(rule1);
            compensation1.setId(1L);
            AdvertisementProductCompensation compensation2 = new AdvertisementProductCompensation();
            compensation2.setCompensationRule(rule2);
            compensation2.setId(2L);
            advertisementProduct.add(compensation1);
            advertisementProduct.add(compensation2);

            // Act & Assert
            assertDoesNotThrow(() -> advertisementProductServiceImpl.validateRules(advertisementProduct));
        }

        // Validates an empty set of AdvertisementProductCompensation objects.
        @Test
        void test_empty_set() {
            // Arrange
            Set<AdvertisementProductCompensation> advertisementProduct = new HashSet<>();

            // Act & Assert
            assertDoesNotThrow(() -> advertisementProductServiceImpl.validateRules(advertisementProduct));
        }


        // Throws a RuntimeBusinessException with a BAD_REQUEST HttpStatus and ADVER$002 error code when the set of AdvertisementProductCompensation objects has duplicate CompensationRulesEntity action IDs.
        @Test
        void test_duplicate_action_ids() {
            // Arrange
            Set<AdvertisementProductCompensation> advertisementProduct = new HashSet<>();
            CompensationActionsEntity action1 = new CompensationActionsEntity(1L, CompensationActions.LIKE, "Action 1");
            CompensationRulesEntity rule1 = new CompensationRulesEntity(1L, "Rule 1", "Rule 1 description", action1, null, true, new HashSet<>());
            AdvertisementProductCompensation compensation1 = new AdvertisementProductCompensation();
            compensation1.setCompensationRule(rule1);
            compensation1.setId(1L);
            AdvertisementProductCompensation compensation2 = new AdvertisementProductCompensation();
            compensation2.setCompensationRule(rule1);
            compensation2.setId(2L);
            advertisementProduct.add(compensation1);
            advertisementProduct.add(compensation2);

            // Act & Assert
            RuntimeBusinessException exception = assertThrows(RuntimeBusinessException.class, () -> advertisementProductServiceImpl.validateRules(advertisementProduct));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ADVER$002.getValue(), exception.getErrorMessage());
        }

        @Test
        public void test_single_element() {
            // Arrange
            Set<AdvertisementProductCompensation> advertisementProduct = new HashSet<>();
            CompensationActionsEntity action1 = new CompensationActionsEntity(1L, CompensationActions.LIKE, "Action 1");
            CompensationRulesEntity rule1 = new CompensationRulesEntity(1L, "Rule 1", "Rule 1 description", action1, null, true, new HashSet<>());
            AdvertisementProductCompensation compensation1 = new AdvertisementProductCompensation();
            compensation1.setId(1L);
            compensation1.setCompensationRule(rule1);
            advertisementProduct.add(compensation1);

            // Act & Assert
            assertDoesNotThrow(() -> advertisementProductServiceImpl.validateRules(advertisementProduct));
        }



}
