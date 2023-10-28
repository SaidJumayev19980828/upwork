package com.nasnav.test;

import com.nasnav.dto.response.navbox.SearchResult;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;

import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.readResourceFileAsString;
import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Search_Test_Data_Insert.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@NotThreadSafe
// @Ignore  //it is used only by developers using a local elasticsearch instance
public class NavBoxSearchTest extends AbstractTestWithTempBaseDir {

    @Value("classpath:/json/elastic_search/search_response.json")
    private Resource searchResponseJson;

    @Value("classpath:/json/elastic_search/products_response.json")
    private Resource productsResponseJson;

    @Value("classpath:/json/elastic_search/bulk_response.json")
    private Resource bulkResponseJson;

    @Value("${nasnav.elasticsearch.url}")
    private String mockServerUrl;

    //failed to mock elastic search responses, returning the same response structure as the
    //the one returned by using postman didn't work, their client fails to parse the
    //response for some reason.
    //if this problem was investigated later, you need to uncomment @Rule and @Before and @Test to
    //initialize the mockserver. the mockserver will responed to requests sent to the url
    // in property nasnav.elasticsearch.url
    //Currently the recommended way by elasticsearch to do the integration tests,
    //is to use TestContainers library, which will run a container with elastic search instance.
    //but doing this requires some agreement in the team first.
    @Rule
    public MockServerRule mockServerRule;


    @Autowired
    private TestRestTemplate template;





    /**
     * set the mockserver port before its initialized by @Rule
     * */
    @PostConstruct
    public  void initElasticSearchMockServer() throws Exception {
        URI mockServerUri = new URI(mockServerUrl);
        int port = mockServerUri.getPort();
        mockServerRule = new MockServerRule(this, port);
    }



    @Test
    public void searchTest() throws IOException{
        mockSearchRequest(mockServerRule);
        ResponseEntity<SearchResult> response =
                template.getForEntity("/navbox/search?org_id=99001&keyword=tag_1", SearchResult.class);
        assertEquals(OK, response.getStatusCode());
    }



    @Test
    public void searchDataSyncTest() throws IOException{
        mockProductsRequest(mockServerRule, "/products");
        mockProductsRequest(mockServerRule, "/collections");
        mockProductsRequest(mockServerRule, "/tags");
        mockProductsRequest(mockServerRule, "/products");
        mockBulkRequest(mockServerRule);

        ResponseEntity<String> response =
                template.postForEntity("/organization/search/data/sync", getHttpEntity("192021"), String.class);
        assertEquals(OK, response.getStatusCode());
    }



    private  void mockSearchRequest(MockServerRule mockServerRule) throws IOException {
        String searchResponse = readResourceFileAsString(searchResponseJson);
        mockServerRule.getClient()
                .when(
                        request().withMethod("POST")
                                .withPath("/_search")
                                )
                .respond(
                        response().withBody(searchResponse, JSON_UTF_8)
                                .withStatusCode(200)
                                .withHeader(CONTENT_TYPE, "application/json; charset=UTF-8"));
    }
    
    private  void mockProductsRequest(MockServerRule mockServerRule, String path) throws IOException {
        String productResponse = readResourceFileAsString(productsResponseJson);
        mockServerRule.getClient()
                .when(
                        request().withMethod("PUT")
                                .withPath(path)
                                )
                .respond(
                        response().withBody(productResponse, JSON_UTF_8)
                                .withStatusCode(200)
                                .withHeader(CONTENT_TYPE, "application/json; charset=UTF-8"));
    }

    private  void mockBulkRequest(MockServerRule mockServerRule) throws IOException {
        String bulkResponse = readResourceFileAsString(bulkResponseJson);
        mockServerRule.getClient()
                .when(
                        request().withMethod("POST")
                                .withPath("/_bulk")
                                )
                .respond(
                        response().withBody(bulkResponse, JSON_UTF_8)
                                .withStatusCode(200)
                                .withHeader(CONTENT_TYPE, "application/json; charset=UTF-8"));
    }
}
