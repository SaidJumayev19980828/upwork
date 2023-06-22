package com.nasnav.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.nasnav.dao.*;
import com.nasnav.dto.ExtraAttributeDTO;
import com.nasnav.dto.ExtraAttributeDefinitionDTO;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.dto.SubAreasRepObj;
import com.nasnav.dto.request.shipping.ShippingServiceRegistration;
import com.nasnav.persistence.*;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.shipping.services.DummyShippingService;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import static com.nasnav.enumerations.ExtraAttributeType.INVISIBLE;
import static com.nasnav.enumerations.ExtraAttributeType.STRING;
import static com.nasnav.test.commons.TestCommons.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@RunWith(SpringRunner.class)
@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert.sql","/sql/Extra_Features_Data_Insert.sql"})
@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class OrganizationManagementTest extends AbstractTestWithTempBaseDir {
    private final String NASNAV_EXTRA_ATTRIBUTES_API_PATH = "/organization/extra_attribute";


    @Value("classpath:sql/database_cleanup.sql")
    private Resource databaseCleanup;
    @Value("classpath:test_imgs_to_upload/nasnav--Test_Photo.png")
    private Resource file;
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ExtraAttributesRepository extraAttrRepo;
    @Autowired
    private ProductExtraAttributesEntityRepository productExtraAttrRepo;
    @Autowired
    private SocialRepository socialRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubAreaRepository subAreaRepo;

    @Autowired
    private AddressRepository addressRepo;

    @Test
    public void updateOrganizationDataSuccessTest() {
        String facebookUrl = "https://www.facebook.com/fortune.stores11/";
        String twitterUrl = "https://www.twitter.com/fortunestores/";
        String instagramUrl = "https://www.instagram.com/islamify/";
        String youtubeUrl = "https://www.youtube.com/";
        String linkedinUrl = "https://www.linkedin.com/";
        String pinterestUrl = "https://www.pinterest.com/";
        String whatsappUrl = "https://www.whatsapp.com/";
        String body = json()
                .put("description", "this company is old and unique")
                .put("social_twitter", twitterUrl)
                .put("social_facebook", facebookUrl)
                .put("social_instagram",instagramUrl)
                .put("social_youtube", youtubeUrl)
                .put("social_linkedin", linkedinUrl)
                .put("social_pinterest", pinterestUrl)
                .put("social_whatsapp", whatsappUrl)
                .toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("properties", body);
        map.add("logo", file);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MULTIPART_FORM_DATA);
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        assertEquals(200, response.getStatusCode().value());
        SocialEntity socialEntity = socialRepository.findOneByOrganizationEntity_Id(99001L).get();
        assertEquals(twitterUrl, socialEntity.getTwitter());
        assertEquals(facebookUrl, socialEntity.getFacebook());
        assertEquals(instagramUrl, socialEntity.getInstagram());
        assertEquals(youtubeUrl, socialEntity.getYoutube());
        assertEquals(linkedinUrl, socialEntity.getLinkedin());
        assertEquals(pinterestUrl, socialEntity.getPinterest());
        assertEquals(whatsappUrl, socialEntity.getWhatsapp());
    }




    //trying to update organization with nasnav_admin user
    @Test
    public void updateOrganizationUnauthorizedUserTest() {
        String body = "{\"org_id\":99001, \"description\":\"this company is o8895ssffld and unique\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("properties", body);
        map.add("logo", file);
        HttpEntity<Object> json = getHttpEntity(map,"abcdefg", MULTIPART_FORM_DATA);
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        assertEquals(403, response.getStatusCode().value());
    }


    @Test
    public void deleteOrganizationSocialLinksTest() {
        // insert social link first
        String body = json()
                .put("social_twitter", "htps://www.twitte.com/fortunestores")
                .toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("properties", body);
        map.add("logo", file);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("htps://www.twitte.com/fortunestores", socialRepository.findOneByOrganizationEntity_Id(99001L).get().getTwitter());

        // try to remove social link
        body = json()
                .put("social_twitter", "")
                .toString();
        map = new LinkedMultiValueMap<>();
        map.add("properties", body);
        map.add("logo", file);
        json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        assertEquals(200, response.getStatusCode().value());
        assertNull(socialRepository.findOneByOrganizationEntity_Id(99001L).get().getTwitter());
    }

    @Test
    public void updateOrganizationInvalidLogoTest() {
        String body = "{\"org_id\":99001, \"description\":\"this company is old and unique\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("properties", body);
        map.add("logo", databaseCleanup);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        assertEquals(406, response.getStatusCode().value());
    }

    private String createOrgJson() {
        return json()
                .put("name", "Solad Pant")
                .put("p_name", "solad-pant-trello")
                .toString();
    }

    @Test
    public void createOrganizationSuccessTest() {
        String body = createOrgJson();
        HttpEntity<Object> json = getHttpEntity(body,"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization", json,
                OrganizationResponse.class);
        organizationRepository.deleteById(response.getBody().getOrganizationId());
        assertEquals(200, response.getStatusCode().value());
    }


    @Test
    public void createOrganizationMissingValuesTest() {
        String body = json()
                .put("name", "Solad Pant")
                .toString();
        HttpEntity<Object> json = getHttpEntity(body, "abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization", json,
                OrganizationResponse.class);
        assertEquals(406, response.getStatusCode().value());

        body = "{\"name\":\"Solad Pant\"}";
        json = getHttpEntity(body,"abcdefg");
        response = template.postForEntity("/admin/organization", json, OrganizationResponse.class);
        assertEquals(406, response.getStatusCode().value());
    }


    @Test
    public void createOrganizationInvalidValuesTest() {
        String body = json()
                .put("name", "23Solad Pant#")
                .put("p_name", "solad-pant")
                .toString();
        HttpEntity<Object> json = getHttpEntity(body,"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization", json,
                OrganizationResponse.class);
        assertEquals(406, response.getStatusCode().value());

        body = "{\"name\":\"Solad Pant\", \"p_name\":\"solad_pant#$!^*\"}";
        json = getHttpEntity(body,"abcdefg");
        response = template.postForEntity("/admin/organization", json, OrganizationResponse.class);
        assertEquals(406, response.getStatusCode().value());
    }

    //trying to create organization with organization_admin user
    @Test
    public void createOrganizationUnauthorizedUserTest() {
        String body = createOrgJson();
        HttpEntity<Object> json = getHttpEntity(body,"hijkllm");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization", json,
                OrganizationResponse.class);
        assertEquals(403, response.getStatusCode().value());
    }


    @Test
    public void getOrgByURLTestSuccess() throws URISyntaxException {
        URI url = new URI("http://www.fortune.nasnav.com/");
        ResponseEntity<String> response = template.getForEntity("/navbox/orgid?url="+ url, String.class);

        checkSuccessResponse(response);

        url = new URI("https://www.nasnav.com/fortune/product/74");
        response = template.getForEntity("/navbox/orgid?url="+ url, String.class);

        checkSuccessResponse(response);

        url = new URI("www.fortune-egypt.com/categories/");
        response = template.getForEntity("/navbox/orgid?url="+ url, String.class);

        checkSuccessResponse(response);
    }

    @Test
    public void getOrgByURLNoOrgTest() throws URISyntaxException {
        URI url = new URI("http://www.invaliddomain.nasnav.com/");
        ResponseEntity<String> response = template.getForEntity("/navbox/orgid?url="+ url, String.class);
        checkFailResponse(response);

        url = new URI("https://www.nasnav.com/invaliddomain/product/74");
        response = template.getForEntity("/navbox/orgid?url="+ url, String.class);
        checkFailResponse(response);

        url = new URI("https://www.invaliddomain-egypt.com/categories/");
        response = template.getForEntity("/navbox/orgid?url="+ url, String.class);
        checkFailResponse(response);
    }


    @Test
    public void updateOrgNameTest () {
        JSONObject body = json().put("id", 99001)
                                .put("name", "new org name")
                                .put("p_name", "org-name");
        HttpEntity<Object> json = getHttpEntity(body.toString(),"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization",
                json, OrganizationResponse.class);
        assertEquals(200, response.getStatusCode().value());
        OrganizationEntity org = organizationRepository.findOneById(99001L);
        assertEquals("new org name", org.getName());
        assertEquals("org-name", org.getPname());
    }


    @Test
    public void updateOrgEcommerceTest () {
        JSONObject body = json().put("id", 99001)
                .put("ecommerce", 1);
        HttpEntity<Object> json = getHttpEntity(body.toString(),"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization",
                json, OrganizationResponse.class);
        assertEquals(200, response.getStatusCode().value());
        OrganizationEntity org = organizationRepository.findOneById(99001L);
        assertEquals(1, org.getEcommerce().intValue());
    }


    @Test
    public void updateOrgGoogleTokenTest () {
        JSONObject body = json().put("id", 99001)
                .put("google_token", "tokeee-eeee-eeeee-eeen");
        HttpEntity<Object> json = getHttpEntity(body.toString(),"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization",
                json, OrganizationResponse.class);
        assertEquals(200, response.getStatusCode().value());
        OrganizationEntity org = organizationRepository.findOneById(99001L);
        assertEquals("tokeee-eeee-eeeee-eeen", org.getGoogleToken());
    }


    @Test
    public void updateOrgCurrencyIsoTest () {
        JSONObject body = json().put("id", 99001)
                .put("currency_iso", 818);
        HttpEntity<Object> json = getHttpEntity(body.toString(),"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization",
                json, OrganizationResponse.class);
        assertEquals(200, response.getStatusCode().value());
        OrganizationEntity org = organizationRepository.findOneById(99001L);
        assertEquals(818, org.getCountry().getIsoCode().intValue());
    }

    @Test
    public void updateOrgMatomoIdTest() {
        JSONObject body = json()
                .put("id", 99001)
                .put("matomo_site_id", 123);
        HttpEntity<Object> json = getHttpEntity(body.toString(),"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization",
                json, OrganizationResponse.class);
        assertEquals(200, response.getStatusCode().value());
        OrganizationEntity org = organizationRepository.findOneById(99001L);
        assertEquals(123, org.getMatomoId().intValue());
    }

    @Test
    public void updateOrgFacebookPixelTest() {
        JSONObject body = json()
                .put("id", 99001)
                .put("pixel_site_id", 123);
        HttpEntity<Object> json = getHttpEntity(body.toString(),"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization",
                json, OrganizationResponse.class);
        assertEquals(200, response.getStatusCode().value());
        OrganizationEntity org = organizationRepository.findOneById(99001L);
        assertEquals("123", org.getPixelId());
    }


    @Test
    public void deleteVariantExtraAttribute() {
        //deleting extra attribute which not attached to any variant
        HttpEntity<?> req = getHttpEntity("123456");
        ResponseEntity<String> res = template.exchange( NASNAV_EXTRA_ATTRIBUTES_API_PATH + "?attr_id=11002",
                DELETE, req, String.class);
        assertEquals(200, res.getStatusCodeValue());
        assertFalse(extraAttrRepo.existsByIdAndOrganizationId(11001, 99002L));
    }


    @Test
    public void deleteVariantExtraAttributeNonExistInSameOrg() {
        HttpEntity<?> req = getHttpEntity("hijkllm");
        ResponseEntity<String> res = template.exchange(NASNAV_EXTRA_ATTRIBUTES_API_PATH + "?attr_id=11001",
                DELETE, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }


    @Test
    public void deleteVariantExtraAttributeNoAuthZ() {
        HttpEntity<?> req = getHttpEntity("abcdefg");
        ResponseEntity<String> res = template.exchange(NASNAV_EXTRA_ATTRIBUTES_API_PATH + "?attr_id=11001",
                DELETE, req, String.class);
        assertEquals(403, res.getStatusCodeValue());
    }


    @Test
    public void deleteVariantExtraAttributeNoAuthN() {
        HttpEntity<?> req = getHttpEntity("noneexist");
        ResponseEntity<String> res = template.exchange(NASNAV_EXTRA_ATTRIBUTES_API_PATH + "?attr_id=11002",
                DELETE, req, String.class);
        assertEquals(401, res.getStatusCodeValue());
    }


    @Test
    public void deleteVariantExtraAttributeAttachedVariant() {
    	assertTrue(extraAttrRepo.existsByIdAndOrganizationId(11003, 99002L));
        assertTrue(productExtraAttrRepo.existsById(11003L));
        
        //deleting extra attribute attached to variant #310002
        HttpEntity<?> req = getHttpEntity("123456");
        ResponseEntity<String> res = template.exchange(NASNAV_EXTRA_ATTRIBUTES_API_PATH + "?attr_id=11003",
                DELETE, req, String.class);
        assertEquals(200, res.getStatusCodeValue());
        assertFalse(extraAttrRepo.existsByIdAndOrganizationId(11003, 99002L));
        assertFalse(productExtraAttrRepo.existsById(11003L));
    }
    
    
    
    @Test
    public void deleteVariantExtraAttributeOfDeletedProduct() {
    	assertTrue(extraAttrRepo.existsByIdAndOrganizationId(11004, 99002L));
        assertTrue(productExtraAttrRepo.existsById(11004L));
        
        //deleting extra attribute attached to variant #310002
        HttpEntity<?> req = getHttpEntity("123456");
        ResponseEntity<String> res = template.exchange(NASNAV_EXTRA_ATTRIBUTES_API_PATH + "?attr_id=11004",
                DELETE, req, String.class);
        assertEquals(200, res.getStatusCodeValue());
        assertFalse(extraAttrRepo.existsByIdAndOrganizationId(11004, 99002L));
        assertFalse(productExtraAttrRepo.existsById(11004L));
    }


    private void checkSuccessResponse(ResponseEntity<String> response) {
        JSONObject json = new JSONObject(response.getBody());

        assertEquals(200, response.getStatusCode().value());
        assertEquals(99001, json.getInt("id"));
    }

    private void checkFailResponse(ResponseEntity<String> response) {
        JSONObject json = new JSONObject(response.getBody());

        assertEquals(200, response.getStatusCode().value());
        assertEquals(0, json.getInt("id"));
    }
    
    
    
    
    @Test
    public void getOrganizationShippingServiceNoAuthZ() {
        HttpEntity<?> req = getHttpEntity("abcdefg");
        ResponseEntity<String> res = 
        		template.exchange("/organization/shipping/service", GET, req, String.class);
        assertEquals(403, res.getStatusCodeValue());
    }


    
    @Test
    public void getOrganizationShippingServiceNoAuthN() {
        HttpEntity<?> req = getHttpEntity("NotExist");
        ResponseEntity<String> res = 
        		template.exchange("/organization/shipping/service", GET, req, String.class);
        assertEquals(401, res.getStatusCodeValue());
    }
    
    
    
    
    @Test
    public void getOrganizationShippingService() throws Exception {
        HttpEntity<?> req = getHttpEntity("hijkllm");
        ResponseEntity<String> res = 
        		template.exchange("/organization/shipping/service", GET, req, String.class);
        
        assertEquals(200, res.getStatusCodeValue());
        
        List<ShippingServiceRegistration> services = objectMapper.readValue(res.getBody(), new TypeReference<>() {});
        assertEquals(1, services.size());
        assertEquals(DummyShippingService.ID, services.get(0).getServiceId());
        assertFalse(services.get(0).getServiceParameters().isEmpty());
    }
    
    
    
    
    
    @Test
    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/ExtraAttributes_Test_Data_Insert.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void getOrganizationShops() throws Exception {
        HttpEntity<?> req = getHttpEntity("hijkllm");
        ResponseEntity<String> res = 
        		template.exchange("/organization/shops", GET, req, String.class);
        
        assertEquals(200, res.getStatusCodeValue());
        
        List<ShopRepresentationObject> services = objectMapper.readValue(res.getBody(), new TypeReference<List<ShopRepresentationObject>>() {});
        assertEquals(2, services.size());
        Set<Long> fetchedIds = 
        		services
        		.stream()
        		.map(ShopRepresentationObject::getId)
        		.collect(toSet());
        assertTrue(fetchedIds.containsAll(asList(100001L, 100003L)));
    }
    
    
    
    
    @Test
    public void getOrganizationShopsNoAuthZ() {
        HttpEntity<?> req = getHttpEntity("abcdefg");
        ResponseEntity<String> res = 
        		template.exchange("/organization/shops", GET, req, String.class);
        assertEquals(403, res.getStatusCodeValue());
    }


    
    @Test
    public void getOrganizationShopsNoAuthN() {
        HttpEntity<?> req = getHttpEntity("NotExist");
        ResponseEntity<String> res = 
        		template.exchange("/organization/shops", GET, req, String.class);
        assertEquals(401, res.getStatusCodeValue());
    }



    @Test
    public void updateSubAreasNoAuthZ(){
        HttpEntity<?> req = getHttpEntity("NotExist");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(401, res.getStatusCodeValue());
    }



    @Test
    public void updateSubAreasNoAuthN(){
        HttpEntity<?> req = getHttpEntity("abcdefg");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(403, res.getStatusCodeValue());
    }



    @Test
    public void updateSubAreasInvalidArea(){
        String name = "werwerland";
        Long areaId = null;
        JSONObject requestBody = createSubAreaUpdateRequest(name, areaId);
        HttpEntity<?> req = getHttpEntity( requestBody.toString(), "hijkllm");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }



    @Test
    public void updateSubAreasMissingArea(){
        String name = "werwerland";
        Long areaId = -1L;
        JSONObject requestBody = createSubAreaUpdateRequest(name, areaId);
        HttpEntity<?> req = getHttpEntity( requestBody.toString(), "hijkllm");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }



    @Test
    public void updateSubAreasInvalidName(){
        String name = "";
        Long areaId = 100001L;
        JSONObject requestBody = createSubAreaUpdateRequest(name, areaId);
        HttpEntity<?> req = getHttpEntity( requestBody.toString(), "hijkllm");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }



    @Test
    public void updateSubAreasMissingName(){
        String name = null;
        Long areaId = 100001L;
        JSONObject requestBody = createSubAreaUpdateRequest(name, areaId);
        HttpEntity<?> req = getHttpEntity( requestBody.toString(), "hijkllm");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }





    @Test
    public void addSubAreasSuccess(){

        assertOldSubAreaExists();

        String name = "Fofo compound";
        Long areaId = 100001L;
        JSONObject requestBody = createSubAreaUpdateRequest(name, areaId);
        HttpEntity<?> req = getHttpEntity( requestBody.toString(), "hijkllm");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(200, res.getStatusCodeValue());
        //---------------------------------------
        assertNewSubAreaInserted(name, req);
        assertOldSubAreaExists();
    }



    private void assertNewSubAreaInserted(String name, HttpEntity<?> req) {
        ResponseEntity<String> countriesResponse =
                template.exchange("/navbox/countries?org_id=99001", GET, req, String.class);
        String subAreaSavedName =
                JsonPath.read(countriesResponse.getBody(), format("$['Egypt']['cities']['Cairo']['areas']['new cairo']['sub_areas']['%s']['name']", name));
        assertEquals(name, subAreaSavedName);
    }


    @Test
    public void addSubAreasWhileKeepingOldOnesSuccess(){

        assertOldSubAreaExists();

        String name = "Fofo compound";
        Long areaId = 100001L;
        JSONObject requestBody = createSubAreaUpdateRequest(name, areaId);
        addOldSubAreaToRequest(requestBody);

        HttpEntity<?> req = getHttpEntity( requestBody.toString(), "hijkllm");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(200, res.getStatusCodeValue());
        //---------------------------------------
        assertNewSubAreaInserted(name, req);

        AddressesEntity addressAfter = addressRepo.findById(12300003L).get();
        assertNotNull("old sub-areas will NOT be cleared from addresses", addressAfter.getSubAreasEntity());

        assertTrue("test old sub-areas are not deleted ", subAreaRepo.findById(888001L).isPresent());
        assertEquals("test old sub-areas are not deleted ", 3, subAreaRepo.findByOrganization_Id(99001L).size());

    }



    private void addOldSubAreaToRequest(JSONObject requestBody) {
        requestBody
            .getJSONArray("sub_areas")
            .put(json()
                    .put("id", nullableJsonValue(888001L))
                    .put("name", nullableJsonValue("Badr city"))
                    .put("area_id", nullableJsonValue(100001)));
    }



    private void assertOldSubAreaExists() {
        AddressesEntity addressBefore = addressRepo.findById(12300003L).get();
        assertNotNull(addressBefore.getSubAreasEntity());
        assertTrue(subAreaRepo.findById(888001L).isPresent());
    }




    @Test
    public void updateSubAreasSuccess(){
        Long id = 888001L;
        String name = "werwerland";
        Long areaId = 100001L;
        JSONObject requestBody = createSubAreaUpdateRequest(id, name, areaId);

        HttpEntity<?> req = getHttpEntity( requestBody.toString(), "hijkllm");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas", POST, req, String.class);
        assertEquals(200, res.getStatusCodeValue());

        SubAreasEntity savedSubArea = subAreaRepo.findByIdAndOrganization_Id(id, 99001L).get();
        assertEquals(name, savedSubArea.getName());
    }


    @Test
    public void deleteSubAreasSuccess(){
        Long subAreaId = 888001L;
        assertTrue(subAreaRepo.existsById(subAreaId));
        HttpEntity<?> req = getHttpEntity("hijkllm");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas?sub_areas="+ subAreaId, DELETE, req, String.class);
        assertEquals(200, res.getStatusCodeValue());
        assertFalse(subAreaRepo.existsById(subAreaId));
    }


    @Test
    public void deleteSubAreasNonExistingId(){
        long subAreaId = 988001;
        HttpEntity<?> req = getHttpEntity("hijkllm");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas?sub_areas="+ subAreaId, DELETE, req, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }


    @Test
    public void deleteSubAreasInvalidToken(){
        long subAreaId = 888001;
        HttpEntity<?> req = getHttpEntity("invalid");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas?sub_areas="+ subAreaId, DELETE, req, String.class);
        assertEquals(401, res.getStatusCodeValue());
    }


    @Test
    public void deleteSubAreasInvalidAuthZ(){
        long subAreaId = 888001;
        HttpEntity<?> req = getHttpEntity("abcdefg");
        ResponseEntity<String> res =
                template.exchange("/organization/sub_areas?sub_areas="+ subAreaId, DELETE, req, String.class);
        assertEquals(403, res.getStatusCodeValue());
    }


    @Test
    public void getOrgSubAreasWithFilters() throws IOException {
        HttpEntity<?> req = getHttpEntity("hijkllm");

        // get all sub_areas with no filters
        ResponseEntity<String> res = template.exchange("/organization/sub_areas", GET, req, String.class);
        assertEquals(200, res.getStatusCodeValue());
        List<SubAreasRepObj> subareasList = parseGetSubareasResponse(res.getBody());
        assertEquals(2, subareasList.size());

        // filter by area_id
        res = template.exchange("/organization/sub_areas?area_id=100001", GET, req, String.class);
        assertEquals(200, res.getStatusCodeValue());
        subareasList = parseGetSubareasResponse(res.getBody());
        assertEquals(1, subareasList.size());

        // filter by city_id
        res = template.exchange("/organization/sub_areas?city_id=100001", GET, req, String.class);
        assertEquals(200, res.getStatusCodeValue());
        subareasList = parseGetSubareasResponse(res.getBody());
        assertEquals(1, subareasList.size());

        // filter by city_id
        res = template.exchange("/organization/sub_areas?country_id=1", GET, req, String.class);
        assertEquals(200, res.getStatusCodeValue());
        subareasList = parseGetSubareasResponse(res.getBody());
        assertEquals(1, subareasList.size());
    }


    @Test
    public void getSubAreasInvalidToken(){
        HttpEntity<?> req = getHttpEntity("invalid");

        ResponseEntity<String> res = template.exchange("/organization/sub_areas", GET, req, String.class);
        assertEquals(401, res.getStatusCodeValue());
    }


    @Test
    public void getSubAreasInvalidAuthZ(){
        HttpEntity<?> req = getHttpEntity("abcdefg");

        ResponseEntity<String> res = template.exchange("/organization/sub_areas", GET, req, String.class);
        assertEquals(403, res.getStatusCodeValue());
    }



    @Test
    public void getExtraAttributesInvalidAuthN(){
        HttpEntity<?> req = getHttpEntity("abcdefg");

        ResponseEntity<String> res = template.exchange(NASNAV_EXTRA_ATTRIBUTES_API_PATH, GET, req, String.class);
        assertEquals(403, res.getStatusCodeValue());
    }



    @Test
    public void getExtraAttributesInvalidAuthZ(){
        HttpEntity<?> req = getHttpEntity("invalid");

        ResponseEntity<String> res = template.exchange(NASNAV_EXTRA_ATTRIBUTES_API_PATH, GET, req, String.class);
        assertEquals(401, res.getStatusCodeValue());
    }



    @Test
    public void getExtraAttributes() throws IOException {
        HttpEntity<?> req = getHttpEntity("hijkllm");

        ResponseEntity<String> res = template.exchange(NASNAV_EXTRA_ATTRIBUTES_API_PATH, GET, req, String.class);

        assertEquals(200, res.getStatusCodeValue());

        List<ExtraAttributeDefinitionDTO> body = objectMapper.readValue(res.getBody(), new TypeReference<List<ExtraAttributeDefinitionDTO>>(){});

        assertEquals(2, body.size());

        ExtraAttributeDefinitionDTO invisibleAttr =
                body.stream().filter(ExtraAttributeDefinitionDTO::getInvisible).findFirst().get();
        assertEquals(INVISIBLE , invisibleAttr.getType());
    }

    @Test
    public void createExtraAttributesTest() throws Exception {
        ExtraAttributeDTO extraAttributeDTO = getExtraAttributesDTO();
        String json = objectMapper.writeValueAsString(extraAttributeDTO);
        String authToken = "123456";

        HttpEntity req = getHttpEntity(json, authToken);

        ResponseEntity<Integer> res = template.postForEntity(NASNAV_EXTRA_ATTRIBUTES_API_PATH + "?operation=create", req, Integer.class);

        ExtraAttributesEntity extraAttributes = extraAttrRepo.findById(res.getBody()).get();

        assertEquals(OK, res.getStatusCode());
        assertNotNull(extraAttributes);
        assertExtraAttributesDTOMatchesEntity(extraAttributeDTO, extraAttributes);
    }

    @Test
    public void createExtraAttributesTestWithNulls() throws Exception {
        String authToken = "123456";
        String nameNullJson = json()
                .put("icon", "icon")
                .put("type", "STRING")
                .toString();

        HttpEntity req = getHttpEntity(nameNullJson, authToken);
        ResponseEntity<Object> res_1 = template.postForEntity(NASNAV_EXTRA_ATTRIBUTES_API_PATH + "?operation=create", req, Object.class);
        assertEquals(NOT_ACCEPTABLE, res_1.getStatusCode());

        String jsonNullType = json()
                .put("name", "extra_attr_name")
                .put("icon", "icon")
                .toString();


        req = getHttpEntity(jsonNullType, authToken);
        ResponseEntity<Integer> res_2 = template.postForEntity(NASNAV_EXTRA_ATTRIBUTES_API_PATH + "?operation=create", req, Integer.class);
        ExtraAttributesEntity extraAttributes = extraAttrRepo.findById(res_2.getBody()).get();

        assertEquals(OK, res_2.getStatusCode());
        assertEquals("String", extraAttributes.getType());
    }

    private void assertExtraAttributesDTOMatchesEntity(ExtraAttributeDTO extraAttributeDTO, ExtraAttributesEntity extraAttributesEntity){
        assertEquals(extraAttributeDTO.getName(), extraAttributesEntity.getName());
        assertEquals(extraAttributeDTO.getIconUrl(), extraAttributesEntity.getIconUrl());
        assertEquals(extraAttributeDTO.getType().getValue(), extraAttributesEntity.getType());
    }

    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/ExtraAttributes_Test_Data_Insert_2.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    @Test
    public void updateExtraAttributesTest() throws Exception {
        ExtraAttributeDTO extraAttributeDTO = new ExtraAttributeDTO();
        Integer existingExtraAttrId = 11002;

        extraAttributeDTO.setId(existingExtraAttrId);
        extraAttributeDTO.setType(INVISIBLE);
        extraAttributeDTO.setIconUrl("updated_url");
        extraAttributeDTO.setName("updated_name");

        String json = objectMapper.writeValueAsString(extraAttributeDTO);
        HttpEntity req = getHttpEntity(json, "123456");

        ResponseEntity<Integer> res = template.postForEntity(NASNAV_EXTRA_ATTRIBUTES_API_PATH + "?operation=update", req, Integer.class);

        ExtraAttributesEntity extraAttributes = extraAttrRepo.findById(existingExtraAttrId).get();

        assertEquals(OK, res.getStatusCode());
        assertEquals(INVISIBLE.getValue(), extraAttributes.getType());
        assertEquals("updated_url", extraAttributes.getIconUrl());
        assertEquals("updated_name", extraAttributes.getName());
    }

    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/ExtraAttributes_Test_Data_Insert_2.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    @Test
    public void updateExtraAttributesWrongIdTest() throws Exception {
        ExtraAttributeDTO extraAttributeDTO = new ExtraAttributeDTO();
        Integer existingExtraAttrId = 11003;
        extraAttributeDTO.setId(existingExtraAttrId);
        extraAttributeDTO.setType(INVISIBLE);

        String json = objectMapper.writeValueAsString(extraAttributeDTO);
        HttpEntity req = getHttpEntity(json, "123456");

        ResponseEntity<Object> res = template.postForEntity(NASNAV_EXTRA_ATTRIBUTES_API_PATH + "?operation=update", req, Object.class);

        assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
    }

    private ExtraAttributeDTO getExtraAttributesDTO(){
        ExtraAttributeDTO extraAttributeDTO = new ExtraAttributeDTO();

        extraAttributeDTO.setName("extra_attr_name");
        extraAttributeDTO.setType(STRING);
        extraAttributeDTO.setIconUrl("extra_attr_icon");

        return extraAttributeDTO;
    }

    private List<SubAreasRepObj> parseGetSubareasResponse(String res) throws IOException {
        return objectMapper.readValue(res, new TypeReference<List<SubAreasRepObj>>(){});
    }

    private JSONObject createSubAreaUpdateRequest(String name, Long areaId) {
        return createSubAreaUpdateRequest(null, name, areaId);
    }

    private JSONObject createSubAreaUpdateRequest(Long id, String name, Long areaId) {
        return json()
                .put("sub_areas",
                        jsonArray()
                                .put(json()
                                        .put("id", nullableJsonValue(id))
                                        .put("name", nullableJsonValue(name))
                                        .put("area_id", nullableJsonValue(areaId))
                                )
                );
    }



}