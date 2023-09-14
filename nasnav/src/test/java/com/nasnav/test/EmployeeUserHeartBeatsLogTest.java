package com.nasnav.test;


import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.stream.IntStream;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/EmpUsers_Test_Data_Insert.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
class EmployeeUserHeartBeatsLogTest extends AbstractTestWithTempBaseDir {
    @Autowired
    private TestRestTemplate template;

    @Test
    void testFindALlActiveEmployee() {
        String[] tokens = {"abcdefg", "hijkllm", "123", "456", "131415"};
        IntStream.range(0, 10).forEach(it -> {
            for (String token : tokens) {
                log(token);
            }
        });
        ResponseEntity<List<UserRepresentationObject>> response = getAllActiveEmployee(99001);
        assertThat(response.getStatusCode().value(), is(200));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody().size(), is(5));

    }

    private ResponseEntity<Void> log(String token) {
        return template.exchange("/employee-user-heart-beats-logs/log", HttpMethod.POST, getHttpEntity(token), Void.class);
    }

    private ResponseEntity<List<UserRepresentationObject>> getAllActiveEmployee(Integer orgId) {
        ParameterizedTypeReference<List<UserRepresentationObject>> responseType = new ParameterizedTypeReference<>() {
        };
        HttpEntity httpEntity = new HttpEntity<>(null);
        return template.exchange("/employee-user-heart-beats-logs/list-active-employee?org_id=" + orgId, HttpMethod.GET, httpEntity, responseType);
    }
}
