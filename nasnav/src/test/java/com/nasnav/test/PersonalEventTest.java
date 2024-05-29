package com.nasnav.test;

import com.nasnav.dto.InvitePeopleDTO;
import com.nasnav.dto.PersonalEventDTO;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/personal_event.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class PersonalEventTest  extends AbstractTestWithTempBaseDir {

    @Autowired
    private TestRestTemplate template;


    @Test
    public void createEventTest(){
        PersonalEventDTO eventDTO = new PersonalEventDTO("Test Event",LocalDateTime.now().plusMinutes(10),LocalDateTime.now().plusHours(1),"Test Description");
        HttpCookie cookie = new HttpCookie("User-Token", "123");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "123");
        HttpEntity<PersonalEventDTO> request = new HttpEntity<>(eventDTO,headers);
        ResponseEntity<Void> response = template.postForEntity("/party-room", request, Void.class);
        assertEquals(201, response.getStatusCode().value());
    }


    @Test
    public void updateEventTest(){
        PersonalEventDTO eventDTO = new PersonalEventDTO("Test Event",LocalDateTime.now().plusMinutes(10),LocalDateTime.now().plusHours(1),"Test Description");
        HttpCookie cookie = new HttpCookie("User-Token", "123");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "123");
        HttpEntity<PersonalEventDTO> request = new HttpEntity<>(eventDTO,headers);
        ResponseEntity<Void> response = template.exchange("/party-room/99",HttpMethod.PUT, request, Void.class);
        assertEquals(201, response.getStatusCode().value());
    }


    @Test
    public void createEventTestException(){
        PersonalEventDTO eventDTO = buildDTO("test1" , "desc", LocalDateTime.now() , LocalDateTime.now().plusHours(1));
        HttpCookie cookie = new HttpCookie("User-Token", "123");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "123");
        HttpEntity<PersonalEventDTO> request = new HttpEntity<>(eventDTO,headers);
        ResponseEntity<Void> response = template.postForEntity("/party-room", request, Void.class);
        assertEquals(400, response.getStatusCode().value());


        PersonalEventDTO eventDTO2 = buildDTO("test1" , "desc", LocalDateTime.now() , LocalDateTime.now().minusHours(1));
        HttpEntity<PersonalEventDTO> request2 = new HttpEntity<>(eventDTO2,headers);
        ResponseEntity<Void> response2 = template.postForEntity("/party-room", request2, Void.class);
        assertEquals(400, response2.getStatusCode().value());



        PersonalEventDTO eventDTO3 =  buildDTO("test1" , "desc", LocalDateTime.now().plusHours(3) , LocalDateTime.now().plusHours(1));
        HttpEntity<PersonalEventDTO> request3 = new HttpEntity<>(eventDTO3,headers);
        ResponseEntity<Void> response3 = template.postForEntity("/party-room", request3, Void.class);
        assertEquals(400, response3.getStatusCode().value());
    }

    @Test
    public void cancelEventTestUser() {
        HttpCookie cookie = new HttpCookie("User-Token", "123");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "123");
        HttpEntity<PersonalEventDTO> request = new HttpEntity<>(headers);
        ResponseEntity<Void> response = template.exchange(
                "/party-room/99",
                HttpMethod.DELETE,
                request,
                Void.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    public void cancelEventTestUserEmployee() {
        HttpCookie cookie = new HttpCookie("User-Token", "abcdefg");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "abcdefg");
        HttpEntity<PersonalEventDTO> request = new HttpEntity<>(headers);
        ResponseEntity<Void> response = template.exchange(
                "/party-room/101",
                HttpMethod.DELETE,
                request,
                Void.class
        );
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void cancelEventTestUserException() {
        HttpCookie cookie = new HttpCookie("User-Token", "123");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "123");
        HttpEntity<PersonalEventDTO> request = new HttpEntity<>(headers);
        ResponseEntity<Void> response = template.exchange(
                "/party-room/100",
                HttpMethod.DELETE,
                request,
                Void.class
        );
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void findAll(){
        HttpCookie cookie = new HttpCookie("User-Token", "123");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "123");
        HttpEntity<PersonalEventDTO> request = new HttpEntity<>(headers);
        ResponseEntity<Void> response = template.exchange(
                "/party-room/all",
                HttpMethod.GET,
                request,
                Void.class
        );
        assertEquals(200, response.getStatusCode().value());
    }

