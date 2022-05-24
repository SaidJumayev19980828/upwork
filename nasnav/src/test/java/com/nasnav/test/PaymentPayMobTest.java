package com.nasnav.test;

import com.nasnav.NavBox;
import com.nasnav.dao.*;
import com.nasnav.payments.misc.Commons;
import com.nasnav.payments.paymob.PaymobPaymentResponse;
import com.nasnav.payments.paymob.PaymobService;
import com.nasnav.payments.paymob.PaymobSource;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.PaymentEntity;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.nasnav.enumerations.PaymentStatus.PAID;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Transactional
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Order_Info_Test.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class PaymentPayMobTest {

    @Autowired
    private PaymobService service;

    @Autowired
    private MetaOrderRepository ordersRepository;

    @Autowired
    private Commons paymentCommons;

    @Ignore
    @Test
    @Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD , scripts = {"/sql/Payment_Test_Data_Insert.sql"})
    @Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void testPayMob() throws Exception {
        PaymobSource source = new PaymobSource("01010101010", "WALLET");
        Optional<MetaOrderEntity> metaOrder = ordersRepository.findByMetaOrderId(1L);
        String result = service.payMobCardInit(metaOrder.get());

        assertNotNull(result);
    }


}
