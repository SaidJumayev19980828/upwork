package com.nasnav.test.controller;

import com.nasnav.dto.request.ServiceDTO;
import com.nasnav.dto.response.ServiceResponse;
import com.nasnav.service.ServiceInterface;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestTemplate;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;

@NotThreadSafe
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Package_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class ServiceControllerTest extends AbstractTestWithTempBaseDir {

    private RestTemplate restTemplate;
    private String url;
    @LocalServerPort
    private int serverPort;

    @MockBean
    private ServiceInterface serviceInterface;

    @BeforeEach
    public void init() {

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        restTemplate = new RestTemplate(factory);

        restTemplate.setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("User-Token", "abcdefg");
                    return execution.execute(request, body);
                }));
        url = "http://localhost:" + serverPort + "/service";

        lenient().when(serviceInterface.createService(any(ServiceDTO.class))).thenReturn(new ServiceResponse());
        lenient().when(serviceInterface.updateService(anyLong(), any(ServiceDTO.class))).thenReturn(new ServiceResponse());
        lenient().doNothing().when(serviceInterface).deleteService(anyLong());
        lenient().when(serviceInterface.getService(anyLong())).thenReturn(new ServiceResponse());
        lenient().when(serviceInterface.getALlServices()).thenReturn(new ArrayList<>());

    }

    public ServiceDTO serviceDTO() {
        ServiceDTO service = new ServiceDTO();
        service.setCode("testCode");
        service.setDescription("desc");
        service.setName("test");
        service.setEnabled(true);
        service.setLightLogo("light");
        service.setDarkLogo("dark");
        return service;
    }

    @Test
    void createServiceTest() {
        ServiceDTO serviceDTO = serviceDTO();
        restTemplate.setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("User-Token", "abcdefg");
                    return execution.execute(request, body);
                }));

        assertEquals(HttpStatus.OK,
                (restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(serviceDTO), ServiceResponse.class))
                        .getStatusCode());
    }

    @Test
    void updateServiceTest() {
        ServiceDTO serviceDTO = serviceDTO();
        restTemplate.setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("User-Token", "abcdefg");
                    return execution.execute(request, body);
                }));

        assertEquals(HttpStatus.OK,
                (restTemplate.exchange(url + "/1", HttpMethod.PUT, new HttpEntity<>(serviceDTO), ServiceResponse.class))
                        .getStatusCode());
    }

    @Test
    void deleteServiceTest() {
        ServiceDTO serviceDTO = serviceDTO();
        restTemplate.setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("User-Token", "abcdefg");
                    return execution.execute(request, body);
                }));

        assertEquals(HttpStatus.OK,
                (restTemplate.exchange(url + "/1", HttpMethod.DELETE, null, ServiceResponse.class))
                        .getStatusCode());
    }

    @Test
    void getServiceTest() {
        ServiceDTO serviceDTO = serviceDTO();
        restTemplate.setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("User-Token", "abcdefg");
                    return execution.execute(request, body);
                }));

        assertEquals(HttpStatus.OK,
                (restTemplate.exchange(url + "/1", HttpMethod.GET, null, ServiceResponse.class))
                        .getStatusCode());
    }

    @Test
    void getAllServiceTest() {
        ServiceDTO serviceDTO = serviceDTO();
        restTemplate.setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("User-Token", "abcdefg");
                    return execution.execute(request, body);
                }));

        assertEquals(HttpStatus.OK,
                (restTemplate.exchange(url, HttpMethod.GET, null, ArrayList.class))
                        .getStatusCode());
    }

}
