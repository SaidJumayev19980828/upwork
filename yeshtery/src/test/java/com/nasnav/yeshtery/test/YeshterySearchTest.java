package com.nasnav.yeshtery.test;

import com.nasnav.dto.response.navbox.SearchResult;
import com.nasnav.yeshtery.Yeshtery;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;

import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.yeshtery.test.commons.TestCommons.readResourceFileAsString;
import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Yeshtery.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Search_Test_Data_Insert.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@NotThreadSafe
@Ignore  //it is used only by developers using a local elasticsearch instance
public class YeshterySearchTest {

    private String mockServerFullUrl;

    @Value("classpath:/json/elastic_search/search_response.json")
    private Resource searchResponseJson;

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
//    @Rule
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


    /**
     * Prepares the mocks server responses, this must run after both @PostConstruct
     * and @Rule, as @Rule will create initialize the mock server.
     * */
//    @Before
    public void prepareMockServer() throws Exception {
        prepareMockRequests(mockServerRule);
    }



//    @Test
    public void searchTest(){
        ResponseEntity<SearchResult> response =
                template.getForEntity("/navbox/search?keyword=tag_1", SearchResult.class);
        assertEquals(OK, response.getStatusCode());
    }



//    @Test
    public void searchDataSyncTest(){
        ResponseEntity<String> response =
                template.postForEntity("/organization/search/data/sync", getHttpEntity("192021"), String.class);
        assertEquals(OK, response.getStatusCode());
    }



    private  void prepareMockRequests(MockServerRule mockServerRule) throws Exception {
        mockSearchRequest(mockServerRule);
    }



    private  void mockSearchRequest(MockServerRule mockServerRule) throws IOException {
        String productsResponse = readResourceFileAsString(searchResponseJson);
        mockServerRule.getClient()
                .when(
                        request().withMethod("POST")
                                .withPath("/_search")
                                )
                .respond(
                        response().withBody(productsResponse, JSON_UTF_8)
                                .withStatusCode(200)
                                .withHeader(CONTENT_TYPE, "application/json; charset=UTF-8"))
        ;
    }
}
