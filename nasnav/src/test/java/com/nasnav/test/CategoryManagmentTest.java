package com.nasnav.test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.AppConfig;
import com.nasnav.controller.AdminController;
import com.nasnav.dao.*;
import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.TagsRepresentationObject;
import com.nasnav.dto.TagsTreeNodeDTO;
import com.nasnav.persistence.CategoriesEntity;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.response.CategoryResponse;
import com.nasnav.service.CategoryService;
import com.nasnav.test.commons.TestCommons;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import lombok.extern.slf4j.Slf4j;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.nasnav.test.commons.TestCommons.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;

@RunWith(SpringRunner.class)
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@Slf4j
public class CategoryManagmentTest extends AbstractTestWithTempBaseDir {

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
    @Autowired
    private ProductRepository productRepo;

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
        String body = json()
                .put("operation", "create")
                .put("parent_id", 202)
                .put("name", "Perfumes")
                .put("logo", "categories/logos/564961451_56541.jpg")
                .put("cover", "cover")
                .put("cover_small", "cover_small")
                .toString();
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<CategoryResponse> response = template.postForEntity("/admin/category", json, CategoryResponse.class);
        assertEquals(OK, response.getStatusCode());
        CategoriesEntity entity = categoryRepository.findById(response.getBody().getCategoryId()).get();
        assertEquals("Perfumes", entity.getName());
        assertEquals("categories/logos/564961451_56541.jpg", entity.getLogo());
        assertEquals("cover", entity.getCover());
        assertEquals("cover_small", entity.getCoverSmall());

        categoryRepository.deleteById(response.getBody().getCategoryId());
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
        String body = json()
                .put("operation", "update")
                .put("id", 201)
                .put("name", "Makeups")
                .put("logo", "categories/logos/1111111111.jpg")
                .put("cover", "cover")
                .put("cover_small", "cover_small")
                .toString();
        HttpEntity<Object> json = TestCommons.getHttpEntity(body, "abcdefg");
        ResponseEntity<CategoryResponse> response = template.postForEntity("/admin/category", json, CategoryResponse.class);
        CategoriesEntity entity = categoryRepository.findById(response.getBody().getCategoryId()).get();
        assertEquals("Makeups", entity.getName());
        assertEquals("categories/logos/1111111111.jpg", entity.getLogo());
        assertEquals("cover", entity.getCover());
        assertEquals("cover_small", entity.getCoverSmall());

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
        ResponseEntity<String> deleteResponse = template.exchange("/admin/category?category_id=" + response.getBody().getCategoryId().intValue(),
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
        assertTrue(404 == deleteResponse.getStatusCodeValue());
    }

    
    
    
    @Test
    public void deleteCategoryUsedCategoryTest() {
        HttpHeaders header = getHeaders("abcdefg");
        ResponseEntity<String> deleteResponse = template.exchange("/admin/category?category_id=202",
                HttpMethod.DELETE, new HttpEntity<>(header), String.class);
        assertTrue(409 == deleteResponse.getStatusCode().value());
    }


    @Test
    public void deleteCategoryUsedCategoryInProductsTest() {


        HttpHeaders header = getHeaders("abcdefg");
        ResponseEntity<String> deleteResponse = template.exchange("/admin/category?category_id=207",
                HttpMethod.DELETE, new HttpEntity<>(header), String.class);
        assertTrue(406 == deleteResponse.getStatusCode().value());
    }

    @Test
    public void getTagsPaginated() {
        ParameterizedTypeReference<PaginatedResponse<TagsRepresentationObject>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<PaginatedResponse<TagsRepresentationObject>> response = template.exchange(
                "/navbox/tags?org_id=99001&start=" + 0 + "&count=" + 1,
                HttpMethod.GET, getHttpEntity(Object.class), responseType);

        Assert.assertEquals(Integer.valueOf(7), response.getBody().getTotalPages());

    }

