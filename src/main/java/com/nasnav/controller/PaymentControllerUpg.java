package com.nasnav.controller;

import com.nasnav.AppConfig;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.OrganizationPaymentGatewaysRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.upg.UpgLightbox;
import com.nasnav.payments.misc.Tools;
import com.nasnav.payments.upg.UpgSession;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.service.OrderService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
    private OrderService orderService;

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
    public ResponseEntity<?> testMezza(@RequestParam(name = "order_id")  Long metaOrderId) throws BusinessException {
        ArrayList<OrdersEntity> orders = Tools.getOrdersForMetaOrder(ordersRepository, metaOrderId);

        String accountName = Tools.getAccount(orders, "upg", orgPaymentGatewaysRep);

        Properties props = Tools.getPropertyForAccount(accountName, upgLogger, config.paymentPropertiesDir);
        if (props == null) {
            throw new BusinessException("Unknown payment account","",HttpStatus.NOT_ACCEPTABLE);
        }
        session.getUpgAccount().init(props);

        UpgLightbox lightbox = new UpgLightbox();
        JSONObject data = lightbox.getJsonConfig(metaOrderId, session.getUpgAccount(), ordersRepository, session.getOrderService(), upgLogger);
        String testPage = lightbox.getConfiguredHtml(data,"static/upg-lightbox.html", "/payment/upg/callback");

//        String initResult = initPayment(orderId).getBody().toString();
        return new ResponseEntity<>(testPage, HttpStatus.OK);
    }

    @RequestMapping(value = "initialize")
    public ResponseEntity<?> upgGetData(@RequestParam(name = "order_id") Long metaOrderId) throws BusinessException {
        ArrayList<OrdersEntity> orders = Tools.getOrdersForMetaOrder(ordersRepository, metaOrderId);

        validateOrdersForCheckOut(orders);
        
        String accountName = Tools.getAccount(orders, "upg", orgPaymentGatewaysRep);

        Properties props = Tools.getPropertyForAccount(accountName, upgLogger, config.paymentPropertiesDir);
        if (props == null) {
            throw new BusinessException("Unknown payment account","",HttpStatus.NOT_ACCEPTABLE);
        }
        session.getUpgAccount().init(props);
        upgLogger.info("Setting up payment for meta order: {} via processor: {}", metaOrderId, session.getUpgAccount().getUpgMerchantId());

        UpgLightbox lightbox = new UpgLightbox();
        JSONObject data = lightbox.getJsonConfig(metaOrderId, session.getUpgAccount(), ordersRepository, session.getOrderService(), upgLogger);
        return new ResponseEntity<>(data.toString(), HttpStatus.OK);
    }
    
    
    private void validateOrdersForCheckOut(List<OrdersEntity> orders) {
		List<Long> orderIds = 
				orders
				.stream()
				.map(OrdersEntity::getId)
				.collect(toList());        
		orderService.validateOrderIdsForCheckOut(orderIds);
	}

    @PostMapping(value = "callback")
    public ResponseEntity<?> upgCallback(@RequestBody String content) throws BusinessException {
        upgLogger.info("Received payment confirmation: {}", content);
        UpgLightbox lightbox = new UpgLightbox();
        return lightbox.callback(content, ordersRepository, paymentsRepository, session.getUpgAccount(), session.getOrderService(), upgLogger);
    }

 }
