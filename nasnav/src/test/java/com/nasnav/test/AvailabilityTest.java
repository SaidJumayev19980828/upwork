package com.nasnav.test;

import com.nasnav.dao.AvailabilityRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.SchedulerTaskRepository;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.persistence.AvailabilityEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.SchedulerTaskEntity;
import com.nasnav.service.AvailabilityService;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Appointment_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class AvailabilityTest extends AbstractTestWithTempBaseDir {
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private AvailabilityRepository availabilityRepository;
    @Autowired
    private SchedulerTaskRepository schedulerTaskRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired

    private AvailabilityService availabilityService;
    @Test
    public void createAvailabilitiesTest(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime half = LocalDateTime.now().plusMinutes(40);
        LocalDateTime end = LocalDateTime.now().plusMinutes(60);
        String requestBody =
                json()
                        .put("starts_at", now)
                        .put("ends_at", end)
                        .put("organization_id", 99001L)
                        .put("period", 15)
                        .toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/availability", json, Void.class);
        assertEquals(200, response.getStatusCode().value());

        String requestBody2 =
                json()
                        .put("starts_at", now)
                        .put("ends_at", half)
                        .put("organization_id", 99001L)
                        .put("period", 20)
                        .toString();

        HttpEntity<?> json2 = getHttpEntity(requestBody2, "abcdefg");
        ResponseEntity<Void> response2 = template.postForEntity("/availability?force=true", json2, Void.class);
        assertEquals(200, response2.getStatusCode().value());
        OrganizationEntity org = organizationRepository.findOneById(99001L);
        List<AvailabilityEntity> entities = availabilityRepository.getAllFreeAvailabilitiesByOrganization(org);
        assertEquals(6,entities.size());
    }


    @Test
    public void shiftAvailability(){
        List<Long> ids = new ArrayList<>();
        ids.add(1L);
        ids.add(2L);
        ids.add(3L);
        String requestBody = ids.toString();
        HttpEntity<?> json = getHttpEntity(requestBody,"abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/availability/shift/10", json, Void.class);
        assertEquals(200, response.getStatusCode().value());
        Optional<AvailabilityEntity> entity = availabilityRepository.findById(1L);
        if(entity.isPresent()){
            LocalDateTime result = LocalDateTime.of(2033,01,13, 17,20);
            assertEquals(result,entity.get().getStartsAt());
        }
        Optional<SchedulerTaskEntity> entity2 = schedulerTaskRepository.findById(1L);
        if(entity2.isPresent()){
            LocalDateTime result = LocalDateTime.of(2033,01,13, 17,20);
            assertEquals(result,entity.get().getStartsAt());
        }
    }

    //TODO
    @Test
    @Ignore

    public void setAppointment(){


        HttpEntity<?> json = getHttpEntity("123");
        ResponseEntity<Void> response = template.postForEntity("/appointment/1", json, Void.class);
        assertEquals(200, response.getStatusCode().value());
        Optional<AvailabilityEntity> entity = availabilityRepository.findById(1L);
        long result = entity.get().getUser().getId();
        assertEquals(88,result);
    }

    @Test
    public void getAllEmployeesWithOrWithoutSlotsByOrg(){
        Set<UserRepresentationObject> emps = availabilityService.getAllEmployeesWithOrWithoutSlotsByOrg(99001L,true);
        assertEquals(1, emps.size());
        Set<UserRepresentationObject> empsWithoutSlots = availabilityService.getAllEmployeesWithOrWithoutSlotsByOrg(99001L,true);
        assertEquals(1,empsWithoutSlots.size());

    }



//    @Test
//    public void checkIntersectionValidator(){
//        //DB -> "2023-01-13 17:10:00"  to "2023-01-13 17:21:00"
//        //left Non touch false
//        LocalDateTime start = LocalDateTime.of(2023,01,13,17,1);
//        LocalDateTime end = LocalDateTime.of(2023,01,13,17,9);
//        //right Non touch false
//        LocalDateTime start1 = LocalDateTime.of(2023,01,13,17,22);
//        LocalDateTime end1 = LocalDateTime.of(2023,01,13,17,39);
//        //right touch false ----
//        LocalDateTime start2 = LocalDateTime.of(2023,01,13,17,1);
//        LocalDateTime end2 = LocalDateTime.of(2023,01,13,17,10);
//        //right touch false ----
//        LocalDateTime start3 = LocalDateTime.of(2023,01,13,17,21);
//        LocalDateTime end3 = LocalDateTime.of(2023,01,13,17,31);
//        //left intersect true
//        LocalDateTime start4 = LocalDateTime.of(2023,01,13,17,5);
//        LocalDateTime end4 = LocalDateTime.of(2023,01,13,17,12);
//        //right intersect true
//        LocalDateTime start5 = LocalDateTime.of(2023,01,13,17,20);
//        LocalDateTime end5 = LocalDateTime.of(2023,01,13,17,25);
//        //inside true ----
//        LocalDateTime start6 = LocalDateTime.of(2023,01,13,17,15);
//        LocalDateTime end6 = LocalDateTime.of(2023,01,13,17,20);
//        //containing true
//        LocalDateTime start7 = LocalDateTime.of(2023,01,13,17,9);
//        LocalDateTime end7 = LocalDateTime.of(2023,01,13,17,22);
//
//        assertEquals(false,availabilityRepository.existsByEndsAtIsGreaterThanAndStartsAtIsLessThan(start,end));
//        assertEquals(false,availabilityRepository.existsByEndsAtIsGreaterThanAndStartsAtIsLessThan(start1,end1));
//        assertEquals(false,availabilityRepository.existsByEndsAtIsGreaterThanAndStartsAtIsLessThan(start2,end2));
//        assertEquals(false,availabilityRepository.existsByEndsAtIsGreaterThanAndStartsAtIsLessThan(start3,end3));
//        assertEquals(true,availabilityRepository.existsByEndsAtIsGreaterThanAndStartsAtIsLessThan(start4,end4));
//        assertEquals(true,availabilityRepository.existsByEndsAtIsGreaterThanAndStartsAtIsLessThan(start5,end5));
//        assertEquals(true,availabilityRepository.existsByEndsAtIsGreaterThanAndStartsAtIsLessThan(start6,end6));
//        assertEquals(true,availabilityRepository.existsByEndsAtIsGreaterThanAndStartsAtIsLessThan(start7,end7));
//
//
//    }

}
