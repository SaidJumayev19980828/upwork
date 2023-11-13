package com.nasnav.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.EventsNewDTO;
import com.nasnav.dto.request.ContactUsFeedBackRequestDto;
import com.nasnav.dto.request.ContactUsRequestDto;
import com.nasnav.dto.request.EventForRequestDTO;
import com.nasnav.dto.response.RestResponsePage;
import com.nasnav.persistence.ContactUsEntity;
import com.nasnav.service.ContactUsService;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import net.jcip.annotations.NotThreadSafe;
import org.elasticsearch.rest.RestResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureWebTestClient
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Contact_US_TEST_DATA.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class ContactUsTest extends AbstractTestWithTempBaseDir {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContactUsService contactUsService;

    @Test
    public void testContactUsMail() throws Exception {
        ContactUsRequestDto requestDto = new ContactUsRequestDto("John Doe", "john@example.com", "Hello");
        Long orgId = 99001L;

        HttpCookie cookie = new HttpCookie("User-Token", "161718");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "161718");
        HttpEntity<ContactUsRequestDto> request = new HttpEntity<>(requestDto,headers);


        ResponseEntity<Void> response = template.exchange("/contactUs?orgId=99001" , HttpMethod.POST, request ,Void.class);
        assertEquals(200, response.getStatusCode().value());

    }

    @Test
    public void testContactUsFeedbackMail() throws Exception {
        ContactUsFeedBackRequestDto requestDto = new ContactUsFeedBackRequestDto("Feedback message", 1L);


        HttpCookie cookie = new HttpCookie("User-Token", "161718");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "161718");
        HttpEntity<ContactUsFeedBackRequestDto> request = new HttpEntity<>(requestDto,headers);


        ResponseEntity<Void> response = template.exchange("/contactUs/feedback" , HttpMethod.POST, request ,Void.class);
        assertEquals(200, response.getStatusCode().value());

    }

    @Test
    public void testGetContactUs() throws Exception {
        HttpEntity<Object> httpEntity = getHttpEntity("101112");
        ParameterizedTypeReference<ContactUsEntity> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<ContactUsEntity> response = template.exchange("/contactUs?formId=1" , HttpMethod.GET, httpEntity, responseType);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testGetAllForms() throws Exception {

        HttpEntity<Object> httpEntity = getHttpEntity("101112");
        ParameterizedTypeReference<RestResponsePage<ContactUsEntity>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<RestResponsePage<ContactUsEntity>> response = template.exchange("/contactUs/all?fromDate=2023-11-11T17:58:26.746741&start=0&count=1" , HttpMethod.GET, httpEntity, responseType);
        assertEquals(200, response.getStatusCode().value());
    }
}
