package com.nasnav.test;

import com.nasnav.dao.BankAccountRepository;
import com.nasnav.dao.BankReservationRepository;
import com.nasnav.persistence.BankAccountEntity;
import com.nasnav.persistence.BankReservationEntity;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import lombok.AllArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;


import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/BankingTestData.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class BankingTest extends AbstractTestWithTempBaseDir {
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private BankReservationRepository bankReservationRepository;

    @Test
    public void createAccount(){
        String requestBody =
                json()
                        .put("wallerAddress", "wallerAddress121")
                        .toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "789");
        ResponseEntity<Void> response = template.postForEntity("/bank/account", json, Void.class);
        assertEquals(200, response.getStatusCode().value());

    }

    @Test
    public void lockOrUnlockAccount(){
        String requestBody =
                json().toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "abcdefg");
        template.put("/bank/account?accountId=10&isLocked=true", json, String.class);
        BankAccountEntity entity = bankAccountRepository.findById(10L).get();
        assertEquals(true, entity.getLocked());
    }

    @Test
    public void outsideTransaction(){
        String requestBody =
                json().toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "123");
        ResponseEntity<Void> response = template.postForEntity("/bank/transaction/out?amount=3&isDeposit=true&blockChainKey=0x4a84a5b33bc537266ddce8431683a8cc8c2a8662034c57885be73c972d2e4638", json, Void.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void insideTransaction(){
        String requestBody =
                json().toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "123");
        ResponseEntity<Void> response = template.postForEntity("/bank/transaction/in?amount=3&receiverAccountId=11", json, Void.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void fulfilReservation(){
        String requestBody =
                json().toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "123");
        template.put("/bank/account/reservation/fulfill?reservationId=30", json, String.class);
        BankReservationEntity entity = bankReservationRepository.findById(30L).get();
        assertEquals(true, entity.getFulfilled());
    }
}
