package com.nasnav.test;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.nasnav.NavBox;
import com.nasnav.controller.PaymentControllerMastercard;
import com.nasnav.controller.PaymentControllerPayMob;
import com.nasnav.dao.*;
import com.nasnav.payments.misc.Commons;
import com.nasnav.payments.paymob.PaymobService;
import com.nasnav.payments.paymob.PaymobSource;
import com.nasnav.payments.paymob.TokenResponse;
import com.nasnav.payments.upg.UpgLightbox;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.PaymentEntity;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.LinkedList;
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
        TokenResponse result = service.init(metaOrder.get(), source);

        assertNotNull(result);
        assertNotNull(result.getToken());


        service.verifyAndStore(result.getToken());
        PaymentEntity payment = paymentCommons.getPaymentForOrderUid(result.getToken());

        assertNotNull(payment);
        assertEquals(payment.getStatus(), PAID);

    }


}
