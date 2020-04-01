import static com.nasnav.test.commons.TestCommons.getHeaders;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static com.nasnav.test.commons.TestCommons.jsonArray;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.AppConfig;
import com.nasnav.NavBox;
import com.nasnav.controller.AdminController;
import com.nasnav.dao.CategoryRepository;
import com.nasnav.dao.TagGraphEdgesRepository;
import com.nasnav.dao.TagGraphNodeRepository;
import com.nasnav.dao.TagsRepository;
import com.nasnav.dto.TagsRepresentationObject;
import com.nasnav.dto.TagsTreeNodeDTO;
import com.nasnav.persistence.TagsEntity;
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

    @SuppressWarnings("unused")
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
    private CategoryService service;

    @Autowired
    private TagsRepository orgTagsRepo;

    @Autowired
    private TagGraphEdgesRepository tagEdgesRepo;
    
    @Autowired
    private TagGraphNodeRepository tagNodesRepo;

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
        assertTrue(200 == response.getStatusCode().value());
    }

    
    
    
    @Test
    public void createCategoryMissingNameTest() {
        String body = "{\"logo\":\"categories/logos/564961451_56541.jpg\", \"operation\": \"create\"}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<Object> response = template.postForEntity("/admin/category", json, Object.class);
        assertTrue(406 == response.getStatusCode().value());
    }

    
    
    
    @Test
    public void createCategoryInvalidNameTest() {
        String body = "{\"logo\":\"categories/logos/564961451_56541.jpg\",\"name\":\"123Perfumes#$\", \"operation\": \"create\"}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<Object> response = template.postForEntity("/admin/category", json, Object.class);
        assertTrue(406 == response.getStatusCode().value());
    }

    
    
    
    @Test
    public void createCategoryNonExistingParentTest() {
        String body = "{\"logo\":\"categories/logos/564961451_56541.jpg\",\"name\":\"Perfumes\", \"operation\": \"create\",\"parent_id\": 200}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<Object> response = template.postForEntity("/admin/category", json, Object.class);
        assertTrue(406 == response.getStatusCode().value());
    }

    
    
    
    @Test
    public void updateCategorySuccessTest() {
        String body = "{\"id\":201,\"logo\":\"categories/logos/1111111111.jpg\",\"name\":\"Makeups\", \"operation\": \"update\"}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<Object> response = template.postForEntity("/admin/category", json, Object.class);
        assertTrue(200 == response.getStatusCode().value());
    }

    
    
    
    @Test
    public void updateCategoryNoIdTest() {
        String body = "{\"logo\":\"categories/logos/1111111111.jpg\",\"name\":\"Makeups\", \"operation\": \"update\"}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<Object> response = template.postForEntity("/admin/category", json, Object.class);
        assertTrue(406 == response.getStatusCode().value());
    }

    
    
    
    
    @Test
    public void updateCategoryNoEntityTest() {
        String body = "{\"id\":2000009,\"logo\":\"categories/logos/1111111111.jpg\",\"name\":\"Makeups\", \"operation\": \"update\"}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<Object> response = template.postForEntity("/admin/category", json, Object.class);
        assertTrue(406 == response.getStatusCode().value());
    }

    
    
    
    
    @Test
    public void updateCategoryInvalidNameTest() {
        String body = "{\"id\":202,\"logo\":\"categories/logos/564961451_56541.jpg\",\"name\":\"123Perfumes#$\", \"operation\": \"update\"}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<Object> response = template.postForEntity("/admin/category", json, Object.class);
        assertTrue(406 == response.getStatusCode().value());
    }

    
    
    
    @Test
    public void updateCategoryNonExistingParentTest() {
        String body = "{\"id\":202,\"logo\":\"categories/logos/564961451_56541.jpg\",\"name\":\"Perfumes\", \"operation\": \"create\",\"parent_id\": 200}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<Object> response = template.postForEntity("/admin/category", json, Object.class);
        assertTrue(406 == response.getStatusCode().value());
    }

    
    
    
    @Test
    public void deleteCategorySuccessTest() {
        //create a category
        String body = "{\"name\":\"Perfumes\", \"operation\": \"create\",\"parent_id\": 202}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<CategoryResponse> response = template.postForEntity("/admin/category", json, CategoryResponse.class);
        assertTrue(200 == response.getStatusCode().value());
        //delete created category
        HttpHeaders header = TestCommons.getHeaders("abcdefg");
        ResponseEntity<String> deleteResponse = template.exchange("/admin/category?category_id=" + response.getBody().getCategoryId(),
                HttpMethod.DELETE, new HttpEntity<>(header), String.class);
        assertTrue(200 == deleteResponse.getStatusCode().value());
    }

    
    
    
    
    @Test
    public void deleteCategoryMissingIdTest() {
        HttpHeaders header = TestCommons.getHeaders("abcdefg");
        ResponseEntity<String> deleteResponse = template.exchange("/admin/category",
                HttpMethod.DELETE, new HttpEntity<>(header), String.class);
        assertTrue(400 == deleteResponse.getStatusCode().value());
    }

    
    
    
    @Test
    public void deleteCategoryMissingCategoryTest() {
        HttpHeaders header = TestCommons.getHeaders("abcdefg");
        ResponseEntity<String> deleteResponse = template.exchange("/admin/category?category_id=209",
                HttpMethod.DELETE, new HttpEntity<>(header), String.class);
        assertTrue(406 == deleteResponse.getStatusCode().value());
    }

    
    
    
    @Test
    public void deleteCategoryUsedCategoryTest() {
        HttpHeaders header = getHeaders("abcdefg");
        ResponseEntity<String> deleteResponse = template.exchange("/admin/category?category_id=202",
                HttpMethod.DELETE, new HttpEntity<>(header), String.class);
        assertTrue(409 == deleteResponse.getStatusCode().value());
    }


    
   

    @SuppressWarnings("rawtypes")
	@Test
    public void getTags() {
        ResponseEntity<List> tags = template.getForEntity("/navbox/tags?org_id=99001", List.class);

        assertTrue(!tags.getBody().isEmpty());
        assertEquals(7, tags.getBody().size());
        System.out.println(tags.getBody().toString());
    }

    
    
    
    @SuppressWarnings("rawtypes")
    @Test
    public void getTagsTree() {
		ResponseEntity<List> response = template.getForEntity("/navbox/tagstree?org_id=99001", List.class);
        List treeRoot = response.getBody();
        assertTrue(!treeRoot.isEmpty());
        assertEquals(3, treeRoot.size());
    }
    
    
    
    
    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_4.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void getTagsTreeWithNodeHavingMultipleParents() throws JsonParseException, JsonMappingException, IOException {
		ResponseEntity<String> response = template.getForEntity("/navbox/tagstree?org_id=99001", String.class);
		String body = response.getBody();
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<List<TagsTreeNodeDTO>> typeReference = new TypeReference<List<TagsTreeNodeDTO>>(){};
		List<TagsTreeNodeDTO> tree = mapper.readValue(body, typeReference);
		long nodesNum = 
        		tree
        		.stream()
        		.filter(rootNode -> hasNodeWithId(rootNode, 50016L))
        		.count();
				
        assertTrue(!tree.isEmpty());        
        assertEquals(3, tree.size());
        assertEquals("The node with multiple parents should appear as child multiple times", 2L, nodesNum);
    }





	private boolean hasNodeWithId(TagsTreeNodeDTO node, Long id) {
		return node
				.children
				.stream()
				.anyMatch(child -> Objects.equals(child.getNodeId(), id));
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

        assertTrue(result.getBoolean("success") == true);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void createOrgTagMissingOperation() {
        String body = "{\"category_id\":201, \"alias\":\"org1_tag_1\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createOrgTagInvalidOperation() {
        String body = "{\"operation\":\"invalid operation\",  \"category_id\":2001, \"alias\":\"org1_tag_1\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createOrgTagMissingTagId() {
        String body = "{\"operation\":\"create\", \"alias\":\"org1_tag_1\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createOrgTagInvalidTagId() {
        String body = "{\"operation\":\"create\", \"category_id\":2008, \"alias\":\"org1_tag_1\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateOrgTagsSuccess() {
        String body = "{\"operation\":\"update\",  \"id\":5001, \"name\":\"org1_tag_1\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        JSONObject result = new JSONObject(response.getBody());

        assertTrue(result.getBoolean("success") == true);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void updateOrgTagGraphId() {
        String body = "{\"operation\":\"update\",  \"id\":5005, \"graph_id\":9}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        JSONObject result = new JSONObject(response.getBody());

        assertTrue(result.getBoolean("success") == true);
        assertEquals(200, response.getStatusCode().value());

        TagsEntity tag = orgTagsRepo.findById(5005L).get();
        assertEquals("for now graph_id values are either null or organization id", 99001, tag.getGraphId().intValue());


        body = "{\"operation\":\"update\",  \"id\":5005, \"graph_id\":null}";

        json = TestCommons.getHttpEntity(body,"hijkllm");
        response = template.postForEntity("/organization/tag", json, String.class);

        result = new JSONObject(response.getBody());

        assertTrue(result.getBoolean("success") == true);
        assertEquals(200, response.getStatusCode().value());

        tag = orgTagsRepo.findById(5005L).get();
        assertNull(tag.getGraphId());
    }

    @Test
    public void updateLinkedOrgTagNullGraphId() {
        String body = "{\"operation\":\"update\",  \"id\":5001, \"graph_id\":null}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateOrgTagsMissingId() {
        String body = "{\"operation\":\"update\", \"alias\":\"org1_tag_1\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateOrgTagsInvalidId() {
        String body = "{\"operation\":\"update\", \"id\":5008, \"alias\":\"org1_tag_1\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag", json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    
    
    
    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_3.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void createTagsTreeSuccess() {
    	long countNodesBefore =  tagNodesRepo.count();
    	long countEdgesBefore =  tagEdgesRepo.count();
    	
    	assertEquals("In this test, we assume that the nodes table is empty", 0L, countNodesBefore);
    	assertEquals("In this test, we assume that the edges table is empty", 0L, countEdgesBefore);
    	
    	String body = createValidTagTreeRequestBody();

        HttpEntity<Object> json = getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag/tree", json, String.class);

        long countNodesAfter =  tagNodesRepo.count();
    	long countEdgesAfter =  tagEdgesRepo.count();
    	
        assertEquals(200, response.getStatusCode().value());
        assertEquals(3L, countNodesAfter);
        assertEquals(1L, countEdgesAfter);
    }





	private String createValidTagTreeRequestBody() {
		return	json()
        		 .put("nodes"
        			, jsonArray()
        				 .put(createTagTreeNode(
        						 	5003L, 
        						 	asList(createTagTreeNode(5004L, null))))
        				 .put(createTagTreeNode(5005L, null))
        					    )        		 		
        		 .toString();
	}

    
    
    
    @Test
    public void createTagsTreeMissingChildId() { // creates top level tag (add graph_id to each parent)
        String body = "{\"tags_links\":[{\"tag_id\":5003}]}";

        HttpEntity<Object> json = getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag/tree", json, String.class);

        assertEquals(200, response.getStatusCode().value());
    }

    
    
    
    
    @Test
    public void createTagsTreeInvalidChildId() {
        String body = 
        		json()
        		 .put("nodes"
        			, jsonArray()
        				 .put(createTagTreeNode(
        						 	5003L, 
        						 	asList(createTagTreeNode(5008L, null))))
        					    )
        		 .toString();

        HttpEntity<Object> json = getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag/tree", json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }
    
    
    
    
    
    
    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_5.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void createTagsTreeTagWithNoCategory() {
        String body = 
        		json()
        		 .put("nodes"
        			, jsonArray()
        				 .put(createTagTreeNode(5003L, null)))
        		 .toString();

        HttpEntity<Object> json = getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag/tree", json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    
    
    
    
    private JSONObject createTagTreeNode(Long tagId, List<JSONObject> children) {
    	JSONArray childrenArr = new JSONArray();
    	ofNullable(children)
    	.orElse(emptyList())
    	.forEach(childrenArr::put);
    	return json()
    			.put("tag_id", tagId)
			    .put("children", childrenArr);
    }
    


    


    @Test
    public void addProductTagsSuccess() {
        String body = "{\"products_ids\":[1001, 1002], \"tags_ids\":[5004, 5006]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/product/tag", json, String.class);

        assertEquals(200, response.getStatusCode().value());
    }

    
    
    
    @Test
    public void addProductTagsMissingProducts() {
        String body = "{\"tags_ids\":[5004, 5006]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/product/tag", json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void addProductTagsMissingTags() {
        String body = "{\"products_ids\":[1001, 1002]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/product/tag", json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    
    
    
    @Test
    public void addProductTagsInvalidProduct() {
        String body = "{\"products_ids\":[1009], \"tags_ids\":[5004, 5006]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/product/tag", json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    
    
    
    @Test
    public void addProductTagsInvalidTag() {
        String body = "{\"products_ids\":[1002], \"tags_ids\":[5009, 5006]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/product/tag", json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    
    
    
    @Test
    public void deleteProductTagsSuccess() {
        String body = "{\"products_ids\":[1006], \"tags_ids\":[5006]}";

        HttpEntity<Object> json = getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.exchange("/product/tag", DELETE, json, String.class);

        assertEquals(200, response.getStatusCode().value());
    }

    
    
    
    @Test
    public void deleteProductTagsNonExistingLink() {
        String body = "{\"products_ids\":[1005], \"tags_ids\":[5006]}";

        HttpEntity<Object> json = getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.exchange("/product/tag", DELETE, json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    
    
    
    @Test
    public void deleteProductTagsMissingProductId() {
        String body = "{\"tags_ids\":[5006]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.exchange("/product/tag", DELETE, json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    
    
    
    
    @Test
    public void deleteProductTagsMissingTagId() {
        String body = "{\"products_ids\":[1005]}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.exchange("/product/tag", DELETE, json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    
    
    
    
    @Test
    public void deleteTagSuccess() {
        HttpEntity<Object> json = getHttpEntity("hijkllm");
        ResponseEntity<String> response = template.exchange("/organization/tag?tag_id=5007", DELETE, json, String.class);

        assertEquals(200, response.getStatusCode().value());
    }

    
    
    @Test
    public void deleteTagMissingTag() {
        HttpEntity<Object> json = getHttpEntity("hijkllm");
        ResponseEntity<String> response = template.exchange("/organization/tag?tag_id=5009", DELETE, json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    
    
    
    @Test
    public void deleteTagInTagTree() {
        HttpEntity<Object> json = getHttpEntity("hijkllm");
        ResponseEntity<String> response = template.exchange("/organization/tag?tag_id=5001", DELETE, json, String.class);
        assertEquals(406, response.getStatusCode().value());
    }

    
    
    
    
    @Test
    public void deleteTagWithLinkedProducts() {

        // adding tag to product first
        String body = "{\"products_ids\":[1006], \"tags_ids\":[5007]}";
        HttpEntity<Object> json = getHttpEntity(body,"hijkllm");
        template.exchange("/product/tag", POST, json, String.class);

        // trying to delete tag
        json = getHttpEntity("hijkllm");
        ResponseEntity<String> response = template.exchange("/organization/tag?tag_id=5007", DELETE, json, String.class);
        assertEquals(200, response.getStatusCode().value());
    }


}
