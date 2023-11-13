package com.nasnav.test;

import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.PackageRegisteredRepository;
import com.nasnav.dao.PackageRepository;
import com.nasnav.dao.ServiceRepository;
import com.nasnav.dto.request.ServiceDTO;
import com.nasnav.dto.response.PackageResponse;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.PackageEntity;
import com.nasnav.persistence.PackageRegisteredEntity;
import com.nasnav.persistence.ServiceEntity;
import com.nasnav.service.PackageService;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.Set;

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
    private PackageRegisteredRepository  packageRegisteredRepository;

    @Autowired
    private PackageRepository packageRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PackageService packageService;

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
                .put("services", servicesExample())
                .put("stripe_price_id","price_1NzLNBGR4qGEOW4EItZ5eE2p")
                .toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<PackageResponse> response = template.postForEntity("/package/create", json, PackageResponse.class);
        assertEquals(200, response.getStatusCode().value());
        PackageEntity packageEntity = packageRepository.findById(response.getBody().getId()).get();
        assertEquals(packageEntity.getName(), "first name");
        assertEquals(packageEntity.getDescription(), "description test");
        assertTrue(packageEntity.getCountry().getIsoCode().equals(818));
        assertEquals("price_1NzLNBGR4qGEOW4EItZ5eE2p", packageEntity.getStripePriceId());
        validateServices(packageEntity.getId(),servicesExample());
    }


    @Test
    public void createPackageWithMissingStripePriceIdTest() {
        String requestBody = json().put("name", "first name").put("description", "description test").put("price", 1.5).put("period_in_days", 30).put("currency_iso", 818)
                .put("services", servicesExample())
                .toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<PackageResponse> response = template.postForEntity("/package/create", json, PackageResponse.class);
        assertEquals(406, response.getStatusCode().value());
    }

    private Set<ServiceDTO> servicesExample(){
           return Set.of(
                new ServiceDTO("THREE_SIXTY_TEST"),
                new ServiceDTO("CHAT_SERVICES_TEST"),
                new ServiceDTO("MET_AVERSE_TEST")
        );
    }

    @Transactional
    public void validateServices(Long package_id, Set<ServiceDTO> services){
        Set<ServiceEntity> servicesEntitySet =  serviceRepository.findAllByPackageEntity_Id(package_id);
        for(ServiceDTO serviceDTO : services){
            Assert.assertTrue(!servicesEntitySet.stream().filter(serviceEntity -> serviceEntity.getCode().equals(serviceDTO.getCode())).findFirst().isEmpty());
        }
    }

    @Test
    public void createPackageWrongCurrencyTest() {
        String requestBody = json().put("name", "first name ").put("description", "description tes ").put("price", 1.5).put("currency_iso", 123464)
                .put("services", servicesExample())
                .put("stripe_price_id","price_1NzLNBGR4qGEOW4EItZ5eE2p")
                .toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/package/create", json, Void.class);
        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updatePackageTest() {
        String requestBody = json().put("name", "updated name ").put("description", "description updated ").put("price", 2000).put("period_in_days", 40).put("currency_iso", 819)
                .put("services", servicesExample())
                .put("stripe_price_id","price_1NzLNBGR4qGEOW4EItZ5eE2p")
                .toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<PackageResponse> response = template.exchange("/package/" + 99001L, PUT, json, PackageResponse.class);
        assertEquals(200, response.getStatusCode().value());
        PackageEntity packageEntity = packageRepository.findById(99001L).get();
        assertEquals("updated name ", packageEntity.getName());
        assertEquals("description updated ", packageEntity.getDescription());
        assertEquals("price_1NzLNBGR4qGEOW4EItZ5eE2p", packageEntity.getStripePriceId());
        validateServices(99001L,servicesExample());
    }

    @Test
    public void updatePackageWithMissingStripePriceIdTest() {
        String requestBody = json().put("name", "updated name ").put("description", "description updated ").put("price", 2000).put("period_in_days", 40).put("currency_iso", 819)
                .put("services", servicesExample())
                .toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<PackageResponse> response = template.exchange("/package/" + 99001L, PUT, json, PackageResponse.class);
        assertEquals(406, response.getStatusCode().value());
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
        ResponseEntity<Object[]> resAfter = template.exchange("/package", GET, null, Object[].class);
        assertEquals(1, resAfter.getBody().length);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void updateRegisteredPackageTest() {
        String requestBody = json().put("package_id", 99001L).toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "123456");
        ResponseEntity response = template.postForEntity("/package/register-package-profile", json, Void.class);
        assertEquals(200, response.getStatusCode().value());
        OrganizationEntity org = organizationRepository.findById(99002l).get();
        PackageRegisteredEntity registation = packageRegisteredRepository.findByOrganization(org).get();
        assertEquals(99002L, registation.getOrganization().getId().longValue());
        assertEquals(99001L, registation.getPackageEntity().getId().longValue());
        assertEquals(70L, registation.getCreatorEmployee().getId().longValue());

        requestBody = json().put("package_id", 99002L).toString();
        json = getHttpEntity(requestBody, "123456");
        response = template.postForEntity("/package/register-package-profile", json, Void.class);
        assertEquals(200, response.getStatusCode().value());
        registation = packageRegisteredRepository.findByOrganization(org).get();
        assertEquals(99002L, registation.getOrganization().getId().longValue());
        assertEquals(99002L, registation.getPackageEntity().getId().longValue());
        assertEquals(70L, registation.getCreatorEmployee().getId().longValue());
    }

    @Test
    public void updateRegisteredPackageNotValidPackageTest() {
        String requestBody = json().put("package_id", 123456L).toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "123456");
        ResponseEntity response = template.postForEntity("/package/register-package-profile", json, Void.class);
        assertEquals(404, response.getStatusCode().value());
    }
}
