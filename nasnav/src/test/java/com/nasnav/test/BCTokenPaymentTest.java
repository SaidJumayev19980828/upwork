package com.nasnav.test;

import com.nasnav.dto.response.TokenPaymentResponse;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/BC_Payment.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class BCTokenPaymentTest extends AbstractTestWithTempBaseDir {

    public static final String USELESS_NOTE = "come after dinner";
    @Autowired
    private TestRestTemplate template;



    private String buildRequest(){
        JSONObject requestBody =new JSONObject();
        requestBody.put("currency", "EGP");
        requestBody.put("amount", 500);
        return requestBody.toString();
    }

    @Test
    @Ignore
    public void tokenPaymentTest(){
        HttpEntity<?> request = getHttpEntity(buildRequest(), "123");
        ResponseEntity<TokenPaymentResponse> res = template.postForEntity("/cart/token-paymet/102", request, TokenPaymentResponse.class);
        assertEquals(200, res.getStatusCodeValue());
    }

    @Test
    public void tokenPaymentExceptionBrandNotFound(){
        HttpEntity<?> request = getHttpEntity(buildRequest(), "123");
        ResponseEntity<TokenPaymentResponse> res = template.postForEntity("/cart/token-paymet/1", request, TokenPaymentResponse.class);
        assertEquals(404, res.getStatusCodeValue());
    }

    @Test
    public void tokenPaymentExceptionOrganizationACCNotFound(){
        HttpEntity<?> request = getHttpEntity(buildRequest(), "123");
        ResponseEntity<TokenPaymentResponse> res = template.postForEntity("/cart/token-paymet/101", request, TokenPaymentResponse.class);
        assertEquals(404, res.getStatusCodeValue());
    }
    @Test
    public void tokenPaymentExceptionEmployeeNotAllowed(){
        HttpEntity<?> request = getHttpEntity(buildRequest(), "101112");
        ResponseEntity<TokenPaymentResponse> res = template.postForEntity("/cart/token-paymet/102", request, TokenPaymentResponse.class);
        assertEquals(403, res.getStatusCodeValue());
    }

    @Test
    public void tokenPaymentTestExcepion(){
        HttpEntity<?> request = getHttpEntity(buildRequest(), "456");
        ResponseEntity<TokenPaymentResponse> res = template.postForEntity("/cart/token-paymet/102", request, TokenPaymentResponse.class);
        assertEquals(404, res.getStatusCodeValue());

    }

}
