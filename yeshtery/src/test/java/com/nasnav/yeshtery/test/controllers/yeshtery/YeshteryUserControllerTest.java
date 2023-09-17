package com.nasnav.yeshtery.test.controllers.yeshtery;

import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.*;

import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.enumerations.Gender;
import com.nasnav.response.UserApiResponse;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.stream.Stream;

@NotThreadSafe
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Update_User_Info.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class YeshteryUserControllerTest extends AbstractTestWithTempBaseDir {
    private static final String ADMIN_TOKEN = "nasnav-admin-token";
    private static final String CUSTOMER_TOKEN = "nasnav-customer-token";
    private static final String EMPLOYEE_TOKEN = "nasnav-employee-token";
    private static final String YESHTERY_USER_INFO_URL = "/v1/user/info?id=%s&is_employee=%s";
    private static final String YESHTERY_USER_UPDATE_URL = "/v1/user/update";
    private static final String EMPLOYEE_ID = "163";
    private static final Boolean IS_EMPLOYEE = true;
    @Autowired
    private TestRestTemplate template;


    @ParameterizedTest
    @MethodSource("argumentsProviderForEmployees")
    public void getAdminInfoBeforeUpdateTest(String token, String id, String mail) {
        HttpEntity<Object> request = getHttpEntity(token);
        ResponseEntity<UserRepresentationObject> responseEntity =
                template.exchange(format(YESHTERY_USER_INFO_URL, id, IS_EMPLOYEE)
                        , GET
                        , request
                        , UserRepresentationObject.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        UserRepresentationObject body = responseEntity.getBody();
        assertEquals(body.getGender(), Gender.MALE);
        assertEquals(body.getOrganizationId(), 99001);
        assertEquals(body.getEmail(), mail);
    }

    @Test
    public void getEmployeeAfterUpdateTest() {
        HttpEntity<Object> request = getHttpEntity(EMPLOYEE_TOKEN);
        ResponseEntity<UserRepresentationObject> responseEntity =
                template.exchange(format(YESHTERY_USER_INFO_URL, "164", IS_EMPLOYEE)
                        , GET
                        , request
                        , UserRepresentationObject.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        UserRepresentationObject body = responseEntity.getBody();
        assertEquals(body.getGender(), Gender.MALE);
        assertEquals(body.getOrganizationId(), 99001);
        assertEquals(body.getEmail(), "org.employee@nasnav.com");
    }


    @ParameterizedTest
    @MethodSource("argumentsProvider")
    public void getInfoAfterUpdateTest(String token, String id, boolean is_employee, String gender) {
        JSONObject requestBody = createUpdateInfoBody(id, is_employee, gender);
        HttpEntity<Object> request = getHttpEntity(requestBody.toString(), token);
        ResponseEntity<UserApiResponse> updateUserInfoResponseEntity = template
                .postForEntity(YESHTERY_USER_UPDATE_URL, request, UserApiResponse.class);
        assertEquals(HttpStatus.OK, updateUserInfoResponseEntity.getStatusCode());

        HttpEntity<Object> getInfoRequest = getHttpEntity(ADMIN_TOKEN);
        ResponseEntity<UserRepresentationObject> userInfoResponseEntity =
                template.exchange(format(YESHTERY_USER_INFO_URL, EMPLOYEE_ID, is_employee)
                        , GET
                        , getInfoRequest
                        , UserRepresentationObject.class);

        assertEquals(HttpStatus.OK, userInfoResponseEntity.getStatusCode());
    }

    /*
        Employee with role (ORGANIZATION_EMPLOYEE) can't update neither his data nor another employee's data.
        is that accepted from business point of view !!!
    */
    @Test
    public void updateEmployeeInfoTest() {
        JSONObject requestBody = createUpdateInfoBody("164", true, "FEMALE");
        HttpEntity<Object> request = getHttpEntity(requestBody.toString(), EMPLOYEE_TOKEN);
        ResponseEntity<UserApiResponse> updateUserInfoResponseEntity = template
                .postForEntity(YESHTERY_USER_UPDATE_URL, request, UserApiResponse.class);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, updateUserInfoResponseEntity.getStatusCode());
    }

    private JSONObject createUpdateInfoBody(String id, Boolean is_employee, String gender) {
        JSONObject body = new JSONObject();
        body.put("updated_user_id", id);
        body.put("employee", is_employee);
        body.put("gender", gender);
        body.put("birth_date", "1997-01-25T00:00:00.000");
        return body;
    }

    public static Stream<Arguments> argumentsProvider() {
        return Stream.of(Arguments.of(ADMIN_TOKEN, "163", true, "FEMALE")
                , Arguments.of(CUSTOMER_TOKEN, "808", false, "MALE"));
    }

    public static Stream<Arguments> argumentsProviderForEmployees() {
        return Stream.of(Arguments.of(ADMIN_TOKEN, "163", "nasnav.admin@nasnav.com")
                , Arguments.of(EMPLOYEE_TOKEN, "164", "org.employee@nasnav.com"));
    }

}
