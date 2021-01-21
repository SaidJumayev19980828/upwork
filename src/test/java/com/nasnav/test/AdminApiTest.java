package com.nasnav.test;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Map;

import com.nasnav.dto.AreasRepObj;
import com.nasnav.dto.CitiesRepObj;
import com.nasnav.service.AddressService;
import com.nasnav.test.commons.TestCommons;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.dao.OrganizationDomainsRepository;
import com.nasnav.dto.CountriesRepObj;
import com.nasnav.persistence.OrganizationDomainsEntity;
import com.nasnav.service.AddressServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Admin_Api_Test_Data.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class AdminApiTest {

	 @Autowired
	 private TestRestTemplate template;
	 
	 @Autowired
	 private OrganizationDomainsRepository domainRepo;
	 
	 @Autowired
	 private AddressService addressService;

	 @Value("classpath:json/admin_api/update_countries_body.json")
	 private Resource updateCountriesBody;
	 
	 
	 @Test
	 public void updateDomainTest() {
		 String newDomain = "new.com";
		 Long orgId = 99001L;
		String requestBody = 
				json()
				.put("domain", newDomain)
				.put("organization_id", orgId)
				.toString();
		HttpEntity<?> json = getHttpEntity(requestBody,"abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/admin/organization/domain", json, Void.class);
        
        OrganizationDomainsEntity domainAfter =
				domainRepo
						.findByOrganizationEntity_IdOrderByIdDesc(orgId)
						.stream()
						.findFirst()
						.get();
        
        assertEquals(200, response.getStatusCode().value());
        assertEquals(newDomain, domainAfter.getDomain());
	 }
	 
	 
	 
	 
	 @Test
	 public void updateCountriesTest() throws IOException {
		String requestBody = TestCommons.readResourceFileAsString(updateCountriesBody);

		HttpEntity<?> json = getHttpEntity(requestBody,"abcdefg");
        ResponseEntity<Void> response = template.postForEntity("/admin/country/bulk", json, Void.class);
        
        assertEquals(200, response.getStatusCode().value());
        
        Map<String, CountriesRepObj> countries =  addressService.getCountries(false, null);
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
	
}
