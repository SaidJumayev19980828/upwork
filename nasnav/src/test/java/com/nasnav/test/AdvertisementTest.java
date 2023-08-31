package com.nasnav.test;


import com.nasnav.dao.AdvertisementProductRepository;
import com.nasnav.dao.AdvertisementRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.response.AdvertisementDTO;
import com.nasnav.dto.response.RestResponsePage;
import com.nasnav.dto.response.navbox.AdvertisementProductDTO;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.nasnav.test.commons.TestCommons.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Advertisements_Api_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
class AdvertisementTest extends AbstractTestWithTempBaseDir {
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private AdvertisementRepository advertisementRepository;

    @ParameterizedTest
    @ValueSource(strings = {"", "?orgId=99002"})
    void testFindAllAdvertisement(String urlQuery) {
        ParameterizedTypeReference<RestResponsePage<AdvertisementDTO>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<RestResponsePage<AdvertisementDTO>> exchange = template.exchange("/advertisement" + urlQuery, HttpMethod.GET, getHttpEntity("", "1"), responseType);
        assertThat(exchange.getStatusCode().is2xxSuccessful(), equalTo(true));
        RestResponsePage<AdvertisementDTO> body = exchange.getBody();
        assertThat(body, is(notNullValue()));
        assertThat(body.getContent(), is(notNullValue()));
        assertThat(body.getContent().size(), is(greaterThan(0)));
    }

    @Test
    void testCreateAdvertisement() {
        long prevCount = advertisementRepository.count();

        String requestBody =
                json()
                        .put("bannerUrl", "bannerUrl")
                        .put("fromDate", LocalDateTime.now().minusYears(10).toString())
                        .put("toDate", LocalDateTime.now().plusYears(20).toString())
                        .put("orgId", 99001)
                        .put("advertisementProductDTOS",
                                jsonArray()
                                        .put(0, json()
                                                .put("coins", 100)
                                                .put("likes", 3000)
                                                .put("productId", 1001)
                                        )
                                        .put(1, json()
                                                .put("coins", 100)
                                                .put("likes", 3000)
                                                .put("productId", 1002)
                                        )
                                        .put(2, json()
                                                .put("coins", 100)
                                                .put("likes", 3000)
                                                .put("productId", 1003)
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
        assertThat(body.getAdvertisementProductDTOS(), is(notNullValue()));

        assertThat(body.getAdvertisementProductDTOS(), everyItem(hasProperty("id", is(notNullValue()))));


        assertThat(body.getAdvertisementProductDTOS(), hasSize(3));

        assertThat(body.getAdvertisementProductDTOS(), hasItem(hasProperty("productId", is(equalTo(1001L)))));
        assertThat(body.getAdvertisementProductDTOS(), hasItem(hasProperty("productId", is(equalTo(1002L)))));
        assertThat(body.getAdvertisementProductDTOS(), hasItem(hasProperty("productId", is(equalTo(1003L)))));

        assertThat(body.getAdvertisementProductDTOS(), everyItem(hasProperty("brandsDTO", is(notNullValue()))));
        assertThat(body.getAdvertisementProductDTOS(), everyItem(hasProperty("productDetailsDTO", is(notNullValue()))));

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
        assertThat(body.getAdvertisementProductDTOS(), is(notNullValue()));

        assertThat(body.getAdvertisementProductDTOS(), everyItem(hasProperty("id", is(notNullValue()))));


        assertThat(body.getAdvertisementProductDTOS(), hasSize(3));

        assertThat(body.getAdvertisementProductDTOS(), hasItem(hasProperty("productId", is(equalTo(1001L)))));
        assertThat(body.getAdvertisementProductDTOS(), hasItem(hasProperty("productId", is(equalTo(1002L)))));
        assertThat(body.getAdvertisementProductDTOS(), hasItem(hasProperty("productId", is(equalTo(1003L)))));

        assertThat(body.getAdvertisementProductDTOS(), everyItem(hasProperty("brandsDTO", is(notNullValue()))));
        assertThat(body.getAdvertisementProductDTOS(), everyItem(hasProperty("productDetailsDTO", is(notNullValue()))));
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AdvertisementProductRepository advertisementProductRepository;


    @Autowired
    private OrganizationRepository organizationRepository;

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
        List<Long> productIds = firstBodyExchange.getAdvertisementProductDTOS().stream().map(AdvertisementProductDTO::getProductId).collect(Collectors.toList());
        List<Long> advertisementProductIds = firstBodyExchange.getAdvertisementProductDTOS().stream().map(AdvertisementProductDTO::getId).collect(Collectors.toList());


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

}
