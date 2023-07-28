package com.nasnav.test;


import com.nasnav.dao.AdvertisementRepository;
import com.nasnav.dto.ProductBaseInfo;
import com.nasnav.dto.response.AdvertisementDTO;
import com.nasnav.dto.response.RestResponsePage;
import com.nasnav.persistence.AdvertisementEntity;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Arrays;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Advertisements_Api_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
class AdvertisementTest extends AbstractTestWithTempBaseDir {
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private AdvertisementRepository advertisementRepository;

    @Test
    void testFindAllAdvertisement() {
        ParameterizedTypeReference<RestResponsePage<AdvertisementDTO>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<RestResponsePage<AdvertisementDTO>> exchange = template.exchange("/advertisement", HttpMethod.GET, getHttpEntity("", "1"), responseType);
        assertThat(exchange.getStatusCode().is2xxSuccessful(), equalTo(true));
        RestResponsePage<AdvertisementDTO> body = exchange.getBody();
        assertThat(body, is(notNullValue()));
        assertThat(body.getContent(), is(notNullValue()));
        assertThat(body.getContent().size(), is(greaterThan(0)));
    }

    @Test
    void testCreateAdvertisement() {
        long prevCount = advertisementRepository.count();
        ProductBaseInfo product = new ProductBaseInfo();
        product.setId(1L);
        String requestBody =
                json()
                        .put("coins", 100)
                        .put("likes", 3)
                        .put("fromDate", LocalDateTime.now().plusMonths(1).toString())
                        .put("toDate", LocalDateTime.now().plusMonths(4).toString())
                        .put("product", json().put("id",1001))
                        .toString();

        ParameterizedTypeReference<AdvertisementDTO> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<AdvertisementDTO> exchange = template.exchange("/advertisement", HttpMethod.POST, getHttpEntity(requestBody, "1"), responseType);
        assertThat(exchange.getStatusCode().is2xxSuccessful(), equalTo(true));
        AdvertisementDTO body = exchange.getBody();
        assertThat(body, is(notNullValue()));
        long newCount = advertisementRepository.count();
        assertThat(prevCount, equalTo(newCount - 1));
        assertThat(body,is(notNullValue()));
        assertThat(body.getId(),is(notNullValue()));
        AdvertisementEntity advertisement = advertisementRepository.getOne(body.getId());
        assertThat(advertisement,is(notNullValue()));

    }
}
