package com.nasnav.yeshtery.test;

import com.nasnav.dao.*;
import com.nasnav.dto.response.EventsAndReqsResponse;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@AutoConfigureWebTestClient
@NotThreadSafe
@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = { "/sql/Event_Test_Data.sql" })
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = { "/sql/database_cleanup.sql" })
public class InfluencerTest extends AbstractTestWithTempBaseDir {
    @Autowired
    private TestRestTemplate template;

    @Test
    public void testInfluencerEventsAndRequestsWithDefaultSort() {
        HttpEntity<Object> httpEntity = getHttpEntity("101112");
        ResponseEntity<EventsAndReqsResponse> response = template.exchange("/v1/influencer/myEventsAndRequests", HttpMethod.GET, httpEntity,
                EventsAndReqsResponse.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testInfluencerEventsAndRequestsWithCoinSort() {
        HttpEntity<Object> httpEntity = getHttpEntity("101112");
        ResponseEntity<EventsAndReqsResponse> response = template.exchange("/v1/influencer/myEventsAndRequests?sortBy=coins", HttpMethod.GET,
                httpEntity, EventsAndReqsResponse.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testInfluencerEventsAndRequestsWithCoinDescSort() {
        HttpEntity<Object> httpEntity = getHttpEntity("101112");
        ResponseEntity<EventsAndReqsResponse> response = template.exchange("/v1/influencer/myEventsAndRequests?sortBy=coins-desc", HttpMethod.GET,
                httpEntity, EventsAndReqsResponse.class);
        assertEquals(200, response.getStatusCode().value());
        ResponseEntity<EventsAndReqsResponse> response2 = template.exchange("/v1/influencer/myEventsAndRequests?sortBy=coins-desc&status=PENDING",
                HttpMethod.GET, httpEntity, EventsAndReqsResponse.class);
        assertEquals(200, response2.getStatusCode().value());
    }

    @Test
    public void testInfluencerEventsAndRequestsWithNotApprovedInfluencer() {
        HttpEntity<Object> httpEntity = getHttpEntity("131415");
        ResponseEntity<EventsAndReqsResponse> response = template.exchange("/v1/influencer/myEventsAndRequests", HttpMethod.GET, httpEntity,
                EventsAndReqsResponse.class);
        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void testInfluencerEventsAndRequestsWithNotUserNotInfluencer() {
        HttpEntity<Object> httpEntity = getHttpEntity("177147");
        ResponseEntity<EventsAndReqsResponse> response = template.exchange("/v1/influencer/myEventsAndRequests", HttpMethod.GET, httpEntity,
                EventsAndReqsResponse.class);
        assertEquals(404, response.getStatusCode().value());
    }
}
