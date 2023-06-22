package com.nasnav.test;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.SeoKeywordRepository;
import com.nasnav.dto.SeoKeywordsDTO;
import com.nasnav.enumerations.SeoEntityType;
import com.nasnav.persistence.SeoKeywordEntity;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.nasnav.enumerations.SeoEntityType.*;
import static com.nasnav.test.commons.TestCommons.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.json.JSONObject.NULL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Seo_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@NotThreadSafe
public class SeoApiTest extends AbstractTestWithTempBaseDir {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private SeoKeywordRepository seoRepo;

    @Autowired
    private ObjectMapper objectMapper;



    @Test
    public void addSeoKeywordsTest(){
        Long orgId = 99001L;
        var entityId = orgId;
        var type = ORGANIZATION;
        var seoKeywordsBefore =
                seoRepo.findByEntityIdAndTypeIdAndOrganization_Id(entityId, type.getValue(), orgId);

        assertTrue(seoKeywordsBefore.isEmpty());

        var keywords = asList("Hello Search BOT!", "Here's 10 LE");

        var requestBody = createAddSeoKeywordRequest(keywords, entityId, type);

        var response =
                template.postForEntity("/organization/seo"
                        , getHttpEntity(requestBody.toString(), "192021")
                        , String.class);

        assertKeywordDataValid(orgId, keywords, requestBody);
    }



    private JSONObject createAddSeoKeywordRequest(List<String> keywords, Long entityId, SeoEntityType typ) {
        return json()
                .put("type", typ.name())
                .put("id", entityId)
                .put("keywords", toJsonArray(keywords));
    }







    private void assertKeywordDataValid(Long orgId, List<String> keywords, JSONObject requestBody) {
        Long entityId = requestBody.getLong("id");
        var type = requestBody.getString("type" );
        var typeId = SeoEntityType.valueOf(type).getValue();
        var seoKeywordsAfter =
                seoRepo.findByEntityIdAndTypeIdAndOrganization_Id(entityId, typeId, orgId);
        assertEquals(keywords.size(), seoKeywordsAfter.size());

        var allHaveOrganizationType =
                seoKeywordsAfter
                    .stream()
                    .allMatch(kw -> Objects.equals(kw.getTypeId(), ORGANIZATION.getValue()));

        var allFollowTheOrganization =
                seoKeywordsAfter
                        .stream()
                        .allMatch(kw -> Objects.equals(kw.getOrganization().getId(), orgId));

        var allAttachedToOrganizationEntity =
                seoKeywordsAfter
                        .stream()
                        .allMatch(kw -> Objects.equals(kw.getEntityId(), orgId));

        var allKeywordsWereAdded =
                keywords.stream().allMatch(kw -> anySeoKeywordEntityHasValue(seoKeywordsAfter, kw));

        assertTrue(allHaveOrganizationType);
        assertTrue(allFollowTheOrganization);
        assertTrue(allAttachedToOrganizationEntity);
        assertTrue(allKeywordsWereAdded);
    }




    private boolean anySeoKeywordEntityHasValue(List<SeoKeywordEntity> seoKeywordsAfter, String kw) {
        return seoKeywordsAfter.stream().anyMatch(ent -> ent.getKeyword().equals(kw));
    }



    @Test
    public void addSeoKeywordsNoAuthNTest(){
        var response =
                template.postForEntity("/organization/seo"
                        , getHttpEntity("NOT-EXISTING")
                        , String.class);
        assertEquals(UNAUTHORIZED, response.getStatusCode());
    }



    @Test
    public void addSeoKeywordNoAuthZTest(){
        var response =
                template.postForEntity("/organization/seo"
                        , getHttpEntity("161718")
                        , String.class);
        assertEquals(FORBIDDEN, response.getStatusCode());
    }



    @Test
    public void updateSeoKeywordForExistingEntityTest(){
        Long orgId = 99002L;
        var entityId = orgId;
        var type = ORGANIZATION;
        var seoKeywordsBefore =
                seoRepo.findByEntityIdAndTypeIdAndOrganization_Id(entityId, type.getValue(), orgId);

        assertEquals(1, seoKeywordsBefore.size());

        var keywords = asList("Hello Search BOT!", "Here's 10 LE");

        var requestBody = createAddSeoKeywordRequest(keywords ,entityId, type);

        var response =
                template.postForEntity("/organization/seo"
                        , getHttpEntity(requestBody.toString(), "131415")
                        , String.class);

        assertKeywordDataValid(orgId, keywords, requestBody);
    }



    @Test
    public void addSeoKeywordMissingDataTest(){
        var keywords = asList("Hello Search BOT!", "Here's 10 LE");

        var requestBody = createAddSeoKeywordRequest(keywords, 1001L, PRODUCT);
        requestBody.put("type", NULL);

        var response =
                template.postForEntity("/organization/seo"
                        , getHttpEntity(requestBody.toString(), "192021")
                        , String.class);
        assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }



