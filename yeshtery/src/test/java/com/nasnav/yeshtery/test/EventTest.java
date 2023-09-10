package com.nasnav.yeshtery.test;

import com.nasnav.dao.EventLogsRepository;
import com.nasnav.dao.EventRepository;
import com.nasnav.dao.EventRequestsRepository;
import com.nasnav.dao.InfluencerRepository;
import com.nasnav.dto.response.EventResponseDto;
import com.nasnav.dto.response.RestResponsePage;
import com.nasnav.persistence.EventAttachmentsEntity;
import com.nasnav.persistence.EventEntity;
import com.nasnav.persistence.EventRequestsEntity;
import com.nasnav.persistence.InfluencerEntity;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.nasnav.yeshtery.test.commons.TestCommons.*;
import static org.junit.Assert.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@AutoConfigureWebTestClient
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Event_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class EventTest extends AbstractTestWithTempBaseDir {
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventLogsRepository eventLogsRepository;
    @Autowired
    private InfluencerRepository influencerRepository;
    @Autowired
    private EventRequestsRepository eventRequestsRepository;

    @Test
    public void createEvent(){
        EventAttachmentsEntity attachment1 = new EventAttachmentsEntity();
        attachment1.setUrl("URL");
        attachment1.setType("Media type");
        List<EventAttachmentsEntity> attachments = Arrays.asList(attachment1,attachment1);
        JSONObject requestBody = json()
                .put("startsAt","2023-01-28T15:08:39")
                .put("endsAt","2023-01-29T15:08:39")
                .put("organizationId","99001")
                .put("attachments",attachments)
                .put("name","name")
                .put("description","description")
                .put("productsIds",Arrays.asList(1001))
                .put("visible","false")
                ;

        ResponseEntity<String> response = template.postForEntity("/v1/event",
                getHttpEntity(requestBody.toString(), "161718"), String.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void updateEvent(){
        EventAttachmentsEntity attachment1 = new EventAttachmentsEntity();
        attachment1.setUrl("URL");
        attachment1.setType("Media type");
        List<EventAttachmentsEntity> attachments = Arrays.asList(attachment1,attachment1);
        JSONObject requestBody = json()
                .put("id",1)
                .put("startsAt","2023-01-28T15:08:39")
                .put("endsAt","2023-01-29T15:08:39")
                .put("organizationId","99001")
                .put("attachments",attachments)
                .put("name","name")
                .put("status","PENDING")
                .put("description","description")
                .put("productsIds",Arrays.asList(1001))
                .put("visible",true)
                ;

        template.put("/v1/event/100",
                getHttpEntity(requestBody.toString(), "101112"), String.class);
        Optional<EventEntity> entity = eventRepository.findById(100L);
        assertTrue(entity.get().getVisible());
        assertNotNull(entity.get().getAttachments());
        assertNotNull(entity.get().getProducts());
    }

    @Test
    public void getAllEvents(){
        HttpEntity<Object> httpEntity = getHttpEntity("101112");
        ParameterizedTypeReference<RestResponsePage<EventResponseDto>> responseType = new ParameterizedTypeReference<>() {
        };
        LocalDateTime fromDate = LocalDateTime.now().minusDays(15);
        LocalDateTime toDate = LocalDateTime.now().plusDays(15);
        ResponseEntity<RestResponsePage<EventResponseDto>> response = template.exchange("/v1/event/list?fromDate=" + fromDate + "&toDate=" + toDate, HttpMethod.GET, httpEntity, responseType);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void interestForUser() {
        ResponseEntity<String> response = template.postForEntity("/v1/event/interset/100",
                getHttpEntity("", "123"), String.class);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(eventLogsRepository.existsByEvent_IdAndUser_IdOrEmployee_Id(100L,88L,88L));
    }

    @Test
    public void becomeInfluencerRequest() {
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(201L);
        ResponseEntity<String> response = template.postForEntity("/v1/influencer/request",
                getHttpEntity(categoryIds.toString(), "123"), String.class);
        assertEquals(200, response.getStatusCode().value());
        InfluencerEntity entity = influencerRepository.getByUser_IdOrEmployeeUser_Id(88L,88L);
        assertNotNull(entity);
        assertFalse(entity.getApproved());
    }

    @Test
    public void approveInfluencerRequest() {
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(201L);
        ResponseEntity<String> response = template.postForEntity("/v1/influencer/response?influencerId=100&action=true",
                getHttpEntity("", "101112"), String.class);
        assertEquals(200, response.getStatusCode().value());
        InfluencerEntity entity = influencerRepository.getByUser_IdOrEmployeeUser_Id(71L,71L);
        assertNotNull(entity);
        assertTrue(entity.getApproved());
    }

    @Test
    public void hostRequest() {
        JSONObject requestBody = json()
                .put("id",100)
                .put("starts_at","2023-01-17T15:20:11")
                .put("ends_at","2023-01-17T16:20:11")
                ;

        ResponseEntity<String> response = template.postForEntity("/v1/influencer/host",
                getHttpEntity(requestBody.toString(), "abcdefg"), String.class);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(eventRequestsRepository.existsByEvent_IdAndStatusEquals(100L,0));
    }

    @Test
    public void approveHostRequest() {
        template.put("/v1/influencer/host/100?approve=true",
                getHttpEntity("", "161718"), String.class);
        Optional<EventRequestsEntity> entity = eventRequestsRepository.findById(100L);
        assertEquals(1,entity.get().getStatus().intValue());
    }

}