    @Test
    public void getTags() {
        HttpEntity<Object> httpEntity = getHttpEntity("101112");
        ParameterizedTypeReference<PaginatedResponse<TagsRepresentationObject>> responseType = new ParameterizedTypeReference<>() {
        };

        ResponseEntity<PaginatedResponse<TagsRepresentationObject>> response = template.exchange("/navbox/tags?org_id=99001",
                HttpMethod.GET, httpEntity, responseType);

        Assert.assertEquals(Integer.valueOf(1), response.getBody().getTotalPages());

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
        log.debug("{}", tags);
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



    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_7.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void createTagsTreeDeletedEdgesRemainsAfterError() {
        long countEdgesBefore =  tagEdgesRepo.count();

        assertEquals(1L, countEdgesBefore);

        String body = json()
                .put("nodes"
                        , jsonArray()
                                .put(createTagTreeNode(null, asList(createTagTreeNode(null, null, null,5)), 5003L,8))
                                .put(createTagTreeNode(5005L, null, null,6))
                ).toString();

        HttpEntity<Object> json = getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag/tree", json, String.class);

        long countEdgesAfter =  tagEdgesRepo.count();

        assertEquals(406, response.getStatusCode().value());
        assertEquals(1L, countEdgesAfter);
    }



    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_7.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void createTagsTreeExistingNodeId() {
        long countNodesBefore =  tagNodesRepo.count();
        long countEdgesBefore =  tagEdgesRepo.count();

        assertEquals( 2L, countNodesBefore);

        String body = json()
                .put("nodes"
                        , jsonArray()
                                .put(createTagTreeNode(5003L, asList(createTagTreeNode(5004L, null, null,33)), 5003L,12))
                                .put(createTagTreeNode(5005L, null, null,23))
                    ).toString();

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
        						 	asList(createTagTreeNode(5004L, null, null,9)),
                                 null,76))
        				 .put(createTagTreeNode(5005L, null, null,99))
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
        						 	asList(createTagTreeNode(5008L, null, null,54)),
                                 null,65))
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
        				 .put(createTagTreeNode(5003L, null, null,43)))
        		 .toString();

        HttpEntity<Object> json = getHttpEntity(body,"hijkllm");
        ResponseEntity<String> response = template.postForEntity("/organization/tag/tree", json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }



    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_5.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void getTag() {
        ResponseEntity<TagsRepresentationObject> res =
                template.getForEntity("/navbox/tag?tag_id=5001", TagsRepresentationObject.class);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals(5001L, res.getBody().getId().longValue());
    }


    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_5.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void getTagNonExistingTag() {
        ResponseEntity<TagsRepresentationObject> res =
                template.getForEntity("/navbox/tag?tag_id=-1", TagsRepresentationObject.class);
        assertEquals(404, res.getStatusCodeValue());
    }
    
    
    
    
    private JSONObject createTagTreeNode(Long tagId, List<JSONObject> children, Long nodeId,Integer priority) {
    	JSONArray childrenArr = new JSONArray();
    	ofNullable(children)
    	.orElse(emptyList())
    	.forEach(childrenArr::put);
    	return json()
                .put("node_id", nodeId)
    			.put("tag_id", tagId)
                .put("priority",priority)
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
        HttpEntity<Object> json = getHttpEntity("hijkllm");
        ResponseEntity<String> response = template.exchange("/product/tag?products_ids=1006&tags_ids=5006",
                DELETE, json, String.class);

        assertEquals(200, response.getStatusCode().value());
    }

    
    
    
    @Test
    public void deleteProductTagsNonExistingLink() {
        HttpEntity<Object> json = getHttpEntity("hijkllm");
        ResponseEntity<String> response = template.exchange("/product/tag?products_ids=1005&tags_ids=5006",
                DELETE, json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    
    
    
    @Test
    public void deleteProductTagsMissingProductId() {
        HttpEntity<Object> json = TestCommons.getHttpEntity("hijkllm");
        ResponseEntity<String> response = template.exchange("/product/tag?tags_ids=5006&products_ids=",
                DELETE, json, String.class);

        assertEquals(406, response.getStatusCode().value());
    }

    
    
    
    
    @Test
    public void deleteProductTagsMissingTagId() {
        HttpEntity<Object> json = TestCommons.getHttpEntity("hijkllm");
        ResponseEntity<String> response = template.exchange("/product/tag?products_ids=1005&tags_ids=",
                DELETE, json, String.class);

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
    
    
    

    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_6.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void assignTagCategoryTest() {
   	
    	List<TagsEntity> tagNoCategory = 
    			orgTagsRepo.findByCategoriesEntity_IdAndOrganizationEntity_Id(null, 99001L);
    	List<Long> tagsIds = tagNoCategory.stream().map(TagsEntity::getId).collect(toList());
    	assertNotEquals(0L, tagNoCategory.size());
    	
    	String tagParam = tagsIds.toString().replace("[", "").replace("]", "");
        HttpEntity<Object> json = getHttpEntity("","hijkllm");
        String url = format("/organization/tag/category?category_id=%d&tags=%s", 201, tagParam);
        ResponseEntity<String> response = template.exchange(url, POST, json, String.class);
        assertEquals(OK, response.getStatusCode());
        
        List<TagsEntity> tagNoCategoryAfter = 
    			orgTagsRepo.findByCategoriesEntity_IdAndOrganizationEntity_Id(null, 99001L);
        assertEquals(0L, tagNoCategoryAfter.size());
    }
    
    
    
    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_6.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void assignTagCategoryNoTagsParamTest() {
    	Long categoryId = 201L;
    	List<TagsEntity> tagNoCategory = 
    			orgTagsRepo.findByCategoriesEntity_IdAndOrganizationEntity_Id(null, 99001L);
    	assertNotEquals(0L, tagNoCategory.size());
    	
        HttpEntity<Object> json = getHttpEntity("","hijkllm");
        String url = format("/organization/tag/category?category_id=%d", categoryId);
        ResponseEntity<String> response = template.exchange(url, POST, json, String.class);
        assertEquals(OK, response.getStatusCode());
        
        List<TagsEntity> tagNoCategoryAfter = 
    			orgTagsRepo.findByCategoriesEntity_IdAndOrganizationEntity_Id(null, 99001L);
        assertEquals(0L, tagNoCategoryAfter.size());
        
        List<TagsEntity> tagsWithAssignedCategory = 
    			orgTagsRepo.findByCategoriesEntity_IdAndOrganizationEntity_Id(categoryId, 99001L);
        assertEquals("Only tags with no categories were assigned to the new category",tagNoCategory.size() , tagsWithAssignedCategory.size());
    }


    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_6.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void assignTagCategoryByNasnavAdmin() {
        var body =
                json()
                .put("category_id", 201)
                .put("tags", jsonArray().put(5004).put(5005).put(5006).put(5007))
                .toString();
        HttpEntity<Object> json = getHttpEntity(body, "abcdefg");
        String url = "/admin/tag/category";
        ResponseEntity<String> response = template.exchange(url, POST, json, String.class);
        assertEquals(OK, response.getStatusCode());

        List<Long> tags = orgTagsRepo.findByCategoryId(201L);
        assertEquals(4, tags.size());
    }

    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_6.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void assignTagCategoryByNasnavAdminMissingCategoryId() {
        HttpEntity<Object> json = getHttpEntity("abcdefg");
        String url = "/admin/tag/category?tags=5004&tags=5005&tags=5006&tags=5007";
        ResponseEntity<String> response = template.exchange(url, POST, json, String.class);
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_6.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void assignTagCategoryByNasnavAdminInvalidToken() {
        HttpEntity<Object> json = getHttpEntity("invalid");
        String url = "/admin/tag/category?category_id=201&tags=5004&tags=5005&tags=5006&tags=5007";
        ResponseEntity<String> response = template.exchange(url, POST, json, String.class);
        assertEquals(UNAUTHORIZED, response.getStatusCode());
    }


    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_6.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void assignTagCategoryByNasnavAdminInvalidAuthN() {
        HttpEntity<Object> json = getHttpEntity("hijkllm");
        String url = "/admin/tag/category?category_id=201&tags=5004&tags=5005&tags=5006&tags=5007";
        ResponseEntity<String> response = template.exchange(url, POST, json, String.class);
        assertEquals(FORBIDDEN, response.getStatusCode());
    }


    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_6.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void assignProductCategoryByNasnavAdmin() {
        var body =
                json()
                    .put("category_id", 202)
                    .put("products", jsonArray().put(1001).put(1002))
                    .toString();
        HttpEntity<Object> json = getHttpEntity(body, "abcdefg");
        String url = "/admin/product/category";
        ResponseEntity<String> response = template.exchange(url, POST, json, String.class);
        assertEquals(OK, response.getStatusCode());

        List<Long> products = productRepo.findProductsIdsByCategoryId(202L);
        assertEquals(5, products.size());
    }



    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_6.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void assignProductCategoryByNasnavAdminInvalidToken() {
        HttpEntity<Object> json = getHttpEntity("invalid");
        String url = "/admin/product/category?category_id=202&products=1001&products=1002";
        ResponseEntity<String> response = template.exchange(url, POST, json, String.class);
        assertEquals(UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Category_Test_Data_Insert_6.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void assignProductCategoryByNasnavAdminInvalidAuthZ() {
        HttpEntity<Object> json = getHttpEntity("hijkllm");
        String url = "/admin/product/category?category_id=202&products=1001&products=1002";
        ResponseEntity<String> response = template.exchange(url, POST, json, String.class);
        assertEquals(FORBIDDEN, response.getStatusCode());
    }
}
