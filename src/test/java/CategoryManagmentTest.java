import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.nasnav.AppConfig;
import com.nasnav.NavBox;
import com.nasnav.controller.AdminController;
import com.nasnav.dao.CategoryRepository;
import com.nasnav.dao.TagsRepository;
import com.nasnav.dto.TagsRepresentationObject;
import com.nasnav.response.CategoryResponse;
import com.nasnav.service.CategoryService;
import com.nasnav.test.commons.TestCommons;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class CategoryManagmentTest {

    private MockMvc mockMvc;
    @Mock
    private AdminController adminController;
    @Autowired
    private AppConfig config;
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private DataSource datasource;

    @Autowired
    private CategoryService service;

    @Autowired
    private TagsRepository orgTagsRepo;

    @Before
    public void setup() {
        config.mailDryRun = true;
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }


    
    
    
    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_2.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void getCategoryListDifferentFiltersTest() {
        // with no filters
        ResponseEntity<String> response = template.getForEntity("/navbox/categories", String.class);
        JSONArray json = new JSONArray(response.getBody());
        assertEquals("there are 4 Categories in total", 7, json.length());

        //TODO Modify category listing test after getting categories by organization
        // with organization_id = 99001
        response = template.getForEntity("/navbox/categories?org_id=99001", String.class);
        json = new JSONArray(response.getBody());
        assertEquals("there are 3 Categories used by org 99001", 4, json.length());

        // with organization_id = 99002
        response = template.getForEntity("/navbox/categories?org_id=99002", String.class);
        json = new JSONArray(response.getBody());
        assertEquals("there are 2 Categories used by org 99002", 2, json.length());
        
        // with category_id = 201
        response = template.getForEntity("/navbox/categories?category_id=201", String.class);
        json = new JSONArray(response.getBody());
        assertEquals("category 201 has 2 children .. 3 total Categories", 3, json.length());

        // with category_id = 202
        response = template.getForEntity("/navbox/categories?category_id=202", String.class);
        json = new JSONArray(response.getBody());
        assertEquals("category 202 has 1 children .. 2 total Categories", 2, json.length());

        // with category_id = 203
        response = template.getForEntity("/navbox/categories?category_id=203", String.class);
        json = new JSONArray(response.getBody());
        assertEquals("category 203 has no children .. 1 total Category", 1, json.length());
        //checking returned json properties
        assertTrue(response.getBody().contains("\"id\":203"));
        assertTrue(response.getBody().contains("\"name\":\"category_3\""));
        assertTrue(response.getBody().contains("\"logo_url\":\"logo_3\""));
        assertTrue(response.getBody().contains("\"parent_id\":201"));
        assertTrue(response.getBody().contains("\"p_name\":\"category-3\""));
    }
    
    
    

    @Test
    public void createCategorySuccessTest() {
        String body = "{\"logo\":\"categories/logos/564961451_56541.jpg\",\"name\":\"Perfumes\", \"operation\": \"create\",\"parent_id\": 202}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<CategoryResponse> response = template.postForEntity("/admin/category", json, CategoryResponse.class);
        categoryRepository.deleteById(response.getBody().getCategoryId());
        Assert.assertTrue(200 == response.getStatusCode().value());
    }

    @Test
    public void createCategoryMissingNameTest() {
        String body = "{\"logo\":\"categories/logos/564961451_56541.jpg\", \"operation\": \"create\"}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<Object> response = template.postForEntity("/admin/category", json, Object.class);
        Assert.assertTrue(406 == response.getStatusCode().value());
    }

    @Test
    public void createCategoryInvalidNameTest() {
        String body = "{\"logo\":\"categories/logos/564961451_56541.jpg\",\"name\":\"123Perfumes#$\", \"operation\": \"create\"}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<Object> response = template.postForEntity("/admin/category", json, Object.class);
        Assert.assertTrue(406 == response.getStatusCode().value());
    }

    @Test
    public void createCategoryNonExistingParentTest() {
        String body = "{\"logo\":\"categories/logos/564961451_56541.jpg\",\"name\":\"Perfumes\", \"operation\": \"create\",\"parent_id\": 200}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<Object> response = template.postForEntity("/admin/category", json, Object.class);
        Assert.assertTrue(406 == response.getStatusCode().value());
    }

    @Test
    public void updateCategorySuccessTest() {
        String body = "{\"id\":201,\"logo\":\"categories/logos/1111111111.jpg\",\"name\":\"Makeups\", \"operation\": \"update\"}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<Object> response = template.postForEntity("/admin/category", json, Object.class);
        Assert.assertTrue(200 == response.getStatusCode().value());
    }

    @Test
    public void updateCategoryNoIdTest() {
        String body = "{\"logo\":\"categories/logos/1111111111.jpg\",\"name\":\"Makeups\", \"operation\": \"update\"}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<Object> response = template.postForEntity("/admin/category", json, Object.class);
        Assert.assertTrue(406 == response.getStatusCode().value());
    }

    @Test
    public void updateCategoryNoEntityTest() {
        String body = "{\"id\":2000009,\"logo\":\"categories/logos/1111111111.jpg\",\"name\":\"Makeups\", \"operation\": \"update\"}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<Object> response = template.postForEntity("/admin/category", json, Object.class);
        Assert.assertTrue(406 == response.getStatusCode().value());
    }

    @Test
    public void updateCategoryInvalidNameTest() {
        String body = "{\"id\":202,\"logo\":\"categories/logos/564961451_56541.jpg\",\"name\":\"123Perfumes#$\", \"operation\": \"update\"}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<Object> response = template.postForEntity("/admin/category", json, Object.class);
        Assert.assertTrue(406 == response.getStatusCode().value());
    }

    @Test
    public void updateCategoryNonExistingParentTest() {
        String body = "{\"id\":202,\"logo\":\"categories/logos/564961451_56541.jpg\",\"name\":\"Perfumes\", \"operation\": \"create\",\"parent_id\": 200}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<Object> response = template.postForEntity("/admin/category", json, Object.class);
        Assert.assertTrue(406 == response.getStatusCode().value());
    }

    @Test
    public void deleteCategorySuccessTest() {
        //create a category
        String body = "{\"name\":\"Perfumes\", \"operation\": \"create\",\"parent_id\": 202}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<CategoryResponse> response = template.postForEntity("/admin/category", json, CategoryResponse.class);
        Assert.assertTrue(200 == response.getStatusCode().value());
        //delete created category
        HttpHeaders header = TestCommons.getHeaders("abcdefg");
        ResponseEntity<String> deleteResponse = template.exchange("/admin/category?category_id=" + response.getBody().getCategoryId(),
                HttpMethod.DELETE, new HttpEntity<>(header), String.class);
        Assert.assertTrue(200 == deleteResponse.getStatusCode().value());
    }

    @Test
    public void deleteCategoryMissingIdTest() {
        HttpHeaders header = TestCommons.getHeaders("abcdefg");
        ResponseEntity<String> deleteResponse = template.exchange("/admin/category",
                HttpMethod.DELETE, new HttpEntity<>(header), String.class);
        Assert.assertTrue(400 == deleteResponse.getStatusCode().value());
    }

    @Test
    public void deleteCategoryMissingCategoryTest() {
        HttpHeaders header = TestCommons.getHeaders("abcdefg");
        ResponseEntity<String> deleteResponse = template.exchange("/admin/category?category_id=209",
                HttpMethod.DELETE, new HttpEntity<>(header), String.class);
        Assert.assertTrue(406 == deleteResponse.getStatusCode().value());
    }

    @Test
    public void deleteCategoryUsedCategoryTest() {
        HttpHeaders header = TestCommons.getHeaders("abcdefg");
        ResponseEntity<String> deleteResponse = template.exchange("/admin/category?category_id=202",
                HttpMethod.DELETE, new HttpEntity<>(header), String.class);
        Assert.assertTrue(409 == deleteResponse.getStatusCode().value());
    }


    @Test
    public void categoriesDAGCycles() throws Exception {
        DirectedAcyclicGraph graph = createGraph();

        CycleDetector<String, DefaultEdge> cycleDetector = new CycleDetector<String, DefaultEdge>(graph);
        Set<String> cycleVertices = cycleDetector.findCycles();

        assertTrue(!cycleDetector.detectCycles());
        assertTrue(cycleVertices.size() == 0);
    }

    @Test
    public void categoriesDAGCreateCycleError() throws Exception {
        DirectedAcyclicGraph graph = createGraph();
        try {
            graph.addEdge("Tag#2.2", "Tag#2.1");
            graph.addEdge("Tag#2.1", "Tag#2");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void getTags() {
        List<TagsRepresentationObject> tags = service.getOrganizationTags(99001L, null);
        assertTrue(!tags.isEmpty());
        System.out.println(tags.toString());
    }

    @Test
    public void getTagsWithCategory() {
        List<TagsRepresentationObject> tags = service.getOrganizationTags(99001L, "category_1");
        assertTrue(tags.size() == 1);
        System.out.println(tags.toString());
    }

    @Test
    public void getTagsInvalidCategory() {
        List<TagsRepresentationObject> tags = service.getOrganizationTags(99001L, "invalid Category");
        assertTrue(tags.isEmpty());
    }

    @Test
    public void createOrgTagsSuccess() {
        String body = "{\"operation\":\"create\",  \"category_id\":207, \"name\":\"org1_tag_7\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        JSONObject result = new JSONObject(response.getBody());
        orgTagsRepo.delete(orgTagsRepo.findById(result.getLong("tag_id")).get());

        Assert.assertTrue(result.getBoolean("success") == true);
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void createOrgTagMissingOperation() {
        String body = "{\"category_id\":201, \"alias\":\"org1_tag_1\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createOrgTagInvalidOperation() {
        String body = "{\"operation\":\"invalid operation\",  \"category_id\":2001, \"alias\":\"org1_tag_1\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createOrgTagMissingTagId() {
        String body = "{\"operation\":\"create\", \"alias\":\"org1_tag_1\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createOrgTagInvalidTagId() {
        String body = "{\"operation\":\"create\", \"category_id\":2008, \"alias\":\"org1_tag_1\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateOrgTagsSuccess() {
        String body = "{\"operation\":\"update\",  \"id\":5001, \"name\":\"org1_tag_1\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        JSONObject result = new JSONObject(response.getBody());

        Assert.assertTrue(result.getBoolean("success") == true);
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void updateOrgTagsMissingId() {
        String body = "{\"operation\":\"update\", \"alias\":\"org1_tag_1\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateOrgTagsInvalidId() {
        String body = "{\"operation\":\"update\", \"id\":5008, \"alias\":\"org1_tag_1\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createTagsLinkSuccess() {
        String body = "{\"parent_id\":5001, \"children_ids\":[5004, 5005]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag/link", json, String.class);

        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void createTagsLinkMissingChildId() {
        String body = "{\"parent_id\":5003}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag/link", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createTagsLinkInvalidChildId() {
        String body = "{\"parent_id\":5003, \"children_ids\":[5007]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag/link", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void deleteTopLevelTagsLinkMissingId() {
        String body = "{\"parent_id\":null, \"children_ids\":null}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.exchange("/organization/tag/link", HttpMethod.DELETE, json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void deleteTopLevelTagsLinkInvalidId() {
        String body = "{\"parent_id\":null, \"children_ids\":[5002]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.exchange("/organization/tag/link", HttpMethod.DELETE, json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void deleteTagsLinkMissingChildId() {
        String body = "{\"parent_id\":5001}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.exchange("/organization/tag/link", HttpMethod.DELETE, json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void deleteTagsLinkInvalidChildId() {
        String body = "{\"parent_id\":5001, \"children_ids\":[5008]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.exchange("/organization/tag/link", HttpMethod.DELETE, json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void deleteTagsLinkNonExistingLink() {
        String body = "{\"parent_id\":5001, \"children_ids\":[5004]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.exchange("/organization/tag/link", HttpMethod.DELETE, json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void addProductTagsSuccess() {
        String body = "{\"products_ids\":[1001, 1002], \"tags_ids\":[5004, 5006]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/product/tag", json, String.class);

        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void addProductTagsMissingProducts() {
        String body = "{\"tags_ids\":[5004, 5006]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/product/tag", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void addProductTagsMissingTags() {
        String body = "{\"products_ids\":[1001, 1002]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/product/tag", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void addProductTagsInvalidProduct() {
        String body = "{\"products_ids\":[1009], \"tags_ids\":[5004, 5006]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/product/tag", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void addProductTagsInvalidTag() {
        String body = "{\"products_ids\":[1002], \"tags_ids\":[5009, 5006]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/product/tag", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void deleteProductTagsSuccess() {
        String body = "{\"products_ids\":[1006], \"tags_ids\":[5006]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.exchange("/product/tag", HttpMethod.DELETE, json, String.class);

        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void deleteProductTagsNonExistingLink() {
        String body = "{\"products_ids\":[1005], \"tags_ids\":[5006]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.exchange("/product/tag", HttpMethod.DELETE, json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void deleteProductTagsMissingProductId() {
        String body = "{\"tags_ids\":[5006]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.exchange("/product/tag", HttpMethod.DELETE, json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void deleteProductTagsMissingTagId() {
        String body = "{\"products_ids\":[1005], \"tags_ids\":[5006]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.exchange("/product/tag", HttpMethod.DELETE, json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }


    private JSONArray getChildren(JSONObject k, Graph g, String parent) {
        JSONArray l = new JSONArray();
        for (Object p : Graphs.successorListOf(g, parent)) {
            if (Graphs.successorListOf(g, p.toString()).isEmpty()) {
                l.put(p.toString());
            } else {
                JSONObject o = new JSONObject();
                o.put(p.toString(), getChildren(k, g, p.toString()));
                l.put(o);
                k.put(parent, l);
            }
        }
        return l;
    }

    private DirectedAcyclicGraph createGraph() {
        DirectedAcyclicGraph<String, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        graph.addVertex("Tag#1");
        graph.addVertex("Tag#2");
        graph.addVertex("Tag#1.1");
        graph.addVertex("Tag#1.2");
        graph.addVertex("Tag#2.1");
        graph.addVertex("Tag#2.2");
        graph.addVertex("Tag#1.1.1");

        graph.addEdge("Tag#1", "Tag#1.1");
        graph.addEdge("Tag#1", "Tag#1.2");
        graph.addEdge("Tag#1", "Tag#2.1");
        graph.addEdge("Tag#1.1", "Tag#1.1.1");
        graph.addEdge("Tag#2", "Tag#2.1");
        graph.addEdge("Tag#2", "Tag#2.2");
        graph.addEdge("Tag#2", "Tag#1.2");

        return graph;
    }

}