    @Test
    public void addSeoKeywordEntityFromAnotherOrgTest(){
        Long orgId = 99001L;
        var entityId = orgId;
        var type = ORGANIZATION;
        var seoKeywordsBefore =
                seoRepo.findByEntityIdAndTypeIdAndOrganization_Id(entityId, type.getValue(), orgId);

        assertTrue(seoKeywordsBefore.isEmpty());

        var keywords = asList("Hello Search BOT!", "Here's 10 LE");

        var requestBody = createAddSeoKeywordRequest(keywords, entityId, type);

        var response =
                template.postForEntity("/organization/seo"
                        , getHttpEntity(requestBody.toString(), "131415")
                        , String.class);

        assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }



    @Test
    public void addSeoKeywordTagEntityFromAnotherOrgTest(){
        Long orgId = 99001L;
        Long entityId = 5001L;

        var keywords = asList("Hello Search BOT!", "Here's 10 LE");

        var requestBody =
                json()
                .put("type", TAG.name())
                .put("id", entityId)
                .put("keywords", toJsonArray(keywords));

        var response =
                template.postForEntity("/organization/seo"
                        , getHttpEntity(requestBody.toString(), "131415")
                        , String.class);

        assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }



    @Test
    public void addSeoKeywordProductEntityFromAnotherOrgTest(){
        Long orgId = 99001L;
        Long entityId = 1001L;
        var type = PRODUCT;
        var seoKeywordsBefore =
                seoRepo.findByEntityIdAndTypeIdAndOrganization_Id(entityId, type.getValue(), orgId);

        assertTrue(seoKeywordsBefore.isEmpty());

        var keywords = asList("Hello Search BOT!", "Here's 10 LE");

        var requestBody =
                json()
                .put("type", type.name())
                .put("id", entityId)
                .put("keywords", toJsonArray(keywords));

        var response =
                template.postForEntity("/organization/seo"
                        , getHttpEntity(requestBody.toString(), "131415")
                        , String.class);

        assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }



    @Test
    public void getSeoKeywordsByOrganizationTest() throws IOException {
        Long orgId = 99002L;
        var response =
                template.getForEntity("/navbox/seo?org_id="+orgId, String.class);

        var seoEntities = objectMapper.readValue(response.getBody(), new TypeReference<List<SeoKeywordsDTO>>(){});
        assertEquals(3, seoEntities.size());

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
        var url = String.format("/navbox/seo?org_id=%d&id=%d&type=%s", orgId, entityId, type.name());
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
    public void getSeoKeywordsMissingOrgIdTest() throws IOException {
        var response =
                template.getForEntity("/navbox/seo?id=5002&type=TAG", String.class);

        assertEquals(BAD_REQUEST, response.getStatusCode());
    }



    @Test
    public void getSeoKeywordsMissingEntityIdTest() throws IOException {
        Long orgId = 99002L;
        var response =
                template.getForEntity("/navbox/seo?type=TAG&org_id="+orgId, String.class);

        assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }




    @Test
    public void getSeoKeywordsMissingTypeTest() throws IOException {
        Long orgId = 99002L;
        var response =
                template.getForEntity("/navbox/seo?id=5002&org_id="+orgId, String.class);

        assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }




    private Set<String> getAllKeywords(List<SeoKeywordsDTO> seoEntities) {
        return seoEntities
                .stream()
                .map(SeoKeywordsDTO::getKeywords)
                .flatMap(List::stream)
                .collect(toSet());
    }

    @Test
    public void keywordsCreatedOrderTest() throws JsonProcessingException {
        Long orgId = 99001L;
        Long entityId = orgId;
        SeoEntityType type = ORGANIZATION;

        List<String> keywords = asList("Keyword_1", "Keyword_2", "Keyword_3");

        JSONObject requestBody = createAddSeoKeywordRequest(keywords, entityId, type);

        ResponseEntity<String> response = template.postForEntity("/organization/seo",
                getHttpEntity(requestBody.toString(), "192021"), String.class);
        assertEquals(OK, response.getStatusCode());
        assertKeywordsCreatedOrder(orgId, requestBody);
    }

    private void assertKeywordsCreatedOrder(Long orgId, JSONObject requestBody) throws JsonProcessingException {
        Long entityId = requestBody.getLong("id");
        String type = requestBody.getString("type" );

        ResponseEntity<String> response = template.getForEntity(format("/navbox/seo?id=%d&type=%s&org_id=%d", entityId, type, orgId), String.class);
        var seoEntities = objectMapper.readValue(response.getBody(), new TypeReference<List<SeoKeywordsDTO>>(){});
        List<String> keywords = seoEntities.get(0).getKeywords();

        assertEquals(1, seoEntities.size());
        assertEquals("Keyword_1", keywords.get(0));
        assertEquals("Keyword_2", keywords.get(1));
        assertEquals("Keyword_3", keywords.get(2));
    }
}
