package com.nasnav.controller;

import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.OrderRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.OrderSessionResponse;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.qnb.Account;
import com.nasnav.payments.qnb.ActiveSessions;
import com.nasnav.payments.qnb.PaymentService;
import com.nasnav.payments.qnb.Session;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.OrdersEntity;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

import static com.nasnav.enumerations.TransactionCurrency.EGP;


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
        TransactionCurrency currency = EGP;

        if (session.initialize(orderId, currency)) {
            return new ResponseEntity<>(paymentService.getConfiguredHtml(orderId, session), HttpStatus.OK);
        };

        throw new BusinessException("Unable to initialize QNB payment session",null,HttpStatus.BAD_GATEWAY);
    }
    @PostMapping(value = "/initialize")
    public ResponseEntity<?> initPayment(@RequestParam(name = "order_id") Long orderId) throws BusinessException {
        OrdersEntity order = orderRepository.findById(orderId).get();
        if(order == null){
            throw new BusinessException("No Order Exists with that id", "406",HttpStatus.BAD_REQUEST);
        }
        Account account = new Account();
        session.setMerchantAccount(account);
        // TODO: mockup, later retrieve from order
        long orderPriceInCents = 35000;
        TransactionCurrency currency = EGP;

        if (session.initialize(orderId, currency)) {
            OrderSessionResponse response = createOrderResponseJson(order);
            System.out.println("-------response-----------" + response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        throw new BusinessException("Unable to initialize QNB payment session",null,HttpStatus.BAD_GATEWAY);
    }

    private OrderSessionResponse createOrderResponseJson(OrdersEntity order) {
        OrderSessionResponse response = new OrderSessionResponse();
        try {
            response.setSuccess(true);
            OrderSessionResponse.ResponseSession responseSession = new OrderSessionResponse.ResponseSession();
            responseSession.setId(session.getSessionId());
            response.setSession(responseSession);
            response.setMerchant_id(session.getMerchantId());
            response.setOrder_ref(session.getOrderRef());
            response.setBasket(session.getBasketFromOrderId(order));

            OrderSessionResponse.Customer customer = new OrderSessionResponse.Customer();
            customer.setName(order.getName());
            customer.setEmail(order.getEmail());
            response.setCustomer(customer);

            OrderSessionResponse.Seller seller = new OrderSessionResponse.Seller();
            seller.setName(order.getOrganizationEntity().getName());
            response.setSeller(seller);

            List<BasketsEntity> baskets = basketRepository.findByOrdersEntity_Id(order.getId());
            baskets.stream().forEach( basket -> basket.getStocksEntity());

            //TODO : add currency to stocks and retrieve it from there
            response.setOrder_currency(EGP);
            response.setOrder_value(order.getAmount());
            return response;
        }catch(Exception ex){
            response.setSuccess(false);
            ex.printStackTrace();
            return response;
        }
    }
}
