package com.nasnav.yeshtery.test;

import com.nasnav.dao.EventLogsRepository;
import com.nasnav.dao.EventRepository;
import com.nasnav.dao.EventRequestsRepository;
import com.nasnav.dao.InfluencerRepository;
import com.nasnav.dto.EventsNewDTO;
import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.request.EventForRequestDTO;
import com.nasnav.dto.response.RestResponsePage;
import com.nasnav.persistence.*;
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
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.*;

import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.yeshtery.test.commons.TestCommons.json;
import static org.junit.Assert.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
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
        EventAttachmentsEntity attachment2 = new EventAttachmentsEntity();
        attachment1.setUrl("URL");
        attachment1.setType("Media type");
        attachment1.setCoin(10L);
        attachment2.setUrl("url2");
        attachment2.setType("Media type2");
        attachment2.setCoin(20L);

        List<EventAttachmentsEntity> attachments = Arrays.asList(attachment1,attachment2);

        EventForRequestDTO eventForRequestDTO = new EventForRequestDTO();
       eventForRequestDTO.setOrganizationId(99001L);
       eventForRequestDTO.setStartsAt(LocalDateTime.now());
       eventForRequestDTO.setEndsAt(LocalDateTime.now().plusMonths(2));
       eventForRequestDTO.setName("name");
       eventForRequestDTO.setDescription("description");
       Set<Long> productsIds = new HashSet<>();
       productsIds.add(1001L);
       eventForRequestDTO.setProductsIds(productsIds);
        Set<Long> influencersIds = new HashSet<>();
        productsIds.add(1001L);
       eventForRequestDTO.setInfluencersIds(influencersIds);
       eventForRequestDTO.setSceneId("testasc");
       eventForRequestDTO.setAttachments(attachments);
       eventForRequestDTO.setCoin(10L);
       eventForRequestDTO.setVisible(false);

        HttpCookie cookie = new HttpCookie("User-Token", "161718");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "161718");
        HttpEntity<EventForRequestDTO> request = new HttpEntity<>(eventForRequestDTO,headers);

        ResponseEntity<Void> response = template.postForEntity("/v1/event",request ,Void.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void createEvent2(){
        EventForRequestDTO eventForRequestDTO = new EventForRequestDTO();
        eventForRequestDTO.setOrganizationId(99001L);
        eventForRequestDTO.setStartsAt(LocalDateTime.now());
        eventForRequestDTO.setEndsAt(LocalDateTime.now().plusMonths(2));
        eventForRequestDTO.setName("name");
        eventForRequestDTO.setDescription("description");
        Set<Long> influencersIds = new HashSet<>();
        influencersIds.add(100L);
        eventForRequestDTO.setInfluencersIds(influencersIds);
        eventForRequestDTO.setSceneId("testasc");
        eventForRequestDTO.setCoin(10L);
        eventForRequestDTO.setVisible(false);
        Set<Long> productsIds = new HashSet<>();
        productsIds.add(1001L);
        eventForRequestDTO.setProductsIds(productsIds);


        HttpCookie cookie = new HttpCookie("User-Token", "161718");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "161718");
        HttpEntity<EventForRequestDTO> request = new HttpEntity<>(eventForRequestDTO,headers);

        ResponseEntity<Void> response = template.postForEntity("/v1/event",request ,Void.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void delete(){
        HttpEntity<Object> httpEntity = getHttpEntity("101112");
        ResponseEntity<Void> response = template.exchange("/v1/event/100?force=true", HttpMethod.DELETE, httpEntity, Void.class);

        ResponseEntity<Void> response2 = template.exchange("/v1/event/100", HttpMethod.GET, httpEntity, Void.class);
        assertEquals(404, response2.getStatusCode().value());

    }

    @Test
    public void eventById(){
        HttpEntity<Object> httpEntity = getHttpEntity("101112");
        ResponseEntity<Void> response = template.exchange("/v1/event/100", HttpMethod.GET, httpEntity, Void.class);
        assertEquals(200, response.getStatusCode().value());
    }
    @Test
    public void updateEvent(){
        EventAttachmentsEntity attachment1 = new EventAttachmentsEntity();
        JSONObject requestBody = json()
                .put("id",1)
                .put("startsAt","2023-01-28T15:08:39")
                .put("endsAt","2023-01-29T15:08:39")
                .put("organizationId","99001")
                .put("name","name")
                .put("status","PENDING")
                .put("description","description")
                .put("status","PENDING")
                .put("attachments",Collections.emptyList())
                .put("influencersIds",Collections.emptyList())
                .put("productsIds", Collections.emptyList())
                .put("visible",true);

        template.put("/v1/event/100",
                getHttpEntity(requestBody.toString(), "101112"), String.class);
        Optional<EventEntity> entity = eventRepository.findById(100L);
        assertTrue(entity.get().getVisible());
        assertNotNull(entity.get().getAttachments());
        assertNotNull(entity.get().getProducts());
    }

    @Test
    public void getEventAdvertisement(){
        HttpEntity<Object> httpEntity = getHttpEntity("101112");
        ResponseEntity<Void> response = template.exchange("/v1/event/list/advertise", HttpMethod.GET, httpEntity, Void.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void getEventInterests(){
        HttpEntity<Object> httpEntity = getHttpEntity("101112");
        ResponseEntity<Void> response = template.exchange("/v1/event/interests/101", HttpMethod.GET, httpEntity, Void.class);
        assertEquals(200, response.getStatusCode().value());
    }
    @Test
    public void getAllEvents(){
        HttpEntity<Object> httpEntity = getHttpEntity("101112");
        LocalDateTime fromDate = LocalDateTime.now().minusDays(15);
        LocalDateTime toDate = LocalDateTime.now().plusDays(15);
        ResponseEntity<Void> response = template.exchange("/v1/event/list?fromDate=" + fromDate + "&toDate=" + toDate, HttpMethod.GET, httpEntity, Void.class);
        assertEquals(200, response.getStatusCode().value());

        ResponseEntity<Void> response2 = template.exchange("/v1/event/list?orgId=" + 99001L + "&toDate=" + toDate, HttpMethod.GET, httpEntity, Void.class);
        assertEquals(200, response2.getStatusCode().value());
    }

    @Test
    public void getEventsByName(){
        HttpEntity<Object> httpEntity = getHttpEntity("101112");
        ResponseEntity<Void> response = template.exchange("/v1/event/getEventByName/name" , HttpMethod.GET, httpEntity, Void.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void getEventByNameService(){
        Object page = null;
        PageImpl<EventEntity> entity = eventRepository.findByName("name", null);
        assertFalse(entity.isEmpty());
    }



    @Test
    public void getAllEventsForUnAuth(){
        HttpEntity<Object> httpEntity = getHttpEntity("");
        long organizationId = 99001L;
        LocalDateTime fromDate = LocalDateTime.now().minusDays(15);

        ResponseEntity<Void> response = template.exchange("/v1/event/all?fromDate=" + fromDate + "&?orgId=" + organizationId  , HttpMethod.GET, httpEntity, Void.class);
        assertEquals(200, response.getStatusCode().value());

    }
    @Test
    public void getAllAdvertises(){
        HttpEntity<Object> httpEntity = getHttpEntity("101112");
        ParameterizedTypeReference<RestResponsePage<EventsNewDTO>> responseType = new ParameterizedTypeReference<>() {
        };
        Long organizationId = 99001L;
        ResponseEntity<RestResponsePage<EventsNewDTO>> response = template.exchange("/v1/event/advertise/all?orgId=" + organizationId , HttpMethod.GET, httpEntity, responseType);
        assertEquals(200, response.getStatusCode().value());

        ResponseEntity<RestResponsePage<EventsNewDTO>> response2 = template.exchange("/v1/event/advertise/all" , HttpMethod.GET, httpEntity, responseType);
        assertEquals(200, response2.getStatusCode().value());
    }

    @Test
    public void test_user_is_approved_influencer_employee_is_null() {
        EventLogsEntity eventLogsEntity = new EventLogsEntity();
        UserEntity userEntity = new UserEntity();
        InfluencerEntity influencerEntity = new InfluencerEntity();
        influencerEntity.setApproved(true);
        userEntity.setInfluencer(influencerEntity);
        eventLogsEntity.setUser(userEntity);

        assertTrue(eventLogsEntity.isInfluencer());
    }
    @Test
    public void test_user_is_approved_influencer_user_is_null() {
        EventLogsEntity eventLogsEntity = new EventLogsEntity();
        EmployeeUserEntity employee = new EmployeeUserEntity();
        InfluencerEntity influencerEntity = new InfluencerEntity();
        influencerEntity.setApproved(true);
        employee.setInfluencer(influencerEntity);
        eventLogsEntity.setEmployee(employee);
        assertTrue(eventLogsEntity.isInfluencer());
    }
    @Test
    public void interestForUser() {
        ResponseEntity<String> response = template.postForEntity("/v1/event/interset/100",
                getHttpEntity("", "123"), String.class);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(eventLogsRepository.existsByEvent_IdAndUser_IdOrEvent_IdAndEmployee_Id(100L,88L,100L,88L));

        ResponseEntity<String> response2 = template.postForEntity("/v1/event/interset/100",
                getHttpEntity("", "101112"), String.class);
        assertEquals(200, response2.getStatusCode().value());
    }


    @Test
    public void interestForUserWithException() {
        ResponseEntity<String> response = template.postForEntity("/v1/event/interset/101",
                getHttpEntity("", "192021"), String.class);
        assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
    }

    @Test
    public void interestedForUser()
    {
        ResponseEntity<PaginatedResponse<EventEntity>> response = template.exchange("/v1/event/listHistoryForUser", HttpMethod.GET,
                getHttpEntity("123"), new ParameterizedTypeReference<>()
                {
                });
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void previousInterestedForUser()
    {
        ResponseEntity<PaginatedResponse<EventEntity>> response = template.exchange("/v1/event/listHistoryForUser?previous_events=true",
                HttpMethod.GET, getHttpEntity("", "123"), new ParameterizedTypeReference<>()
                {
                });
        assertEquals(200, response.getStatusCode().value());
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
