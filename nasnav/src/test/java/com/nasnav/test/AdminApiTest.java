package com.nasnav.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.ApiCallsRepository;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.CountryRepository;
import com.nasnav.dao.OrganizationDomainsRepository;
import com.nasnav.dto.AreasRepObj;
import com.nasnav.dto.CitiesRepObj;
import com.nasnav.dto.CountriesRepObj;
import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.request.BrandIdAndPriority;
import com.nasnav.dto.request.DomainUpdateDTO;
import com.nasnav.dto.response.ApiLogsDTO;
import com.nasnav.dto.response.ApiLogsResponse;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.persistence.OrganizationDomainsEntity;
import com.nasnav.service.AddressService;
import com.nasnav.service.AdminService;
import com.nasnav.test.commons.TestCommons;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.CollectionUtils.listsEqualsIgnoreOrder;
import static com.nasnav.test.commons.TestCommons.*;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Admin_Api_Test_Data.sql"})
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class AdminApiTest extends AbstractTestWithTempBaseDir {
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private OrganizationDomainsRepository domainRepo;
    @Autowired
    private CountryRepository countryRepo;
    @Autowired
    private AddressService addressService;
    @Value("classpath:json/admin_api/update_countries_body.json")
    private Resource updateCountriesBody;
    @Autowired
    private ObjectMapper jsonMapper;
    @Autowired
    private AdminService adminService;
    @Autowired
    private BrandsRepository brandsRepository;
    @Autowired
    private ApiCallsRepository apiCallsRepo;

    @Before
    public void clearCache(){
        adminService.invalidateCaches();
    }

    @Test
    public void changeBrandsPriorityTest(){

        List<BrandIdAndPriority> brands = new ArrayList<>();

        brands.add(new BrandIdAndPriority(101L, 2));
        brands.add(new BrandIdAndPriority(102L, 3));
        brands.add(new BrandIdAndPriority(103L, 4));
        brands.add(new BrandIdAndPriority(104L, 1));

        String requestBody =
                    json()
                        .put("", brands)
                        .toString();


        String trimmedString = requestBody.substring(4, requestBody.length() - 1);

        HttpEntity<?> json = getHttpEntity(trimmedString, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/admin/priority/brands", json, Void.class);

        Iterable<BrandsEntity> allBrands = brandsRepository.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        for (BrandsEntity brand : allBrands){
            Integer priority = brands.stream().filter(b -> b.getId().equals(brand.getId())).findFirst().get().getPriority();

            assertEquals(priority, brand.getPriority());
        }
    }

    @Test
    public void createDomainTest() {
        String newDomain = "new.com";
        Long orgId = 99001L;
        String requestBody =
                json()
                        .put("domain", newDomain)
                        .put("organization_id", orgId)
                        .put("priority", 1)
                        .toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/admin/organization/domain", json, Void.class);

        OrganizationDomainsEntity domainAfter =
                domainRepo
                        .findByOrganizationEntity_IdOrderByPriorityDescIdDesc(orgId)
                        .stream()
                        .findFirst()
                        .get();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(newDomain, domainAfter.getDomain());
    }

    @Test
    public void createDomainWithProtocolTwice() {
        String newDomain = "https://www.new.com";
        Long orgId = 99001L;
        String requestBody =
                json()
                        .put("domain", newDomain)
                        .put("organization_id", orgId)
                        .put("priority", 1)
                        .toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/admin/organization/domain", json, Void.class);
        assertEquals(200, response.getStatusCode().value());

        response = template.postForEntity("/admin/organization/domain", json, Void.class);
        assertEquals(406, response.getStatusCodeValue());
    }

    @Test
    public void createDomainInvalidUrl() {
        String newDomain = "{new.com";
        Long orgId = 99001L;
        String requestBody =
                json()
                        .put("domain", newDomain)
                        .put("organization_id", orgId)
                        .toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/admin/organization/domain", json, Void.class);

        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createDomainWithProtocol() {
        String newDomain = "https://www.new.com";
        Long orgId = 99001L;
        String requestBody =
                json()
                        .put("domain", newDomain)
                        .put("organization_id", orgId)
                        .put("priority", 1)
                        .toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/admin/organization/domain", json, Void.class);
        OrganizationDomainsEntity domainAfter =
                domainRepo
                        .findByOrganizationEntity_IdOrderByPriorityDescIdDesc(orgId)
                        .stream()
                        .findFirst()
                        .get();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("www.new.com", domainAfter.getDomain());
    }



    @Test
    public void createDomainRepeatedDomainAndSubdir() {
        String newDomain = "fortune.nasnav.com";
        Long orgId = 99001L;
        String requestBody =
                json()
                .put("domain", newDomain)
                .put("organization_id", orgId)
                .toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/admin/organization/domain", json, Void.class);

        assertEquals(406, response.getStatusCode().value());
    }



    @Test
    public void updateDomainTest() {
        String newDomain = "new.com";
        Long orgId = 99001L;
        Long domainId = 150002L;
        String requestBody =
                json()
                    .put("id", domainId)
                    .put("domain", newDomain)
                    .put("organization_id", orgId)
                    .put("canonical", 1)
                    .toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/admin/organization/domain", json, Void.class);

        OrganizationDomainsEntity domainAfter = domainRepo.findById(domainId).get();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(newDomain, domainAfter.getDomain());
    }



    @Test
    public void updateDomainInvalidAuthN() {
        String requestBody = "";
        HttpEntity<?> json = getHttpEntity(requestBody, "Invalid");
        ResponseEntity<Void> response = template.postForEntity("/admin/organization/domain", json, Void.class);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    public void updateDomainInvalidAuthZ() {
        String requestBody = "";
        HttpEntity<?> json = getHttpEntity(requestBody, "123456");
        ResponseEntity<Void> response = template.postForEntity("/admin/organization/domain", json, Void.class);
        assertEquals(403, response.getStatusCodeValue());
    }


    @Test
    public void deleteDomainTest() {
        HttpEntity<?> json = getHttpEntity("abcdefg");
        ResponseEntity<Void> response = template.exchange("/admin/organization/domain?id=1&org_id=99001", DELETE, json, Void.class);
        assertEquals(200, response.getStatusCodeValue());
        assertFalse(domainRepo.existsById(1L));
    }

    @Test
    public void deleteDomainInvalidAuthN() {
        HttpEntity<?> json = getHttpEntity("invalid");
        ResponseEntity<Void> response = template.exchange("/admin/organization/domain?id=1&org_id=99001", DELETE, json, Void.class);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    public void deleteDomainInvalidAuthA() {
        HttpEntity<?> json = getHttpEntity("123456");
        ResponseEntity<Void> response = template.exchange("/admin/organization/domain?id=1&org_id=99001", DELETE, json, Void.class);
        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    public void deleteDomainNonExistId() {
        Long countBefore = domainRepo.count();
        HttpEntity<?> json = getHttpEntity("abcdefg");
        ResponseEntity<Void> response = template.exchange("/admin/organization/domain?id=3&org_id=99001", DELETE, json, Void.class);

        Long countAfter = domainRepo.count();
        assertEquals(countBefore, countAfter);
    }

    @Test
    public void listOrgDomains() throws IOException {
        HttpEntity<?> json = getHttpEntity("abcdefg");
        ResponseEntity<String> response = template.exchange("/admin/organization/domains?org_id=99001", GET, json, String.class);
        List<DomainUpdateDTO> body = jsonMapper.readValue(response.getBody(), new TypeReference<List<DomainUpdateDTO>>(){});
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("https://www.fortune.com", body.get(0).getDomain());
        assertEquals("https://fortune.nasnav.com", body.get(1).getDomain());
    }

    @Test
    public void listOrgDomainsByOrgAdmin() throws IOException {
        HttpEntity<?> json = getHttpEntity("hijkllm");
        ResponseEntity<String> response = template.exchange("/admin/organization/domains?org_id=99001", GET, json, String.class);
        List<DomainUpdateDTO> body = jsonMapper.readValue(response.getBody(), new TypeReference<List<DomainUpdateDTO>>(){});
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("https://www.fortune.com", body.get(0).getDomain());
        assertEquals("https://fortune.nasnav.com", body.get(1).getDomain());
    }


    @Test
    public void updateCountriesTest() throws IOException {
        String requestBody = TestCommons.readResourceFileAsString(updateCountriesBody);

        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/admin/country/bulk", json, Void.class);

        assertEquals(200, response.getStatusCode().value());

        Map<String, CountriesRepObj> countries = addressService.getCountries(false, null);
        CountriesRepObj egypt = countries.get("Egypt");

        assertEquals(1, countries.size());
        assertNotNull(egypt);

        assertEquals(2, egypt.getCities().size());
        CitiesRepObj alex = egypt.getCities().get("Alexandria");
        assertNotNull(alex);

        assertEquals(1, alex.getAreas().size());
        AreasRepObj abuKir = alex.getAreas().get("Abu Kir");
        assertNotNull(abuKir);
    }


    @Test
    public void createCountryMissingValues() {
        //missing iso_code
        String requestBody = createCountryJsonBody()
                .put("iso_code", JSONObject.NULL)
                .toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/admin/country", json, Void.class);
        assertEquals(406, response.getStatusCodeValue());

        //missing id
        requestBody = createCountryJsonBody()
                .put("id", JSONObject.NULL)
                .toString();

        json = getHttpEntity(requestBody, "abcdefg");
        response = template.postForEntity("/admin/country", json, Void.class);
        assertEquals(406, response.getStatusCodeValue());

        //missing name
        requestBody = createCountryJsonBody()
                .put("name", JSONObject.NULL)
                .toString();

        json = getHttpEntity(requestBody, "abcdefg");
        response = template.postForEntity("/admin/country", json, Void.class);
        assertEquals(406, response.getStatusCodeValue());

        //missing currency
        requestBody = createCountryJsonBody()
                .put("currency", JSONObject.NULL)
                .toString();

        json = getHttpEntity(requestBody, "abcdefg");
        response = template.postForEntity("/admin/country", json, Void.class);
        assertEquals(406, response.getStatusCodeValue());
    }

    private JSONObject createCountryJsonBody() {
        return json()
                .put("id", 3)
                .put("name", "Ireland")
                .put("currency", "IR")
                .put("type", "country")
                .put("iso_code", 111);
    }

    @Test
    public void createCountrySuccess() {
        String requestBody = createCountryJsonBody()
                .toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/admin/country", json, Void.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(countryRepo.findById(3L).isPresent());
    }

    @Test
    public void getOrganizationWithDifferentDomains() {
        ResponseEntity<OrganizationRepresentationObject> response =
                template.getForEntity("/navbox/organization?url=www.fortune.com", OrganizationRepresentationObject.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(Objects.equals(99001L, response.getBody().getId()));

        response =
                template.getForEntity("/navbox/organization?url=fortune.nasnav.com", OrganizationRepresentationObject.class);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(Objects.equals(99001L, response.getBody().getId()));
    }

    @Test
    public void checkApiLogsFilterTest(){
        String token = "abcdefg";
        String requestBody = createCountryJsonBody()
                .toString();

        HttpEntity<?> request = getHttpEntity(requestBody, token);
        template.postForEntity("/admin/country", request, Void.class);
        template.exchange("/order/meta_order/info?id=310001", GET, request, String.class);
        template.exchange("/order/list", GET, request, String.class);

        wait(3);

        int numberOfLogs = apiCallsRepo.findAll().size();

        assertEquals(3, numberOfLogs);
    }

    private void wait(int seconds){
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Api_Logs_Test_Data.sql"})
    @Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getApiLogsFilterByCount(){
        String token = "abcdefg";
        ResponseEntity<ApiLogsResponse> response = sendGetApiLogsRequest("start=0&count=2", token);

        assertEquals(OK, response.getStatusCode());
        assertEquals(2, response.getBody().getApiLogs().size());
        assertResponseResultCount(response, 10L);
    }

    @Test
    @Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Api_Logs_Test_Data.sql"})
    @Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getApiLogsFilterByAllEmployees(){
        String token = "abcdefg";
        ResponseEntity<ApiLogsResponse> response = sendGetApiLogsRequest("employees=true&start=1", token);

        assertEquals(OK, response.getStatusCode());
        assertResponseResultCount(response, 4L);
        assertResponseResultIds(response, 101L, 103L, 107L);
    }

    @Test
    @Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Api_Logs_Test_Data.sql"})
    @Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getApiLogsFilterByAllCustomers(){
        String token = "abcdefg";
        ResponseEntity<ApiLogsResponse> response = sendGetApiLogsRequest("employees=false", token);

        assertEquals(OK, response.getStatusCode());
        assertResponseResultCount(response, 6L);
        assertResponseResultIds(response, 102L, 104L, 105L, 106L, 108L, 109L);
    }

    @Test
    @Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Api_Logs_Test_Data.sql"})
    @Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getApiLogsFilterByCustomers(){
        String token = "abcdefg";
        ResponseEntity<ApiLogsResponse> response = sendGetApiLogsRequest("employees=false&users=71&users=72", token);

        assertEquals(OK, response.getStatusCode());
        assertResponseResultCount(response, 5L);
        assertResponseResultIds(response, 102L, 104L, 105L, 106L, 108L);
    }

    @Test
    @Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Api_Logs_Test_Data.sql"})
    @Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getApiLogsFilterByEmployees(){
        String token = "abcdefg";
        ResponseEntity<ApiLogsResponse> response = sendGetApiLogsRequest("employees=true&users=68&users=69&start=1", token);

        assertEquals(OK, response.getStatusCode());
        assertResponseResultCount(response, 4L);
        assertResponseResultIds(response, 101L, 103L, 107L);
    }

    @Test
    @Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Api_Logs_Test_Data.sql"})
    @Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getApiLogsFilterByCallDate(){
        String token = "abcdefg";
        ResponseEntity<ApiLogsResponse> response = sendGetApiLogsRequest("created_after=2022-06-30:23:59:00&created_before=2022-07-04:12:12:12", token);

        assertEquals(OK, response.getStatusCode());
        assertResponseResultCount(response, 5L);
        assertResponseResultIds(response, 101L, 102L, 103L, 104L, 105L);
    }

    @Test
    @Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Api_Logs_Test_Data.sql"})
    @Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
    public void getApiLogsFilterByOrganization(){
        String token = "abcdefg";
        ResponseEntity<ApiLogsResponse> response = sendGetApiLogsRequest("organizations=99001&start=1", token);

        assertEquals(OK, response.getStatusCode());
        assertResponseResultCount(response, 5L);
        assertResponseResultIds(response, 101L, 104L, 107L, 108L);
    }

    private ResponseEntity<ApiLogsResponse> sendGetApiLogsRequest(String parameters, String token){
        HttpEntity<Object> httpEntity = getHttpEntity(token);
        String url = "/admin/api/logs?" + parameters ;
        return template.exchange(url, GET, httpEntity, ApiLogsResponse.class);
    }

    private void assertResponseResultCount(ResponseEntity<ApiLogsResponse> response, Long expected){
        ApiLogsResponse responseBody = response.getBody();
        assertEquals(expected, responseBody.getTotal());
    }

    private void assertResponseResultIds(ResponseEntity<ApiLogsResponse> response, Long ...expectedIds){
        ApiLogsResponse responseBody = response.getBody();
        List<Long> responseIds = responseBody.getApiLogs()
                                             .stream()
                                             .map(ApiLogsDTO::getId)
                                             .collect(Collectors.toList());

        assertTrue(listsEqualsIgnoreOrder(responseIds, List.of(expectedIds)));
    }
}
