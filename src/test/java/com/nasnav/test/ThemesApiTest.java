package com.nasnav.test;
import static com.google.common.primitives.Longs.asList;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.util.List;

import com.nasnav.dao.*;
import com.nasnav.persistence.OrganizationEntity;
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
import com.nasnav.persistence.ThemeEntity;
import com.nasnav.response.ThemeClassResponse;
import com.nasnav.response.ThemeResponse;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/database_cleanup.sql","/sql/Themes_API_Test_Data_Insert.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class ThemesApiTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ThemeClassRepository themeClassRepo;

    @Autowired
    private ThemesRepository themesRepo;

    @Autowired
    private OrganizationThemeSettingsRepository orgThemeSettingsRepo;

    @Autowired
    private OrganizationRepository orgRepo;


    @Test
    public void getThemesClasses() {
        HttpEntity<?> request = getHttpEntity("101112");
        ResponseEntity<List> response = template.exchange("/admin/themes/class",
                GET, request, List.class);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(!response.getBody().isEmpty());
        assertEquals(2, response.getBody().size());
    }


    @Test
    public void createThemeClassSuccess() {
        JSONObject body = json().put("name", "theme_class_1");

        HttpEntity<?> request =  getHttpEntity(body.toString(),"101112");
        ResponseEntity<ThemeClassResponse> response = template.exchange("/admin/themes/class",
                POST, request, ThemeClassResponse.class);

        assertEquals(200,response.getStatusCodeValue());
        assertTrue(themeClassRepo.existsById(response.getBody().getId()));

        // get rid of created theme because can't remove it via sql query .. I don't know its id
        themeClassRepo.deleteById(response.getBody().getId());
    }


    @Test
    public void updateThemeClassSuccess() {
        JSONObject body = json().put("id", 990011)
                                .put("name", "new name");

        HttpEntity<?> request =  getHttpEntity(body.toString(),"101112");
        ResponseEntity<ThemeClassResponse> response = template.exchange("/admin/themes/class",
                POST, request, ThemeClassResponse.class);

        assertEquals(200,response.getStatusCodeValue());
        assertEquals("new name",themeClassRepo.findById(response.getBody().getId()).get().getName());
    }


    @Test
    public void createThemeClassInvalidToken() {
        JSONObject body = json().put("name", "new theme_class_1");

        HttpEntity<?> request =  getHttpEntity(body.toString(),"1011123");
        ResponseEntity<String> response = template.exchange("/admin/themes/class",
                POST, request, String.class);

        assertEquals(401,response.getStatusCodeValue());
    }


    @Test
    public void createThemeClassUnauthorizedToken() {
    	JSONObject body = json().put("name", "new theme_class_1");
        HttpEntity<?> request =  getHttpEntity(body.toString(),"131415");
        ResponseEntity<ThemeClassResponse> response = template.exchange("/admin/themes/class",
                POST, request, ThemeClassResponse.class);

        assertEquals(403,response.getStatusCodeValue());
    }


    @Test
    public void createThemeClassInvalidId() {
        JSONObject body = json().put("id", -1)
                                .put("name", "new theme_class_1");

        HttpEntity<?> request =  getHttpEntity(body.toString(),"101112");
        ResponseEntity<String> response = template.exchange("/admin/themes/class",
                POST, request, String.class);

        assertEquals(406,response.getStatusCodeValue());
    }


    @Test
    public void deleteThemesAndThemeClass() {
        HttpEntity<?> request =  getHttpEntity("101112");
        template.exchange("/admin/themes?id=5001",
                DELETE, request, String.class);

        template.exchange("/admin/themes?id=5002",
                DELETE, request, String.class);

        template.exchange("/admin/themes/class?id=990011",
                DELETE, request, String.class);

        assertTrue(!themesRepo.existsById(5001));
        assertTrue(!themeClassRepo.existsById(990011));
    }


    @Test
    public void deleteThemesClassInvalidToken() {
        HttpEntity<?> request =  getHttpEntity("1011122");
        ResponseEntity<String> response = template.exchange("/admin/themes/class?id=990011",
                DELETE, request, String.class);

        assertEquals(401, response.getStatusCodeValue());
    }


    @Test
    public void deleteThemesClassUnauthenticatedToken() {
        HttpEntity<?> request =  getHttpEntity("131415");
        ResponseEntity<String> response = template.exchange("/admin/themes/class?id=990011",
                DELETE, request, String.class);

        assertEquals(403, response.getStatusCodeValue());
    }


    @Test
    public void deleteThemeClassUsedId() {
        HttpEntity<?> request =  getHttpEntity("101112");

        ResponseEntity<String> response = template.exchange("/admin/themes/class?id=990013",
                DELETE, request, String.class);

        assertEquals(406, response.getStatusCodeValue());
    }

    @Test
    public void getThemes() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<List> response = template.exchange("/admin/themes",
                GET, request, List.class);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(!response.getBody().isEmpty());
        assertEquals(3, response.getBody().size());
    }


    @Test
    public void createThemeSuccess() {
        JSONObject body = json().put("theme_class_id", 990011)
                                .put("name", "new theme_class_1")
                                .put("uid", "5001");

        HttpEntity<?> request =  getHttpEntity(body.toString(),"101112");
        ResponseEntity<ThemeResponse> response = template.exchange("/admin/themes",
                POST, request, ThemeResponse.class);

        System.out.println(response.getBody());
        assertEquals(200,response.getStatusCodeValue());
        assertTrue(themesRepo.existsByUid(response.getBody().getThemeId()));
        ThemeEntity theme = themesRepo.findByUid(response.getBody().getThemeId()).get();
        themesRepo.delete(theme);
    }


    @Test
    public void updateThemeSuccess() {
        JSONObject body = json().put("name", "new theme name")
                                .put("preview_image", "new theme image")
                                .put("default_settings", "new theme settings")
                                .put("theme_class_id", 990012)
                                .put("uid", "5002");

        HttpEntity<?> request =  getHttpEntity(body.toString(),"101112");
        ResponseEntity<ThemeResponse> response = template.exchange("/admin/themes",
                POST, request, ThemeResponse.class);

        assertEquals(200,response.getStatusCodeValue());

        ThemeEntity entity = themesRepo.findByUid(response.getBody().getThemeId()).get();

        assertEquals("new theme name", entity.getName());
        assertEquals("new theme image", entity.getPreviewImage());
        assertEquals("new theme settings", entity.getDefaultSettings());
        assertEquals(990012, entity.getThemeClassEntity().getId().intValue());
    }


    @Test
    public void createThemeInvalidToken() {
        JSONObject body = json().put("theme_class_id", 990011)
                                .put("name", "theme_1");

        HttpEntity<?> request =  getHttpEntity(body.toString(),"1011122");
        ResponseEntity<ThemeResponse> response = template.exchange("/admin/themes",
                POST, request, ThemeResponse.class);

        assertEquals(401,response.getStatusCodeValue());
    }


    @Test
    public void createThemeUnauthenticatedToken() {
        JSONObject body = json().put("name", "theme_1")
                                .put("theme_class_id", 990011);

        HttpEntity<?> request =  getHttpEntity(body.toString(),"131415");
        ResponseEntity<String> response = template.exchange("/admin/themes",
                POST, request, String.class);

        assertEquals(403,response.getStatusCodeValue());
    }


    @Test
    public void createThemeMissingId() {
        JSONObject body = json().put("name", "theme_1")
                                .put("theme_class_id", 990011);

        HttpEntity<?> request =  getHttpEntity(body.toString(),"101112");
        ResponseEntity<String> response = template.exchange("/admin/themes",
                POST, request, String.class);

        assertEquals(406,response.getStatusCodeValue());
    }


    @Test
    public void createThemeInvalidClassId() {
        JSONObject body = json().put("name", "theme_1")
                                .put("theme_class_id", -1);

        HttpEntity<?> request =  getHttpEntity(body.toString(),"101112");
        ResponseEntity<ThemeResponse> response = template.exchange("/admin/themes",
                POST, request, ThemeResponse.class);

        assertEquals(406,response.getStatusCodeValue());
    }


    @Test
    public void createThemeMissingClassId() {
        JSONObject body = json().put("name", "theme_1");
        HttpEntity<?> request =  getHttpEntity(body.toString(),"101112");
        ResponseEntity<String> response = template.exchange("/admin/themes",
                POST, request, String.class);

        assertEquals(406,response.getStatusCodeValue());
    }


    @Test
    public void deleteThemesInvalidToken() {
        HttpEntity<?> request =  getHttpEntity("1011122");
        ResponseEntity<String> response = template.exchange("/admin/themes?id=5004",
                DELETE, request, String.class);

        assertEquals(401, response.getStatusCodeValue());
    }


    @Test
    public void deleteThemesUnauthenticatedToken() {
        HttpEntity<?> request =  getHttpEntity("131415");
        ResponseEntity<String> response = template.exchange("/admin/themes?id=5001",
                DELETE, request, String.class);

        assertEquals(403, response.getStatusCodeValue());
    }


    @Test
    public void deleteThemeInvalidId() {
        HttpEntity<?> request =  getHttpEntity("101112");

        ResponseEntity<String> response = template.exchange("/admin/themes?id=990013",
                DELETE, request, String.class);

        assertEquals(406, response.getStatusCodeValue());
    }


    @Test
    public void getOrganizationThemesClasses() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<List> response = template.exchange("/organization/themes/class?org_id=99001",
                GET, request, List.class);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(!response.getBody().isEmpty());
        assertEquals(1, response.getBody().size());
    }


    @Test
    public void getOrganizationThemesClassesInvalidOrg() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<String> response = template.exchange("/organization/themes/class?org_id=99003",
                GET, request, String.class);

        assertEquals(404, response.getStatusCodeValue());
    }


    @Test
    public void getOrganizationThemesClassesInvalidToken() {
        HttpEntity<?> request =  getHttpEntity("10111f2");
        ResponseEntity<String> response = template.exchange("/organization/themes/class?org_id=99003",
                GET, request, String.class);

        assertEquals(401, response.getStatusCodeValue());
    }


    @Test
    public void getOrganizationThemesClassesUnauthenticatedToken() {
        HttpEntity<?> request =  getHttpEntity("131415");
        ResponseEntity<String> response = template.exchange("/organization/themes/class?org_id=99003",
                GET, request, String.class);

        assertEquals(403, response.getStatusCodeValue());
    }




    @Test
    public void assignOrganizationThemeClassInvalidOrg() {
        String body = json().put("org_id", 99003).put("class_id", 990012).toString();
        HttpEntity<?> request =  getHttpEntity(body,"101112");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class",
                        POST, request, String.class);

        assertEquals(404,response.getStatusCodeValue());
    }


    @Test
    public void assignOrganizationThemeClassInvalidClass() {
        String body = json().put("org_id", 99003).put("class_id", 990014).toString();
        HttpEntity<?> request =  getHttpEntity(body, "101112");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class",
                        POST, request, String.class);

        assertEquals(404,response.getStatusCodeValue());
    }


    @Test
    public void assignOrganizationThemeClassInvalidToken() {
        HttpEntity<?> request =  getHttpEntity("1011d12");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class?org_id=99001&class_id=990012",
                        POST, request, String.class);

        assertEquals(401,response.getStatusCodeValue());
    }


    @Test
    public void assignOrganizationThemeClassUnauthenticatedToken() {
        HttpEntity<?> request =  getHttpEntity("131415");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class?org_id=99001&class_id=990012",
                        POST, request, String.class);

        assertEquals(403,response.getStatusCodeValue());
    }


    @Test
    public void assignOrganizationThemeClassExistingClass() {
        String body = json().put("org_id", 99001).put("class_ids", asList(990011, 990012)).toString();
        HttpEntity<?> request =  getHttpEntity(body, "101112");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class",
                        POST, request, String.class);

        assertEquals(200,response.getStatusCodeValue());
        assertEquals(2, themeClassRepo.countByOrganizationId(99001L).intValue());
    }


    @Test
    public void removeOrgThemeClassUsedThemeInOrgAndNotExistingInAddedThemeClass() {
        String body = json().put("org_id", 99001).put("class_ids", asList()).toString();
        HttpEntity<?> request =  getHttpEntity(body, "101112");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class",
                        POST, request, String.class);

        assertEquals(406,response.getStatusCodeValue());
        assertTrue(orgThemeSettingsRepo.existsByOrganizationEntity_IdAndThemeId(99001L, 5003));
    }


    @Test
    public void addAndRemoveOrgThemeClass() {
        OrganizationEntity org = orgRepo.findById(99001L).get();
        org.setThemeId(5001);
        orgRepo.save(org);

        String body = json().put("org_id", 99001).put("class_ids", asList(990011)).toString();
        HttpEntity<?> request =  getHttpEntity(body, "101112");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class",
                        POST, request, String.class);

        assertEquals(200,response.getStatusCodeValue());
        assertEquals(1, orgRepo.countThemeClassesByOrganizationId(99001L, 990011).intValue());
    }

    @Test
    public void addOrgThemeClassOrgHasNoThemeClassBefore() {
        OrganizationEntity org = orgRepo.findById(99002L).get();
        org.setThemeId(0);
        orgRepo.save(org);

        String body = json().put("org_id", 99002).put("class_ids", asList(990011)).toString();
        HttpEntity<?> request =  getHttpEntity(body, "101112");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class",
                        POST, request, String.class);

        assertEquals(200,response.getStatusCodeValue());
        assertEquals(1, orgRepo.countThemeClassesByOrganizationId(99002L, 990011).intValue());
    }


    @Test
    public void changeOrgTheme() {

        String body = json().put("org_id", 99001).put("class_ids", asList(990011, 990012)).toString();
        HttpEntity<?> request =  getHttpEntity(body, "101112");
        template.exchange("/organization/themes/class", POST, request, String.class);

        body = "{\"theme_id\":5001}";
        request =  getHttpEntity(body,"131415");

        ResponseEntity<String> response =
                template.exchange("/organization/themes", POST, request, String.class);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(orgThemeSettingsRepo.existsByOrganizationEntity_IdAndThemeId(99001L, 5001));
        
        assertTrue(orgRepo.existsByIdAndThemeId(99001L, 5001));
    }


    @Test
    public void updateOrgThemeSettings() {
        JSONObject body = json().put("theme_id", 5003)
                                .put("settings", "new settings");
        HttpEntity<?> request =  getHttpEntity(body.toString(),"131415");

        ResponseEntity<String> response =
                template.exchange("/organization/themes", POST, request, String.class);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("new settings",
                orgThemeSettingsRepo.findByOrganizationEntity_IdAndThemeId(99001L, 5003).get().getSettings());

    }


    @Test
    public void changeOrgThemeInvalidThemeId() {
        String body = "{\"theme_id\":5006}";
        HttpEntity<?> request =  getHttpEntity(body,"131415");

        ResponseEntity<String> response =
                template.exchange("/organization/themes", POST, request, String.class);

        assertEquals(406, response.getStatusCodeValue());
    }

    @Test
    public void changeOrgThemeNotPermittedThemeId() {
        String body = "{\"theme_id\":5002}";
        HttpEntity<?> request =  getHttpEntity(body,"131415");

        ResponseEntity<String> response =
                template.exchange("/organization/themes", POST, request, String.class);

        assertEquals(406, response.getStatusCodeValue());
    }


    @Test
    public void changeOrgThemeInvalidToken() {
        String body = "{\"theme_id\":5001}";
        HttpEntity<?> request =  getHttpEntity(body,"1314515");

        ResponseEntity<String> response =
                template.exchange("/organization/themes", POST, request, String.class);

        assertEquals(401, response.getStatusCodeValue());
    }


    @Test
    public void changeOrgThemeUnauthenticatedToken() {
        String body = "{\"theme_id\":5001}";
        HttpEntity<?> request =  getHttpEntity(body,"101112");

        ResponseEntity<String> response =
                template.exchange("/organization/themes", POST, request, String.class);

        assertEquals(403, response.getStatusCodeValue());
    }


    @Test
    public void changeOrgThemeNotSameOrg() {
        String body = "{\"theme_id\":5001}";
        HttpEntity<?> request =  getHttpEntity(body,"161718");

        ResponseEntity<String> response =
                template.exchange("/organization/themes", POST, request, String.class);

        assertEquals(406, response.getStatusCodeValue());
    }


    @Test
    public void getOrganizationThemes() {
        HttpEntity<?> request =  getHttpEntity("131415");
        ResponseEntity<List> response =
                template.exchange("/organization/themes", GET, request, List.class);

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
        System.out.println(response.getBody().toString());
        assertEquals(1, response.getBody().size());
    }
}
