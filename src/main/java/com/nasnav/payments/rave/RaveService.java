package com.nasnav.payments.rave;

import com.nasnav.AppConfig;
import com.nasnav.dao.OrganizationPaymentGatewaysRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.misc.Commons;
import com.nasnav.payments.misc.Gateway;
import com.nasnav.payments.misc.Tools;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationPaymentGatewaysEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.service.OrderService;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class RaveService {
	private Logger classLogger = LogManager.getLogger("Payment:RAVE");

	@Autowired
	private AppConfig config;

	@Autowired
	private Commons paymentCommons;

	@Autowired
	private OrderService orderService;

	@Autowired
	private PaymentsRepository paymentsRepository;

	public RaveService() {	}

	@Autowired
	private OrganizationPaymentGatewaysRepository orgPaymentGatewaysRep;

	public RaveAccount getMerchantAccount(int orgPaymentId) throws BusinessException {
		try {
			OrganizationPaymentGatewaysEntity gateway = orgPaymentGatewaysRep.getOne(orgPaymentId);
			RaveAccount merchantAccount = new RaveAccount(Tools.getPropertyForAccount(gateway.getAccount(), classLogger, config.paymentPropertiesDir), orgPaymentId);
			return merchantAccount;
		} catch (Exception ex) {
			throw new BusinessException("Unable to find payment account data for org_payment_id = " + orgPaymentId, "INVALID_PAYMENT_ID", HttpStatus.NOT_ACCEPTABLE);
		}
	}


	public PaymentEntity initialize(RaveAccount merchantAccount, Long metaOrderId) throws BusinessException {
		ArrayList<OrdersEntity> orders = orderService.getOrdersForMetaOrder(metaOrderId);

		if (Objects.isNull(orders)) {
			throw new BusinessException("No orders provided for payment!", "INVALID PARAM: order_id", NOT_ACCEPTABLE);
		}
		if (orders.size() == 0) {
			classLogger.error("No sub-orders matching meta order ({})", metaOrderId);
			throw new BusinessException("No valid order IDs recognized", "PAYMENT_FAILED", HttpStatus.NOT_ACCEPTABLE);
		}
		OrderService.OrderValue orderValue = orderService.getMetaOrderTotalValue(metaOrderId);
		if (orderValue == null) {
			throw new BusinessException("Order ID is invalid", "PAYMENT_FAILED", HttpStatus.NOT_ACCEPTABLE);
		}

		// TODO: Hardcoded currency
		orderValue.currency = TransactionCurrency.NGN;

		if (orderValue.currency != TransactionCurrency.NGN) {
			classLogger.error("Payment for meta order ({}) failed due to incorrect currency: {}", metaOrderId, orderValue.currency);
			throw new BusinessException("Invalid currency, this gateway only supports NGN", "PAYMENT_FAILED", HttpStatus.NOT_ACCEPTABLE);
		}

		String transactionId = Tools.getOrderUid(metaOrderId, classLogger);

		PaymentEntity payment = new PaymentEntity();
		payment.setStatus(PaymentStatus.STARTED);
		payment.setAmount(orderValue.amount);
		payment.setCurrency(orderValue.currency);
		payment.setUid(transactionId);
		payment.setOperator(merchantAccount.getAccountId());
		payment.setExecuted(new Date());
		payment.setUserId(orders.get(0).getUserId());
		payment.setMetaOrderId(metaOrderId);
//System.out.println("####### " + payment.getOperator() + " : " + account.getAccountId());
		paymentsRepository.saveAndFlush(payment);
		return payment;

	}

	public void verifyAndStore(String flwRef, String orderUid) throws BusinessException {


		PaymentEntity payment = paymentCommons.getPaymentForOrderUid(orderUid);
		if (payment == null) {
			classLogger.warn("No payment associated with order {}", orderUid);
			throw new BusinessException("There is no initiated payment associated with the order", "INVALID_INPUT", HttpStatus.NOT_ACCEPTABLE);
		}
		long orderId = Tools.getOrderIdFromUid(orderUid);
		RaveAccount account = getAccountForOrder(orderId);
		if (account == null) {
			throw new BusinessException("No account associated with UID: " + orderUid, "INVALID_INPUT", HttpStatus.NOT_ACCEPTABLE);
		}
        if (payment.getStatus() != PaymentStatus.STARTED) {
            classLogger.error("Invalid state ({}) for payment {}", payment.getStatus(), payment.getId());
            throw new BusinessException("Invalid state for the payment ", "INVALID_INPUT", HttpStatus.NOT_ACCEPTABLE);
        }

		// Verify transaction at the source
		JSONObject data = new JSONObject();
//        data.put("txref", this.transactionData.transactionId);
		data.put("flwref", flwRef);
		data.put("SECKEY", account.getPrivateKey());
		JSONObject responseObject = null;
		try {
			HttpClient client= HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(account.getApiUrl() + "/verify");
			StringEntity requestEntity = new StringEntity(data.toString(), ContentType.APPLICATION_JSON);
			request.setEntity(requestEntity);
			//System.out.println(request.getURI());
			HttpResponse response = client.execute(request);
//            int httpStatus = response.getStatusLine().getStatusCode();
			responseObject = new JSONObject(paymentCommons.readInputStream(response.getEntity().getContent()));
//System.out.println("RESPONSE: " + responseObject.toString());
		} catch (IOException e) {
			classLogger.error("Empty response for flwRef ({})", flwRef);
			throw new BusinessException("Unable to retrieve confirmation from the gateway", "PAYMENT_UNRECOGNIZED_RESPONSE", HttpStatus.BAD_GATEWAY);
		}

		if(responseObject == null || responseObject.isEmpty()) {
			classLogger.error("Empty response for flwRef ({})", flwRef);
			throw new BusinessException("\"Unable to retrieve confirmation from the gateway", "PAYMENT_UNRECOGNIZED_RESPONSE", HttpStatus.BAD_GATEWAY);
		}

		String status = responseObject.optString("status", null);
		data = responseObject.getJSONObject("data");
		if(status == null || data == null) {
			classLogger.error("Invalid response for flwRef ({}). JSON = {}", flwRef, responseObject);
			throw new BusinessException("Invalid state for the payment ", "PAYMENT_UNRECOGNIZED_RESPONSE", HttpStatus.BAD_GATEWAY);
		}

		BigDecimal actualAmount = data.getBigDecimal("amount");
		if(orderUid == null || orderUid.isEmpty()) {
			classLogger.error("Payment with flwRef ({}) doesn't have order uid/reference. JSON = {}", flwRef, responseObject);
			throw new BusinessException("Unable to identify order", "PAYMENT_FAILED", HttpStatus.BAD_GATEWAY);
		}

		Optional<PaymentEntity> paymentOpt = paymentsRepository.findByUid(orderUid);
		if (!paymentOpt.isPresent()) {
			classLogger.warn("No payment associated with order {}", orderUid);
			throw new BusinessException("There is no initiated payment associated with the order", "INVALID_INPUT", HttpStatus.NOT_ACCEPTABLE);
		}
		payment.setObject(responseObject.toString());
		payment.setExecuted(new Date());

		if(!"success".equalsIgnoreCase(status)) {
			classLogger.error("Payment failed for order {}", orderUid);
			payment.setStatus(PaymentStatus.FAILED);
			paymentsRepository.saveAndFlush(payment);
			throw new BusinessException("Invalid state for the payment ", "PAYMENT_FAILED", HttpStatus.NOT_ACCEPTABLE);
		}
//System.out.println(" Actual: " +  actualAmount + " payment: " + payment.getAmount());
		if(payment.getAmount() == null || actualAmount == null || actualAmount.compareTo(payment.getAmount()) != 0) {
//                actualAmount.movePointRight(2).intValue() != payment.getAmount().movePointRight(2).intValue()) {
			classLogger.error("Payment amount doesn't match order {}", orderUid);
			payment.setStatus(PaymentStatus.FAILED);
			paymentsRepository.saveAndFlush(payment);
			throw new BusinessException("Invalid payment amount", "PAYMENT_FAILED", HttpStatus.NOT_ACCEPTABLE);
		}

		// Everything seems OK
		payment.setStatus(PaymentStatus.PAID);
		paymentCommons.finalizePayment(payment);
	}


	public RaveAccount getAccountForOrder(long metaOrderId) throws BusinessException {
		RaveAccount merchantAccount = (RaveAccount)paymentCommons.getMerchantAccount(metaOrderId, Gateway.RAVE);
		if (merchantAccount == null) {
			classLogger.warn("Unable to find payment account for meta order {} via gateway: rave", metaOrderId);
			throw new BusinessException("Unable to identify payment gateway","",HttpStatus.INTERNAL_SERVER_ERROR);
		}
		classLogger.info("Setting up payment for meta order: {}", metaOrderId);
		return merchantAccount;
	}


}
