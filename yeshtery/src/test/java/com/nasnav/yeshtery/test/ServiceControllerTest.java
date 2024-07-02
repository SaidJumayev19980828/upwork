package com.nasnav.yeshtery.test;

import com.nasnav.dao.ServiceRepository;
import com.nasnav.dto.OrganizationServicesDto;
import com.nasnav.dto.response.ServiceResponse;
import com.nasnav.persistence.ServiceEntity;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.yeshtery.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Package_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class ServiceControllerTest extends AbstractTestWithTempBaseDir {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private TestRestTemplate template;

    @Test
    public void getServicesList() {
        ResponseEntity<ServiceResponse[]> response = template.exchange("/v1/service", GET, getHttpEntity("abcdefg"), ServiceResponse[].class);
        assertEquals(OK, response.getStatusCode());
        assertEquals(4, response.getBody().length);
        ServiceResponse[] body = response.getBody();
        for (ServiceResponse pack : body) {
            assertTrue(pack.getId() > 0);
        }
    }

    @Test
    public void createServiceTest() {
        HttpEntity<?> json = getHttpEntity(serviceExample(), "abcdefg");
        ResponseEntity<ServiceResponse> response = template.postForEntity("/v1/service", json, ServiceResponse.class);
        assertEquals(200, response.getStatusCode().value());
        ServiceEntity serviceEntity = serviceRepository.findById(response.getBody().getId()).get();
        assertEquals("NEW_SERVICE", serviceEntity.getName());
    }

    private String serviceExample() {
        return json().put("code", "NEW_SERVICE")
                .put("name", "NEW_SERVICE")
                .put("description", "NEW_SERVICE")
                .put("enabled", true).toString();
    }

    @Test
    public void updateServiceTest() {
        String requestBody = json().put("name", "updated name ").toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<ServiceResponse> response = template.exchange("/v1/service/" + 99001L, PUT, json, ServiceResponse.class);
        assertEquals(200, response.getStatusCode().value());
        ServiceEntity serviceEntity = serviceRepository.findById(99001L).get();
        assertEquals("updated name ", serviceEntity.getName());
        assertEquals("THREE_SIXTY Service", serviceEntity.getDescription());
    }

    @Test
    public void updateInvalidServiceTest() {
        String requestBody = serviceExample();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.exchange("/v1/service/" + 990045L, PUT, json, Void.class);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void deleteServiceTest() {
        HttpEntity<?> json = getHttpEntity("abcdefg");
        ResponseEntity<Object[]> resBefore = template.exchange("/v1/service", GET, json, Object[].class);
        assertEquals(4, resBefore.getBody().length);
        ResponseEntity<Void> response = template.exchange("/v1/service/" + 99002L, DELETE, json, Void.class);
        assertEquals(200, response.getStatusCode().value());
        ResponseEntity<Object[]> resAfter = template.exchange("/v1/service", GET, json, Object[].class);
        assertEquals(3, resAfter.getBody().length);
    }

    @Test
    public void deleteInvalidServiceTest() {
        ResponseEntity<Void> response = template.exchange("/v1/service/" + 990045L, DELETE, getHttpEntity("abcdefg"), Void.class);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void getServiceById() {
        ResponseEntity<ServiceResponse> response = template.exchange("/v1/service/" + 99001, GET, getHttpEntity("abcdefg"), ServiceResponse.class);
        assertEquals(OK, response.getStatusCode());
    }

    @Test
    public void getOrganizationsByServiceIdTest() {
        ResponseEntity<ServiceResponse[]> response = template.exchange("/v1/service/org/99001", GET, getHttpEntity("abcdefg"), ServiceResponse[].class);
        assertEquals(OK, response.getStatusCode());
        assertEquals(3, response.getBody().length);
        ServiceResponse[] body = response.getBody();
        for (ServiceResponse pack : body) {
            assertTrue(pack.getId() > 0);
        }
    }

    @Test
    public void getOrgServicesByOrgId() {
        ResponseEntity<OrganizationServicesDto[]> response = template.exchange("/v1/service/org_service?orgId=" + 99001, GET, getHttpEntity("abcdefg"), OrganizationServicesDto[].class);
        assertEquals(OK, response.getStatusCode());
        assertEquals(3, response.getBody().length);
    }

    @Test
    public void getOrgServicesByServiceId() {
        ResponseEntity<OrganizationServicesDto[]> response = template.exchange("/v1/service/org_service?serviceId=" + 99001, GET, getHttpEntity("abcdefg"), OrganizationServicesDto[].class);
        assertEquals(OK, response.getStatusCode());
        assertEquals(1, response.getBody().length);
    }

    @Test
    public void updateOrgServices() {
        String requestBody = json().put("org_id", 99001).put("service_id", 99004).put("enabled", false).toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.exchange("/v1/service/org", PUT, json, Void.class);
        assertEquals(200, response.getStatusCode().value());

        ResponseEntity<OrganizationServicesDto[]> serviceResponse = template.exchange("/v1/service/org_service?orgId=99001&serviceId=99004", GET, getHttpEntity("abcdefg"), OrganizationServicesDto[].class);
        assertEquals(OK, serviceResponse.getStatusCode());
        assertEquals(1, serviceResponse.getBody().length);
        assertEquals(false, serviceResponse.getBody()[0].getEnabled());
    }

    @Test
    public void enablePackageServicesForOrganization() {
        ResponseEntity<Void> response = template.exchange("/v1/service/enable?packageId=99001&orgId=99001", POST, getHttpEntity("abcdefg"), Void.class);
        assertEquals(200, response.getStatusCode().value());
    }
}
