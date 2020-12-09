package com.nasnav.test;

import com.google.common.net.UrlEscapers;
import com.nasnav.NavBox;
import com.nasnav.dto.response.navbox.SearchResult;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriBuilder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;

import static com.google.common.net.MediaType.APPLICATION_BINARY;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.nasnav.test.commons.TestCommons.readResourceFileAsString;
import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/ExtraAttributes_Test_Data_Insert.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@NotThreadSafe
public class NavBoxSearchTest {

    private String mockServerFullUrl;

    @Value("classpath:/json/elastic_search/search_response.json")
    private Resource searchResponseJson;

    @Value("${nasnav.elasticsearch.url}")
    private String mockServerUrl;

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
    @Before
    public void prepareMockServer() throws Exception {
//        prepareMockRequests(mockServerRule);
    }



    @Test
    public void searchTest(){
        ResponseEntity<SearchResult> response =
                template.getForEntity("/navbox/search?org_id=43&keyword=Shrt", SearchResult.class);
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
