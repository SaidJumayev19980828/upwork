package com.nasnav.controller;

import com.nasnav.dto.ProductDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO 1 : Pre migration tests. It should pass after migration.
 * This class contains test methods for the ProductsController class.
 * It tests various functionalities of the ProductsController class.
 */
@EnabledIfEnvironmentVariable(
		named = "USER",
		matches = "icomma",
		disabledReason = "Java 17 migration, needs local postgresql db with required value")
@EnabledOnOs(OS.LINUX)
@Slf4j
@Disabled
class ProductsControllerTest {


    private static final WebTestClient WEB_TEST_CLIENT = WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:8040")
            .defaultHeader("User-Token", TokenHelper.token())
            .build();

	@Test
	void getProduct() throws IOException {

        var productResource = new ClassPathResource("/json/product-id-1.json").getInputStream();
        ProductDetailsDTO expectedProductDetails = TokenHelper.OBJECT_MAPPER.readValue(productResource, ProductDetailsDTO.class);

        // Perform GET /product operation.
        ProductDetailsDTO response = WEB_TEST_CLIENT
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/product")
                        .queryParam("product_id", "1")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductDetailsDTO.class)
                .value(productDetailsDTO -> {
                    assertThat(productDetailsDTO)
                            .usingRecursiveComparison()
                            .ignoringFieldsMatchingRegexes(".*price", ".*discount")
                            .isEqualTo(expectedProductDetails);
                })
                .returnResult()
                .getResponseBody();

        log.info("Product details: {}", response);
    }
}