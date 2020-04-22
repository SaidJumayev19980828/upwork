import com.nasnav.NavBox;
import com.nasnav.dao.OrganizationThemeSettingsRepository;
import com.nasnav.dao.ThemeClassRepository;
import com.nasnav.dao.ThemesRepository;
import com.nasnav.persistence.ThemeClassEntity;
import com.nasnav.persistence.ThemeEntity;
import com.nasnav.response.ThemeResponse;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Themes_API_Test_Data_Insert.sql"})
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


    @Test
    public void getThemesClasses() {
        HttpEntity<?> request = getHttpEntity("101112");
        ResponseEntity<List> response = template.exchange("/admin/themes/class",
                HttpMethod.GET, request, List.class);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertTrue(!response.getBody().isEmpty());
        Assert.assertEquals(2, response.getBody().size());
    }


    @Test
    public void createThemeClassSuccess() {
        String body = "{\"name\": \"theme_class_1\"}";
        HttpEntity<?> request =  getHttpEntity(body,"101112");
        ResponseEntity<ThemeResponse> response = template.exchange("/admin/themes/class",
                HttpMethod.POST, request, ThemeResponse.class);

        Assert.assertEquals(200,response.getStatusCodeValue());
        Assert.assertTrue(themeClassRepo.existsById(response.getBody().getId()));
        themeClassRepo.deleteById(response.getBody().getId()); //TODO: >>> why the delete?
    }


    @Test
    public void updateThemeClassSuccess() {
    	//TODO: >>> create jsons using JSONObject or TestCommons.json(), that makes them more maintainable and readable, it is hard to maintain strings.
        String body = "{\"id\": 990011,\"name\": \"new name\"}"; 
        HttpEntity<?> request =  getHttpEntity(body,"101112");
        ResponseEntity<ThemeResponse> response = template.exchange("/admin/themes/class",
                HttpMethod.POST, request, ThemeResponse.class); //TODO: >>> static import for POST 

        Assert.assertEquals(200,response.getStatusCodeValue());
        Assert.assertEquals("new name",themeClassRepo.findById(response.getBody().getId()).get().getName());
    }


    @Test
    public void createThemeClassInvalidToken() {
    	//TODO: >>> create jsons using JSONObject or TestCommons.json(), that makes them more maintainable and readable, it is hard to maintain strings.
        String body = "{\"name\": \"theme_class_1\"}";
        HttpEntity<?> request =  getHttpEntity(body,"1011123");
        ResponseEntity<ThemeResponse> response = template.exchange("/admin/themes/class",
                HttpMethod.POST, request, ThemeResponse.class);

        Assert.assertEquals(401,response.getStatusCodeValue());
    }


    @Test
    public void createThemeClassUnauthorizedToken() {
    	//TODO: >>> create jsons using JSONObject or TestCommons.json(), that makes them more maintainable and readable, it is hard to maintain strings.
        String body = "{\"name\": \"theme_class_1\"}";
        HttpEntity<?> request =  getHttpEntity(body,"131415");
        ResponseEntity<ThemeResponse> response = template.exchange("/admin/themes/class",
                HttpMethod.POST, request, ThemeResponse.class);

        Assert.assertEquals(403,response.getStatusCodeValue());
    }


    @Test
    public void createThemeClassInvalidId() {
    	//TODO: >>> create jsons using JSONObject or TestCommons.json(), that makes them more maintainable and readable, it is hard to maintain strings.
        String body = "{\"id\":-1, \"name\": \"theme_class_1\"}";
        HttpEntity<?> request =  getHttpEntity(body,"101112");
        ResponseEntity<ThemeResponse> response = template.exchange("/admin/themes/class",
                HttpMethod.POST, request, ThemeResponse.class);

        Assert.assertEquals(406,response.getStatusCodeValue());
    }


    @Test
    public void deleteThemesAndThemeClass() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<String> response = template.exchange("/admin/themes?id=5003",
                HttpMethod.DELETE, request, String.class);

        response = template.exchange("/admin/themes/class?id=990012",
                HttpMethod.DELETE, request, String.class);

        Assert.assertTrue(!themesRepo.existsById(5003));
        Assert.assertTrue(!themeClassRepo.existsById(990012));
    }


    @Test
    public void deleteThemesClassInvalidToken() {
        HttpEntity<?> request =  getHttpEntity("1011122");
        ResponseEntity<String> response = template.exchange("/admin/themes/class?id=990011",
                HttpMethod.DELETE, request, String.class);

        Assert.assertEquals(401, response.getStatusCodeValue());
    }


    @Test
    public void deleteThemesClassUnauthenticatedToken() {
        HttpEntity<?> request =  getHttpEntity("131415");
        ResponseEntity<String> response = template.exchange("/admin/themes/class?id=990011",
                HttpMethod.DELETE, request, String.class);

        Assert.assertEquals(403, response.getStatusCodeValue());
    }


    @Test
    public void deleteThemeClassUsedId() {
        HttpEntity<?> request =  getHttpEntity("101112");

        ResponseEntity<String> response = template.exchange("/admin/themes/class?id=990013",
                HttpMethod.DELETE, request, String.class);

        Assert.assertEquals(406, response.getStatusCodeValue());
    }

    @Test
    public void getThemes() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<List> response = template.exchange("/admin/themes",
                HttpMethod.GET, request, List.class);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertTrue(!response.getBody().isEmpty());
        Assert.assertEquals(3, response.getBody().size());
    }


    @Test
    public void createThemeSuccess() {
    	//TODO: >>> create jsons using JSONObject or TestCommons.json(), that makes them more maintainable and readable, it is hard to maintain strings.
        String body = "{\"name\": \"theme_1\", \"theme_class_id\": 990011}";
        HttpEntity<?> request =  getHttpEntity(body,"101112");
        ResponseEntity<ThemeResponse> response = template.exchange("/admin/themes",
                HttpMethod.POST, request, ThemeResponse.class);

        Assert.assertEquals(200,response.getStatusCodeValue());
        Assert.assertTrue(themesRepo.existsById(response.getBody().getId()));
        themesRepo.deleteById(response.getBody().getId());
    }


    @Test
    public void updateThemeSuccess() {
        String body = "{\"id\": 5002,\"name\": \"new theme name\", \"preview_image\": \"new theme image\"," +
                      "\"default_settings\": \"new theme settings\",\"theme_class_id\": 990012}";
        HttpEntity<?> request =  getHttpEntity(body,"101112");
        ResponseEntity<ThemeResponse> response = template.exchange("/admin/themes",
                HttpMethod.POST, request, ThemeResponse.class);

        Assert.assertEquals(200,response.getStatusCodeValue());

        ThemeEntity entity = themesRepo.findById(response.getBody().getId()).get();

        Assert.assertEquals("new theme name", entity.getName());
        Assert.assertEquals("new theme image", entity.getPreviewImage());
        Assert.assertEquals("new theme settings", entity.getDefaultSettings());
        Assert.assertEquals(990012, entity.getThemeClassEntity().getId().intValue());
    }


    @Test
    public void createThemeInvalidToken() {
    	//TODO: >>> create jsons using JSONObject or TestCommons.json(), that makes them more maintainable and readable, it is hard to maintain strings.
        String body = "{\"name\": \"theme_1\", \"theme_class_id\": 990011}";
        HttpEntity<?> request =  getHttpEntity(body,"1011122");
        ResponseEntity<ThemeResponse> response = template.exchange("/admin/themes",
                HttpMethod.POST, request, ThemeResponse.class);

        Assert.assertEquals(401,response.getStatusCodeValue());
    }


    @Test
    public void createThemeUnauthenticatedToken() {
        String body = "{\"name\": \"theme_1\", \"theme_class_id\": 990011}";
        HttpEntity<?> request =  getHttpEntity(body,"131415");
        ResponseEntity<ThemeResponse> response = template.exchange("/admin/themes",
                HttpMethod.POST, request, ThemeResponse.class);

        Assert.assertEquals(403,response.getStatusCodeValue());
    }


    @Test
    public void createThemeInvalidId() {
        String body = "{\"id\": -1,\"name\": \"theme_1\", \"theme_class_id\": 990011}";
        HttpEntity<?> request =  getHttpEntity(body,"101112");
        ResponseEntity<ThemeResponse> response = template.exchange("/admin/themes",
                HttpMethod.POST, request, ThemeResponse.class);

        Assert.assertEquals(406,response.getStatusCodeValue());
    }


    @Test
    public void createThemeInvalidClassId() {
        String body = "{\"name\": \"theme_1\", \"theme_class_id\": -1}";
        HttpEntity<?> request =  getHttpEntity(body,"101112");
        ResponseEntity<ThemeResponse> response = template.exchange("/admin/themes",
                HttpMethod.POST, request, ThemeResponse.class);

        Assert.assertEquals(406,response.getStatusCodeValue());
    }


    @Test
    public void createThemeMissingClassId() {
        String body = "{\"name\": \"theme_1\"}";
        HttpEntity<?> request =  getHttpEntity(body,"101112");
        ResponseEntity<ThemeResponse> response = template.exchange("/admin/themes",
                HttpMethod.POST, request, ThemeResponse.class);

        Assert.assertEquals(406,response.getStatusCodeValue());
    }


    @Test
    public void deleteThemesInvalidToken() {
        HttpEntity<?> request =  getHttpEntity("1011122");
        ResponseEntity<String> response = template.exchange("/admin/themes?id=5004",
                HttpMethod.DELETE, request, String.class);

        Assert.assertEquals(401, response.getStatusCodeValue());
    }


    @Test
    public void deleteThemesUnauthenticatedToken() {
        HttpEntity<?> request =  getHttpEntity("131415");
        ResponseEntity<String> response = template.exchange("/admin/themes?id=5001",
                HttpMethod.DELETE, request, String.class);

        Assert.assertEquals(403, response.getStatusCodeValue());
    }


    @Test
    public void deleteThemeInvalidId() {
        HttpEntity<?> request =  getHttpEntity("101112");

        ResponseEntity<String> response = template.exchange("/admin/themes?id=990013",
                HttpMethod.DELETE, request, String.class);

        Assert.assertEquals(406, response.getStatusCodeValue());
    }


    @Test
    public void getOrganizationThemesClasses() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<List> response = template.exchange("/organization/themes/class?org_id=99001",
                HttpMethod.GET, request, List.class);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertTrue(!response.getBody().isEmpty());
        Assert.assertEquals(1, response.getBody().size());
    }


    @Test
    public void getOrganizationThemesClassesInvalidOrg() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<String> response = template.exchange("/organization/themes/class?org_id=99003",
                HttpMethod.GET, request, String.class);

        Assert.assertEquals(404, response.getStatusCodeValue());
    }


    @Test
    public void getOrganizationThemesClassesInvalidToken() {
        HttpEntity<?> request =  getHttpEntity("10111f2");
        ResponseEntity<String> response = template.exchange("/organization/themes/class?org_id=99003",
                HttpMethod.GET, request, String.class);

        Assert.assertEquals(401, response.getStatusCodeValue());
    }


    @Test
    public void getOrganizationThemesClassesUnauthenticatedToken() {
        HttpEntity<?> request =  getHttpEntity("131415");
        ResponseEntity<String> response = template.exchange("/organization/themes/class?org_id=99003",
                HttpMethod.GET, request, String.class);

        Assert.assertEquals(403, response.getStatusCodeValue());
    }


    @Test
    public void assignOrganizationThemeClass() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class?org_id=99001&class_id=990012",
                HttpMethod.POST, request, String.class);

        Assert.assertEquals(200,response.getStatusCodeValue());
    }


    @Test
    public void assignOrganizationThemeClassInvalidOrg() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class?org_id=99003&class_id=990012",
                        HttpMethod.POST, request, String.class);

        Assert.assertEquals(404,response.getStatusCodeValue());
    }


    @Test
    public void assignOrganizationThemeClassInvalidClass() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class?org_id=99003&class_id=990014",
                        HttpMethod.POST, request, String.class);

        Assert.assertEquals(404,response.getStatusCodeValue());
    }


    @Test
    public void assignOrganizationThemeClassInvalidToken() {
        HttpEntity<?> request =  getHttpEntity("1011d12");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class?org_id=99001&class_id=990012",
                        HttpMethod.POST, request, String.class);

        Assert.assertEquals(401,response.getStatusCodeValue());
    }


    @Test
    public void assignOrganizationThemeClassUnauthenticatedToken() {
        HttpEntity<?> request =  getHttpEntity("131415");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class?org_id=99001&class_id=990012",
                        HttpMethod.POST, request, String.class);

        Assert.assertEquals(403,response.getStatusCodeValue());
    }


    @Test
    public void assignOrganizationThemeClassExistingClass() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class?org_id=99001&class_id=990011",
                        HttpMethod.POST, request, String.class);

        Assert.assertEquals(406,response.getStatusCodeValue());
    }
    
    
    
    //TODO: >>> need a test for an admin trying to set the theme for another organization
    //TODO: >>> need test for changing the organization theme setting


    @Test
    public void deleteOrganizationThemeClass() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class?org_id=99001&class_id=990011",
                        HttpMethod.DELETE, request, String.class);

        Assert.assertEquals(200, response.getStatusCodeValue());
    }


    @Test
    public void deleteOrganizationThemeClassInvalidOrg() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class?org_id=990013&class_id=990011",
                        HttpMethod.DELETE, request, String.class);

        Assert.assertEquals(404, response.getStatusCodeValue());
    }


    @Test
    public void deleteOrganizationThemeClassInvalidClass() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class?org_id=99001&class_id=9900131",
                        HttpMethod.DELETE, request, String.class);

        Assert.assertEquals(406, response.getStatusCodeValue());
    }


    @Test
    public void deleteOrganizationThemeClassInvalid() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class?org_id=99001&class_id=990013",
                        HttpMethod.DELETE, request, String.class);

        Assert.assertEquals(406, response.getStatusCodeValue());
    }


    @Test
    public void deleteOrganizationThemeClassInvalidToken() {
        HttpEntity<?> request =  getHttpEntity("1011d12");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class?org_id=99001&class_id=990012",
                        HttpMethod.DELETE, request, String.class);

        Assert.assertEquals(401,response.getStatusCodeValue());
    }


    @Test
    public void deleteOrganizationThemeClassUnauthenticatedToken() {
        HttpEntity<?> request =  getHttpEntity("131415");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class?org_id=99001&class_id=990012",
                        HttpMethod.DELETE, request, String.class);

        Assert.assertEquals(403,response.getStatusCodeValue());
    }


    @Test
    public void deleteOrganizationThemeClassNonExistingLink() {
        HttpEntity<?> request =  getHttpEntity("101112");
        ResponseEntity<String> response =
                template.exchange("/organization/themes/class?org_id=99001&class_id=990012",
                        HttpMethod.DELETE, request, String.class);

        Assert.assertEquals(406, response.getStatusCodeValue());
    }


    @Test
    public void changeOrgTheme() {
        String body = "{\"theme_id\":5001}";
        HttpEntity<?> request =  getHttpEntity(body,"131415");

        ResponseEntity<String> response =
                template.exchange("/organization/themes", HttpMethod.POST, request, String.class);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertTrue(orgThemeSettingsRepo.existsByOrganizationEntity_IdAndThemeId(99001L, 5001));
        
        //TODO: >>> need to check if the theme was assigned to the organization entity
    }


    @Test
    public void changeOrgThemeInvalidThemeId() {
        String body = "{\"theme_id\":5006}";
        HttpEntity<?> request =  getHttpEntity(body,"131415");

        ResponseEntity<String> response =
                template.exchange("/organization/themes", HttpMethod.POST, request, String.class);

        Assert.assertEquals(406, response.getStatusCodeValue());
    }

    @Test
    public void changeOrgThemeNotPermittedThemeId() {
        String body = "{\"theme_id\":5003}";
        HttpEntity<?> request =  getHttpEntity(body,"131415");

        ResponseEntity<String> response =
                template.exchange("/organization/themes", HttpMethod.POST, request, String.class);

        Assert.assertEquals(406, response.getStatusCodeValue());
    }


    @Test
    public void changeOrgThemeInvalidToken() {
        String body = "{\"theme_id\":5001}";
        HttpEntity<?> request =  getHttpEntity(body,"1314515");

        ResponseEntity<String> response =
                template.exchange("/organization/themes", HttpMethod.POST, request, String.class);

        Assert.assertEquals(401, response.getStatusCodeValue());
    }


    @Test
    public void changeOrgThemeUnauthenticatedToken() {
        String body = "{\"theme_id\":5001}";
        HttpEntity<?> request =  getHttpEntity(body,"101112");

        ResponseEntity<String> response =
                template.exchange("/organization/themes", HttpMethod.POST, request, String.class);

        Assert.assertEquals(403, response.getStatusCodeValue());
    }
}
