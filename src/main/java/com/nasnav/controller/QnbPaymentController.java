package com.nasnav.controller;

import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.OrderRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.Currency;
import com.nasnav.dto.OrderSessionBasket;
import com.nasnav.dto.OrderSessionResponse;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.qnb.Account;
import com.nasnav.payments.qnb.ActiveSessions;
import com.nasnav.payments.qnb.PaymentService;
import com.nasnav.payments.qnb.Session;
import com.nasnav.persistence.BasketsEntity;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.nasnav.payments.qnb.Session.TransactionCurrency.EGP;

@RestController
@RequestMapping("/payment/qnb")
public class QnbPaymentController {

    @Autowired
    ActiveSessions activeSessions;
    
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private Session session;

    @RequestMapping(value = "/test/payment/init",produces=MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> testPayment(@RequestParam(name = "order_id") Long orderId) throws BusinessException {

        Account account = new Account();
        session.setMerchantAccount(account);
        Session.TransactionCurrency currency = EGP;

        if (session.initialize(orderId, currency)) {
            return new ResponseEntity<>(paymentService.getConfiguredHtml(orderId, session), HttpStatus.OK);
        };

        throw new BusinessException("Unable to initialize QNB payment session",null,HttpStatus.BAD_GATEWAY);
    }
    @PostMapping(value = "/initialize")
    public ResponseEntity<?> initPayment(@RequestParam(name = "order_id") Long orderId) throws BusinessException {

        Account account = new Account();
        session.setMerchantAccount(account);
        // TODO: mockup, later retrieve from order
        long orderPriceInCents = 35000;
        Session.TransactionCurrency currency = EGP;

        if (session.initialize(orderId, currency)) {
            OrderSessionResponse response = createOrderResponseJson(orderId);
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        }

        throw new BusinessException("Unable to initialize QNB payment session",null,HttpStatus.BAD_GATEWAY);
    }

    private OrderSessionResponse createOrderResponseJson(Long order_id) {
        OrderSessionResponse response = new OrderSessionResponse();
        // TODO: just initial few items for now, will need to add remaining params
        response.setSuccess(true);
        OrderSessionResponse.ResponseSession responseSession = new OrderSessionResponse.ResponseSession();
        responseSession.setId(session.getSessionId());
        response.setSession(responseSession);
        response.setMerchant_id(session.getMerchantId());
        response.setOrder_ref(session.getOrderRef());
        response.setBasket(session.getBasketFromOrderId(order_id));
        response.setOrder_currency(Currency.findById(orderRepository.findById(order_id).get().getBasketsEntity().getCurrency()));
        response.setOrder_value(session.getBasketsTotalAmount(order_id));
        return response;
    }
}
