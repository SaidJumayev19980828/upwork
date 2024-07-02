package com.nasnav.test;

import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.PackageRegisteredRepository;
import com.nasnav.dao.PackageRepository;
import com.nasnav.dto.response.PackageResponse;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.PackageEntity;
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

import java.util.List;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Package_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class PackageTest extends AbstractTestWithTempBaseDir {

    @Autowired
    private PackageRegisteredRepository packageRegisteredRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private TestRestTemplate template;

    @Test
    public void getListPackageSuccess() {
        ResponseEntity<PackageResponse[]> response = template.exchange("/package", GET, null, PackageResponse[].class);
        assertEquals(OK, response.getStatusCode());
        assertEquals(2, response.getBody().length);
        PackageResponse[] body = response.getBody();
        for (PackageResponse pack : body) {
            assertTrue(pack.getId() > 0);
        }
    }

    @Test
    public void createPackageTest() {
        String requestBody = json().put("name", "first name").put("description", "description test").put("price", 1.5).put("period_in_days", 30).put("currency_iso", 818)
                .put("service_ids", servicesExample())
                .put("stripe_price_id", "price_1NzLNBGR4qGEOW4EItZ5eE2p")
                .toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<PackageResponse> response = template.postForEntity("/package", json, PackageResponse.class);
        assertEquals(200, response.getStatusCode().value());
        PackageEntity packageEntity = packageRepository.findById(response.getBody().getId()).get();
        assertEquals(packageEntity.getName(), "first name");
        assertEquals(packageEntity.getDescription(), "description test");
        assertTrue(packageEntity.getCountry().getIsoCode().equals(818));
        assertEquals("price_1NzLNBGR4qGEOW4EItZ5eE2p", packageEntity.getStripePriceId());
    }


    @Test
    public void createPackageWithMissingStripePriceIdTest() {
        String requestBody = json().put("name", "first name").put("description", "description test").put("price", 1.5).put("period_in_days", 30).put("currency_iso", 818)
                .put("services", servicesExample())
                .toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<PackageResponse> response = template.postForEntity("/package", json, PackageResponse.class);
        assertEquals(406, response.getStatusCode().value());
    }

    private List<Long> servicesExample() {
        return List.of(99001L, 99002L);
    }

    @Test
    public void createPackageWrongCurrencyTest() {
        String requestBody = json().put("name", "first name ").put("description", "description tes ").put("price", 1.5).put("currency_iso", 123464)
                .put("services", servicesExample())
                .put("stripe_price_id", "price_1NzLNBGR4qGEOW4EItZ5eE2p")
                .toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/package", json, Void.class);
        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updatePackageTest() {
        String requestBody = json().put("name", "updated name ").put("description", "description updated ").put("price", 2000).put("period_in_days", 40).put("currency_iso", 819)
                .put("service_ids", servicesExample())
                .put("stripe_price_id", "price_1NzLNBGR4qGEOW4EItZ5eE2p")
                .toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<PackageResponse> response = template.exchange("/package/" + 99001L, PUT, json, PackageResponse.class);
        assertEquals(200, response.getStatusCode().value());
        PackageEntity packageEntity = packageRepository.findById(99001L).get();
        assertEquals("updated name ", packageEntity.getName());
        assertEquals("description updated ", packageEntity.getDescription());
        assertEquals("price_1NzLNBGR4qGEOW4EItZ5eE2p", packageEntity.getStripePriceId());
    }

    @Test
    public void updatePackageWrongCurrencyTest() {
        String requestBody = json().put("name", "updated name ").put("description", "description updated ").put("price", 2000).put("period_in_days", 40).put("currency_iso", 123464).toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.exchange("/package/" + 99001L, PUT, json, Void.class);
        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateInvalidPackageTest() {
        String requestBody = json().put("name", "updated name ").put("description", "description updated ").put("price", 2000).put("period_in_days", 40).put("currency_iso", 819).toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.exchange("/package/" + 990045L, PUT, json, Void.class);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void removePackageTest() {
        HttpEntity<?> json = getHttpEntity("abcdefg");
        ResponseEntity<Object[]> resBefore = template.exchange("/package", GET, null, Object[].class);
        assertEquals(2, resBefore.getBody().length);
        ResponseEntity<Void> response = template.exchange("/package/" + 99002L, DELETE, json, Void.class);
        assertEquals(200, response.getStatusCode().value());
        ResponseEntity<Object[]> resAfter = template.exchange("/package", GET, null, Object[].class);
        assertEquals(1, resAfter.getBody().length);
    }

    @Test
    public void updateRegisteredPackageTest() {
        String requestBody = json().put("package_id", 99001L).put("organization_id", 99002).toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "123456");
        ResponseEntity response = template.postForEntity("/package/register", json, Void.class);
        assertEquals(200, response.getStatusCode().value());
        OrganizationEntity org = organizationRepository.findById(99002l).get();
        PackageRegisteredEntity registation = packageRegisteredRepository.findByOrganization(org).get();
        assertEquals(99002L, registation.getOrganization().getId().longValue());
        assertEquals(99001L, registation.getPackageEntity().getId().longValue());
        assertEquals(70L, registation.getCreatorEmployee().getId().longValue());

        requestBody = json().put("package_id", 99002L).put("organization_id", 99002).toString();
        json = getHttpEntity(requestBody, "123456");
        response = template.postForEntity("/package/register", json, Void.class);
        assertEquals(200, response.getStatusCode().value());
        registation = packageRegisteredRepository.findByOrganization(org).get();
        assertEquals(99002L, registation.getOrganization().getId().longValue());
        assertEquals(99002L, registation.getPackageEntity().getId().longValue());
        assertEquals(70L, registation.getCreatorEmployee().getId().longValue());
    }

    @Test
    public void updateRegisteredPackageNotValidPackageTest() {
        String requestBody = json().put("package_id", 123456L).put("organization_id", 99002).toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "123456");
        ResponseEntity response = template.postForEntity("/package/register", json, Void.class);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void getOrganizationsByPackageIdTest() {
        ResponseEntity<Object[]> response = template.exchange("/package/organizations/99001", GET, getHttpEntity("abcdefg"), Object[].class);
        assertEquals(OK, response.getStatusCode());
    }
}
