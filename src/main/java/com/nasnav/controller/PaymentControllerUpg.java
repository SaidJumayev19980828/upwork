package com.nasnav.controller;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.AppConfig;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.OrganizationPaymentGatewaysRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.misc.Tools;
import com.nasnav.payments.upg.UpgLightbox;
import com.nasnav.payments.upg.UpgSession;
import com.nasnav.persistence.OrdersEntity;

import springfox.documentation.annotations.ApiIgnore;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/payment/upg")
public class PaymentControllerUpg {

    private static final Logger upgLogger = LogManager.getLogger("Payment:UPG");

    private final OrdersRepository ordersRepository;

    private final PaymentsRepository paymentsRepository;

    private final UpgSession session;

    @Autowired
    private AppConfig config;

    @Autowired
    private OrganizationPaymentGatewaysRepository orgPaymentGatewaysRep;
    
    @Autowired
    public PaymentControllerUpg(
            OrdersRepository ordersRepository,
            PaymentsRepository paymentsRepository,
            UpgSession session) {
        this.ordersRepository = ordersRepository;
        this.paymentsRepository = paymentsRepository;
        this.session = session;
    }

    @ApiIgnore
    @GetMapping(value = "test/lightbox",produces=MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> testMezza(@RequestParam(name = "order_id") String ordersList) throws BusinessException {
        ArrayList<OrdersEntity> orders = Tools.getOrdersFromString(ordersRepository, ordersList, ",");

        String accountName = Tools.getAccount(Tools.getOrdersFromString(ordersRepository, ordersList, ","), "upg", orgPaymentGatewaysRep);

        Properties props = Tools.getPropertyForAccount(accountName, upgLogger, config.paymentPropertiesDir);
        if (props == null) {
            throw new BusinessException("Unknown payment account","",HttpStatus.NOT_ACCEPTABLE);
        }
        session.getUpgAccount().init(props);

        UpgLightbox lightbox = new UpgLightbox();
        JSONObject data = lightbox.getJsonConfig(orders, session.getUpgAccount(), session.getOrderService(), upgLogger);
        String testPage = lightbox.getConfiguredHtml(data,"static/upg-lightbox.html", "/payment/upg/callback");

//        String initResult = initPayment(orderId).getBody().toString();
        return new ResponseEntity<>(testPage, HttpStatus.OK);
    }

    @RequestMapping(value = "initialize")
    public ResponseEntity<?> upgGetData(@RequestParam(name = "order_id") String ordersList) throws BusinessException {
        ArrayList<OrdersEntity> orders = Tools.getOrdersFromString(ordersRepository, ordersList, ",");

        String accountName = Tools.getAccount(Tools.getOrdersFromString(ordersRepository, ordersList, ","), "upg", orgPaymentGatewaysRep);

        Properties props = Tools.getPropertyForAccount(accountName, upgLogger, config.paymentPropertiesDir);
        if (props == null) {
            throw new BusinessException("Unknown payment account","",HttpStatus.NOT_ACCEPTABLE);
        }
        session.getUpgAccount().init(props);
        upgLogger.info("Setting up payment for order(s): {} via processor: {}", ordersList, session.getUpgAccount().getUpgMerchantId());

        UpgLightbox lightbox = new UpgLightbox();
        JSONObject data = lightbox.getJsonConfig(orders, session.getUpgAccount(), session.getOrderService(), upgLogger);
        return new ResponseEntity<>(data.toString(), HttpStatus.OK);
    }
    

    @PostMapping(value = "callback")
    public ResponseEntity<?> upgCallback(@RequestBody String content) throws BusinessException {
        upgLogger.info("Received payment confirmation: {}", content);
        UpgLightbox lightbox = new UpgLightbox();
        return lightbox.callback(content, ordersRepository, paymentsRepository, session.getUpgAccount(), session.getOrderService(), upgLogger);
    }

 }
