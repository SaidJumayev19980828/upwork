package com.nasnav.test;

import com.nasnav.dao.PackageRegisteredRepository;
import com.nasnav.persistence.PackageRegisteredEntity;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Package_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class PackageTest extends AbstractTestWithTempBaseDir {

    @Autowired
    private PackageRegisteredRepository  packageRegisteredRepository;

    @Autowired
    private TestRestTemplate template;

    @Test
    public void getListPackageSuccess() {
        ResponseEntity<Object[]> response = template.exchange("/package", GET, null, Object[].class);
        assertEquals(OK, response.getStatusCode());
        assertEquals(2, response.getBody().length);
    }

    @Test
    public void createPackageTest() {
        String requestBody = json().put("name", "first name ").put("description", "description tes ").put("price", 1.5).toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/package/create", json, Void.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void updatePackageTest() {
        String requestBody = json().put("name", "updated name ").put("description", "description updated ").put("price", 2.5).toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.exchange("/package/" + 99001L, PUT, json, Void.class);
        ResponseEntity<Object[]> res = template.exchange("/package", GET, null, Object[].class);
        Map<String, Object> body = (Map<String, Object>) res.getBody()[1];
        String name = (String) body.get("name");
        assertEquals("updated name ", name);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void removePackageTest() {
        HttpEntity<?> json = getHttpEntity("abcdefg");
        ResponseEntity<Object[]> resBefore = template.exchange("/package", GET, null, Object[].class);
        assertEquals(2, resBefore.getBody().length);
        ResponseEntity<Void> response = template.exchange("/package/" + 99002L, DELETE, json, Void.class);
        ResponseEntity<Object[]> resAfter = template.exchange("/package", GET, null, Object[].class);
        assertEquals(1, resAfter.getBody().length);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testCompleteRegister() {
        String requestBody = json().put("package_id", 99001L).toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "123456");
        ResponseEntity<String> response = template.postForEntity("/package/complete-profile", json, String.class);
        assertEquals(200, response.getStatusCode().value());
        Long registrationId = Long.parseLong(response.getBody());
        PackageRegisteredEntity registation = packageRegisteredRepository.findById(registrationId).get();
        assertEquals(99002L, registation.getOrganization().getId().longValue());
        assertEquals(99001L, registation.getPackageEntity().getId().longValue());
        assertEquals(70L, registation.getCreatorEmployee().getId().longValue());

        requestBody = json().put("package_id", 99002L).toString();
        json = getHttpEntity(requestBody, "123456");
        response = template.postForEntity("/package/complete-profile", json, String.class);
        assertEquals(200, response.getStatusCode().value());
        registrationId = Long.parseLong(response.getBody());
        registation = packageRegisteredRepository.findById(registrationId).get();
        assertEquals(99002L, registation.getOrganization().getId().longValue());
        assertEquals(99002L, registation.getPackageEntity().getId().longValue());
        assertEquals(70L, registation.getCreatorEmployee().getId().longValue());
    }

}
