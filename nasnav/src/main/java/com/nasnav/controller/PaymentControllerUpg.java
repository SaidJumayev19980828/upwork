package com.nasnav.controller;

import com.nasnav.AppConfig;
import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.OrganizationPaymentGatewaysRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.misc.Commons;
import com.nasnav.payments.misc.Tools;
import com.nasnav.payments.upg.UpgLightbox;
import com.nasnav.payments.upg.UpgSession;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Properties;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/payment/upg")
public class PaymentControllerUpg {

    private static final Logger upgLogger = LogManager.getLogger("Payment:UPG");

    private final UpgSession session;

    @Autowired
    private AppConfig config;

    @Autowired
    private Commons paymentCommons;

    @Autowired
    private OrganizationPaymentGatewaysRepository orgPaymentGatewaysRep;

    @Autowired
    private MetaOrderRepository ordersRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    public PaymentControllerUpg(
            OrdersRepository ordersRepository,
            PaymentsRepository paymentsRepository,
            UpgSession session) {
        this.session = session;
    }

    @Operation(hidden = true)
    @GetMapping(value = "test/lightbox",produces=MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> testMezza(@RequestParam(name = "order_id")  Long metaOrderId) throws BusinessException {

        JSONObject data = new JSONObject(upgGetData(metaOrderId).getBody().toString());
        String testPage = UpgLightbox.getConfiguredHtml(data,"static/upg-lightbox.html", "/payment/upg/callback");

        return new ResponseEntity<>(testPage, HttpStatus.OK);
    }

    @RequestMapping(value = "initialize")
    public ResponseEntity<?> upgGetData(@RequestParam(name = "order_id") Long metaOrderId) throws BusinessException {
        ArrayList<OrdersEntity> orders = orderService.getOrdersForMetaOrder(metaOrderId);

        String accountName = Tools.getAccount(orders, "upg", orgPaymentGatewaysRep);

        Properties props = Tools.getPropertyForAccount(accountName, upgLogger, config.paymentPropertiesDir);
        if (props == null) {
            throw new BusinessException("Unknown payment account","",HttpStatus.NOT_ACCEPTABLE);
        }
        session.getUpgAccount().init(props);
        upgLogger.info("Setting up payment for meta order: {} via processor: {}", metaOrderId, session.getUpgAccount().getUpgMerchantId());

        UpgLightbox lightbox = new UpgLightbox();
        JSONObject data = lightbox.getJsonConfig(metaOrderId, session.getUpgAccount(), orderService, upgLogger);
        return new ResponseEntity<>(data.toString(), HttpStatus.OK);
    
    }

    @PostMapping(value = "callback")
    public ResponseEntity<?> upgCallback(@RequestBody String content) throws BusinessException {
        upgLogger.info("Received payment confirmation: {}", content);
        UpgLightbox lightbox = new UpgLightbox();
        return lightbox.callback(content, session.getUpgAccount(), orderService, paymentCommons, ordersRepository, upgLogger, false);
    }

 }
