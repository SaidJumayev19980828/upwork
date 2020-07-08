package com.nasnav.test;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.dao.OrganizationDomainsRepository;
import com.nasnav.persistence.OrganizationDomainsEntity;

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
        
        OrganizationDomainsEntity domainAfter = domainRepo.findByOrganizationEntity_Id(orgId).get();
        
        assertEquals(200, response.getStatusCode().value());
        assertEquals(newDomain, domainAfter.getDomain());
	 }
	
}