@Test
public void findById(){
    HttpCookie cookie = new HttpCookie("User-Token", "123");
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", cookie.toString());
    headers.add("User-Token", "123");
    HttpEntity<PersonalEventDTO> request = new HttpEntity<>(headers);
    ResponseEntity<Void> response = template.exchange(
            "/party-room/100",
            HttpMethod.GET,
            request,
            Void.class
    );
    assertEquals(200, response.getStatusCode().value());
}


    @Test
    public void findByIdException(){
        HttpCookie cookie = new HttpCookie("User-Token", "123");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "123");
        HttpEntity<PersonalEventDTO> request = new HttpEntity<>(headers);
        ResponseEntity<Void> response = template.exchange(
                "/party-room/1",
                HttpMethod.GET,
                request,
                Void.class
        );
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void inviteUser(){
        HttpCookie cookie = new HttpCookie("User-Token", "123");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "123");
        InvitePeopleDTO  invite = buildInvite(null, false, 89L);
        HttpEntity<InvitePeopleDTO> request = new HttpEntity<>(invite,headers);
        ResponseEntity<Void> response = template.exchange(
                "/party-room/invite/99",
                HttpMethod.POST,
                request,
                Void.class
        );
        assertEquals(200, response.getStatusCode().value());
    }


    @Test
    public void inviteEmployee(){
        HttpCookie cookie = new HttpCookie("User-Token", "123");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "123");
        InvitePeopleDTO  invite = buildInvite(null, true, 68L);
        HttpEntity<InvitePeopleDTO> request = new HttpEntity<>(invite,headers);
        ResponseEntity<Void> response = template.exchange(
                "/party-room/invite/99",
                HttpMethod.POST,
                request,
                Void.class
        );
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void inviteForEmployee(){
        HttpCookie cookie = new HttpCookie("User-Token", "abcdefg");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "abcdefg");
        InvitePeopleDTO  invite = buildInvite(null, true, 69L);
        HttpEntity<InvitePeopleDTO> request = new HttpEntity<>(invite,headers);
        ResponseEntity<Void> response = template.exchange(
                "/party-room/invite/101",
                HttpMethod.POST,
                request,
                Void.class
        );
        assertEquals(200, response.getStatusCode().value());
    }


    @Test
    public void inviteExternal(){
        HttpCookie cookie = new HttpCookie("User-Token", "123");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "123");
        InvitePeopleDTO  invite = buildInvite("test@gmail.com", false, null);
        HttpEntity<InvitePeopleDTO> request = new HttpEntity<>(invite,headers);
        ResponseEntity<Void> response = template.exchange(
                "/party-room/invite/99",
                HttpMethod.POST,
                request,
                Void.class
        );
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void myEvents(){
        HttpCookie cookie = new HttpCookie("User-Token", "123");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "123");
        HttpEntity<PersonalEventDTO> request = new HttpEntity<>(headers);
        ResponseEntity<Void> response = template.exchange(
                "/party-room/mine",
                HttpMethod.GET,
                request,
                Void.class
        );
        assertEquals(200, response.getStatusCode().value());
    }


    private PersonalEventDTO buildDTO(String name, String description, LocalDateTime startAt, LocalDateTime endAt){
        return new PersonalEventDTO(name , startAt, endAt , description);
    }

    private InvitePeopleDTO buildInvite(String email, boolean employee , Long user){
        InvitePeopleDTO invite = new InvitePeopleDTO();
        invite.setExternalMail(email);
        invite.setEmployee(employee);
        invite.setUser(user);
        return invite;
    }
}
