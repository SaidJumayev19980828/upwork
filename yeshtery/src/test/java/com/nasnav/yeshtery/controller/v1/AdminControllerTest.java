package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.CategoryDTO;
import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.response.CategoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * INFO: Java 17 migration pre-test: This test is written before migration to java 17.
 * FIXME: It should run as-is after migrating to java 17
 * <p>
 * The AdminControllerTest class is responsible for testing the functionality of the AdminController class.
 * It contains test methods for various operations such as creating organizations, categories, setting tags and products,
 * deleting categories, listing organizations, listing theme classes, listing themes, updating theme classes and themes,
 * deleting theme classes and themes, adding and removing countries, updating domain, invalidating caches, getting organization domains,
 * deleting organization domains, deleting search indices, bulk updating brand priorities, and getting API calls.
 */
@EnabledIfEnvironmentVariable(
        named = "USER",
        matches = "icomma",
        disabledReason = "Java 17 migration, needs local postgresql db with required value")
@EnabledOnOs(OS.LINUX)
@Slf4j
@Disabled
class AdminControllerTest {
    private static final WebTestClient WEB_TEST_CLIENT = WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:8050")
            .defaultHeader("User-Token", TokenHelper.token())
            .build();

    @RepeatedTest(3)
    void createCategory() {

        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName("Accessories_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        categoryDTO.setOperation("create");

        // create category
        CategoryResponse response = WEB_TEST_CLIENT.post()
                .uri("/v1/admin/category")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(categoryDTO))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CategoryResponse.class)
                .value(categoryResponse -> {
                    assertThat(categoryResponse).isNotNull();
                    assertThat(categoryResponse.getCategoryId()).isNotNull();
                })
                .returnResult()
                .getResponseBody();
        log.info("The created response result is: {} ", response);

        //Update category
        CategoryDTO updatedCategoryDTO = new CategoryDTO();
        updatedCategoryDTO.setId(response.getCategoryId());
        updatedCategoryDTO.setName("Updated Accessories_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        updatedCategoryDTO.setOperation("update");

        CategoryResponse updatedResponse = WEB_TEST_CLIENT.post()
                .uri("/v1/admin/category")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updatedCategoryDTO))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CategoryResponse.class)
                .value((CategoryResponse categoryResponse) -> {
                    assertThat(categoryResponse).isNotNull();
                    assertThat(categoryResponse.getCategoryId()).isNotNull();
                })
                .returnResult()
                .getResponseBody();
        log.info("The updated response result is: {} ", updatedResponse);

        // delete category
        CategoryResponse deleteResponse = WEB_TEST_CLIENT.delete()
                .uri("/v1/admin/category?category_id=" + updatedResponse.getCategoryId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CategoryResponse.class)
                .value((CategoryResponse categoryResponse) -> {
                    assertThat(categoryResponse).isNotNull();
                    assertThat(categoryResponse.getCategoryId()).isNotNull();
                })
                .returnResult()
                .getResponseBody();
        log.info("The delete response result is: {} ", deleteResponse);
    }

    @RepeatedTest(3)
    void listOrganizations() {

        // Perform GET request http://localhost:8050/v1/admin/list_organizations
        List<OrganizationRepresentationObject> organizations = WEB_TEST_CLIENT.get().uri("http://localhost:8050/v1/admin/list_organizations")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrganizationRepresentationObject.class)
                .hasSize(36)
                .value(response -> assertThat(response)
                        .anyMatch(orgObject -> orgObject.getId() == 6
                                && orgObject.getName().equals("Abdou Mostafa")
                                && orgObject.getCurrency().equals("EGP")))
                .returnResult()
                .getResponseBody();
        log.info("There is {} organisations in the DB", organizations.size());
    }
}