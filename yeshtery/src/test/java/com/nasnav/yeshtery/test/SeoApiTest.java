package com.nasnav.yeshtery.test;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.SeoKeywordRepository;
import com.nasnav.dao.TagsRepository;
import com.nasnav.dto.SeoKeywordsDTO;
import com.nasnav.enumerations.SeoEntityType;
import com.nasnav.persistence.SeoKeywordEntity;
import com.nasnav.yeshtery.Yeshtery;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.nasnav.enumerations.SeoEntityType.*;
import static com.nasnav.yeshtery.test.commons.TestCommons.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static org.json.JSONObject.NULL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Yeshtery.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Seo_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@NotThreadSafe
public class SeoApiTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private SeoKeywordRepository seoRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TagsRepository tagRepo;


    @Test
    public void getSeoKeywordsByOrganizationTest() throws IOException {
        Long orgId = 99002L;
        var response =
                template.getForEntity(format("/v1/yeshtery/seo?id=%s&type=%s",orgId, ORGANIZATION.name()), String.class);

        var seoEntities = objectMapper.readValue(response.getBody(), new TypeReference<List<SeoKeywordsDTO>>(){});
        assertEquals(4, seoEntities.size());

        var expectedKeywords = asList("Search bot choco!", "Search bot notella!", "Search bot cookie!");
        var retrievedKeywords = getAllKeywords(seoEntities);
        var expectedKeywordsRetrieved =
                retrievedKeywords.containsAll(expectedKeywords);
        assertTrue(expectedKeywordsRetrieved);
    }



    @Test
    public void getSeoKeywordsByEntityTest() throws IOException {
        Long orgId = 99002L;
        var type = PRODUCT;
        Long entityId = 1002L;
        var url = format("/v1/yeshtery/seo?id=%d&type=%s", entityId, type.name());
        var response =
                template.getForEntity(url, String.class);

        var seoEntities = objectMapper.readValue(response.getBody(), new TypeReference<List<SeoKeywordsDTO>>(){});
        assertEquals(1, seoEntities.size());
        assertEquals(entityId, seoEntities.get(0).getId());
        assertEquals(type, seoEntities.get(0).getType());

        var expectedKeywords = asList("Search bot choco!");
        var retrievedKeywords = getAllKeywords(seoEntities);
        var expectedKeywordsRetrieved = retrievedKeywords.containsAll(expectedKeywords);
        assertTrue(expectedKeywordsRetrieved);
    }



    @Test
    public void getSeoKeywordsByCategoryTest() throws IOException {
        Long entityId = 201L;
        var url = format("/v1/yeshtery/seo?id=%d&type=%s", entityId, CATEGORY.name());
        var response =
                template.getForEntity(url, String.class);

        var seoEntities = objectMapper.readValue(response.getBody(), new TypeReference<List<SeoKeywordsDTO>>(){});
        var allTagsHasCategory = allTagsHasCategory(seoEntities, entityId);
        assertEquals(2, seoEntities.size());
        assertTrue(allTagsHasCategory);
        assertEquals(TAG, seoEntities.get(0).getType());

        var expectedKeywords = asList("Search bot konafa with mango!", "Search bot notella!");
        var retrievedKeywords = getAllKeywords(seoEntities);
        var expectedKeywordsRetrieved = retrievedKeywords.containsAll(expectedKeywords);
        assertTrue(expectedKeywordsRetrieved);
    }



    private boolean allTagsHasCategory(List<SeoKeywordsDTO> seoEntities, Long categoryId) {
        return seoEntities
                .stream()
                .map(SeoKeywordsDTO::getId)
                .collect(
                    collectingAndThen(
                            toList(),
                            tagRepo::findByIdIn))
                .stream()
                .allMatch(tag -> Objects.equals(categoryId, tag.getCategoriesEntity().getId()));
    }


    @Test
    public void getSeoKeywordsMissingEntityIdTest(){
        var response =
                template.getForEntity("/v1/yeshtery/seo?type=TAG", String.class);

        assertEquals(BAD_REQUEST, response.getStatusCode());
    }



    @Test
    public void getSeoKeywordsMissingTypeTest() {
        var response =
                template.getForEntity("/v1/yeshtery/seo?id=5002", String.class);

        assertEquals(BAD_REQUEST, response.getStatusCode());
    }



    private Set<String> getAllKeywords(List<SeoKeywordsDTO> seoEntities) {
        return seoEntities
                .stream()
                .map(SeoKeywordsDTO::getKeywords)
                .flatMap(List::stream)
                .collect(toSet());
    }
}
