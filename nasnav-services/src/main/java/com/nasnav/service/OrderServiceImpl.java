package com.nasnav.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.request.OrderRejectDTO;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.shipping.ShipmentDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.dto.response.OrderConfirmResponseDTO;
import com.nasnav.dto.response.navbox.*;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.enumerations.ShippingStatus;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.exceptions.StockValidationException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.exceptions.InvalidIntegrationEventException;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.*;
import com.nasnav.request.OrderSearchParam;
import com.nasnav.service.helpers.UserServicesHelper;
import com.nasnav.shipping.model.ShipmentTracker;
import com.nasnav.shipping.model.ShippingServiceInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.commons.utils.EntityUtils.*;
import static com.nasnav.commons.utils.MapBuilder.buildMap;
import static com.nasnav.constatnts.EmailConstants.*;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_NO_ENOUGH_STOCK;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_ORDER_NOT_EXISTS;
import static com.nasnav.enumerations.OrderFailedStatus.INVALID_ORDER;
import static com.nasnav.enumerations.OrderStatus.*;
import static com.nasnav.enumerations.PaymentStatus.*;
import static com.nasnav.enumerations.Roles.*;
import static com.nasnav.enumerations.Settings.STOCK_ALERT_LIMIT;
import static com.nasnav.enumerations.ShippingStatus.DRAFT;
import static com.nasnav.enumerations.ShippingStatus.REQUESTED;
import static com.nasnav.enumerations.TransactionCurrency.EGP;
import static com.nasnav.enumerations.TransactionCurrency.getTransactionCurrency;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static javax.persistence.criteria.JoinType.LEFT;
import static org.springframework.http.HttpStatus.*;
@Service
public class OrderServiceImpl implements OrderService {

	private static final String DEFAULT_REJECTION_MESSAGE =
			"We are very sorry to inform you that we were unable to fulfill your order due to some issues."
			+ " Your order will be refunded shortly, but the refund operation may take several business days "
			+ "depending on the payment method.";

	private static final int ORDER_DEFAULT_COUNT = 1000;

	private static final int ORDER_FULL_DETAILS_LEVEL = 3;

	private final OrdersRepository ordersRepository;

	private final BasketRepository basketRepository;

	private final StockRepository stockRepository;

	private final StockService stockService;

	private final UserServicesHelper userServicesHelper;

	private final Logger logger = LogManager.getLogger();

	@PersistenceContext
	@Autowired
	private EntityManager em;

	@Autowired
	private SecurityService securityService;
	@Autowired
	private LoyaltyPointsService loyaltyPointsService;
	@Autowired
	private ShopsRepository shopsRepo;
	@Autowired
	private AddressRepository addressRepo;
	@Autowired
	private CartItemRepository cartItemRepo;
	@Autowired
	private MetaOrderRepository metaOrderRepo;
	@Autowired
	private ShipmentRepository shipmentRepo;
	@Autowired
	private PaymentsRepository paymentsRepo;
	@Autowired
	private RoleEmployeeUserRepository empRoleRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private ProductVariantsRepository variantsRepo;
	@Autowired
	private PromotionRepository promoRepo;
	@Autowired
	private SettingRepository settingRepo;

	@Autowired
	private IntegrationService integrationService;
	@Autowired
	private ProductService productService;
	@Autowired
	private ShippingManagementService shippingMgrService;
	@Autowired
	private MailService mailService;
	@Autowired
	private DomainService domainService;
	@Autowired
	private PromotionsService promoService;
	@Autowired
	private CartOptimizationService cartOptimizationService;
	@Autowired
	private OrderReturnService orderReturnService;

	@Autowired
	private OrderServiceHelper orderServiceHelper;
	@Autowired
	private OrderEmailServiceHelper orderEmailHelper;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private LoyaltyPointTransactionRepository loyaltyPointTransactionRepository;

	@Autowired
	private LoyaltyPinsRepository loyaltyPinsRepository;

	private Map<OrderStatus, Set<OrderStatus>> orderStateMachine;
	private Set<OrderStatus> orderStatusForCustomers;
	private Set<OrderStatus> orderStatusForManagers;


	@Autowired
	public OrderServiceImpl(OrdersRepository ordersRepository, BasketRepository basketRepository,
							StockRepository stockRepository , StockService stockService, UserRepository userRepository,
							UserServicesHelper userServicesHelper, EmployeeUserRepository employeeUserRepository,
							ProductRepository productRepository) {
		this.ordersRepository = ordersRepository;
		this.stockRepository = stockRepository;
		this.basketRepository = basketRepository;
		this.stockService = stockService;
		this.userServicesHelper = userServicesHelper;
		setOrderStatusPermissions();
		buildOrderStatusTransitionMap();
	}





	private void setOrderStatusPermissions() {
		orderStatusForCustomers = setOf(NEW, CLIENT_CONFIRMED, CLIENT_CANCELLED);
		orderStatusForManagers = getManagerPermittedOrderStatuses();
	}





	private Set<OrderStatus> getManagerPermittedOrderStatuses() {
		Set<OrderStatus> orderStatusForMgr = setOf(OrderStatus.values());
		orderStatusForMgr.removeAll(orderStatusForCustomers);
		return orderStatusForMgr;
	}





	private void buildOrderStatusTransitionMap() {
		orderStateMachine = new HashMap<>();
		buildMap(orderStateMachine)
			.put(CLIENT_CONFIRMED	, setOf(CLIENT_CANCELLED, FINALIZED, DISCARDED))
			.put(DISCARDED			, setOf(DISCARDED))
			.put(CLIENT_CANCELLED	, setOf(CLIENT_CANCELLED))
			.put(FINALIZED	 		, setOf(STORE_CONFIRMED, STORE_CANCELLED, CLIENT_CANCELLED))
			.put(STORE_CONFIRMED	, setOf(STORE_PREPARED, STORE_CANCELLED, DISPATCHED, DELIVERED))
			.put(STORE_PREPARED		, setOf(DISPATCHED, DELIVERED, STORE_CANCELLED))
			.put(DISPATCHED			, setOf(DELIVERED, STORE_CANCELLED));
	}








	@Override
	public void updateExistingOrder(OrderJsonDto orderJson) {
		EmployeeUserEntity empUser = (EmployeeUserEntity)securityService.getCurrentUser();
		List<String> employeeUserRoles = userServicesHelper.getEmployeeUserRoles(empUser.getId());
		OrdersEntity order = getAndValidateOrdersEntityForStatusUpdate(orderJson, empUser, employeeUserRoles);

		updateOrderStatusAndMetaOrderIfNeeded(order, orderJson.getStatus());
	}



	private void updateOrderStatusAndMetaOrderIfNeeded(OrdersEntity order, OrderStatus orderStatus) {
		updateOrderStatus(order, orderStatus);
		MetaOrderEntity metaOrder = order.getMetaOrder();
		if(isAllOtherOrdersHaveStatus(order.getId(), metaOrder, orderStatus)) {
			metaOrder.setStatus(orderStatus.getValue());
			metaOrderRepo.save(metaOrder);
		}
		updateYeshteryMetaOrderIfExists(metaOrder, orderStatus);
	}

	private void updateYeshteryMetaOrderIfExists(MetaOrderEntity metaOrder, OrderStatus orderStatus) {
		Optional<MetaOrderEntity> yeshteryMetaOrderOptional = metaOrderRepo.findBySubMetaOrder_Id(metaOrder.getId());
		if(yeshteryMetaOrderOptional.isPresent()) {
			MetaOrderEntity yeshteryMetaOrder = yeshteryMetaOrderOptional.get();
			if (isAllOtherSubMetaOrdersHaveStatus(metaOrder.getId(), yeshteryMetaOrder.getSubMetaOrders(), orderStatus)) {
				yeshteryMetaOrder.setStatus(orderStatus.getValue());
				metaOrderRepo.save(yeshteryMetaOrder);
			}
		}
	}


	private OrdersEntity getAndValidateOrdersEntityForStatusUpdate(OrderJsonDto orderJson, EmployeeUserEntity empUser, List<String> employeeUserRoles) {
		Optional<OrdersEntity> order = empty();
		Long orderId = orderJson.getId();
		if ( collectionContainsAnyOf(employeeUserRoles, ORGANIZATION_ADMIN.name(), ORGANIZATION_MANAGER.name()) )  {
			Long orgId = empUser.getOrganizationId();
			order = ordersRepository.findByIdAndOrganizationEntity_Id(orderId, orgId);
			if (order.isEmpty()) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CFRM$0004, orgId, orderId);
			}
		} else if ( employeeUserRoles.contains(STORE_MANAGER.name())) {
			Long shopId = empUser.getShopId();
			order = ordersRepository.findByIdAndShopsEntity_Id(orderId, shopId);
			if (order.isEmpty()) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CFRM$0001, shopId, orderId);
			}
		}
		return order
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$0001,  orderId));
	}



	private void throwRuntimeInvalidOrderException(String msg, Object... msgParams){
		throw getInvalidRuntimeOrderException(msg, msgParams);
	}




	private BusinessException getInvalidOrderException(String msg, Object... msgParams) {
		String error = INVALID_ORDER.toString();
		return new BusinessException( format(msg, msgParams), error, NOT_ACCEPTABLE);
	}


	private RuntimeBusinessException getInvalidRuntimeOrderException(String msg, Object... msgParams) {
		String error = INVALID_ORDER.toString();
		return new StockValidationException( format(msg, msgParams), error, NOT_ACCEPTABLE);
	}


	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void finalizeOrder(Long orderId) throws BusinessException {
		//TODO: this should be done if the payment API became authenticated
//		Long userId =
//				ofNullable(securityService.getCurrentUser())
//				.map(BaseUserEntity::getId)
//				.orElseThrow(() -> new RuntimeBusinessException("No user provided for the checkout!", "INVALID OPERATION", NOT_ACCEPTABLE));
//		OrdersEntity order = ordersRepository.findByIdAndUserId(orderId, userId);
		//-------------------------------------------
		MetaOrderEntity order =
				metaOrderRepo
				.findFullDataById(orderId)
				.orElseThrow(() -> getInvalidOrderException(ERR_ORDER_NOT_EXISTS, orderId));

		order.getSubOrders().forEach(this::finalizeSubOrder);
		updateOrderStatus(order, FINALIZED);
		setPromoCodeAsUsed(order);

		try {
			order.getSubOrders().forEach(this::sendNotificationEmailToStoreManager);
		}catch(Throwable t) {
			logger.error(t,t);
		}

		try {
			sendBillEmail(order, false);
		}catch(Throwable t) {
			logger.error(t,t);
		}
	}


	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void finalizeYeshteryMetaOrder(MetaOrderEntity metaOrder, Set<OrdersEntity> subOrders) {

		subOrders.forEach(this::finalizeSubOrder);

		updateOrderStatus(metaOrder, FINALIZED);
		metaOrder.getSubMetaOrders().forEach(subMetaOrder -> updateOrderStatus(subMetaOrder, FINALIZED));

		setPromoCodeAsUsed(metaOrder);

		try {
			subOrders.forEach(this::sendNotificationEmailToStoreManager);
		} catch(Throwable t) {
			logger.error(t,t);
		}

		try {
			sendBillEmail(metaOrder, true);
		}catch(Throwable t) {
			logger.error(t,t);
		}
	}


	private void sendNotificationEmailToStoreManager(OrdersEntity order) {
		Long orderId = order.getId();
		String orgName = order.getOrganizationEntity().getName();
		List<String> to = getStoreManagersEmails(order);
		String subject = format("New Order[%d] Created!", orderId);
		List<String> cc = getOrganizationManagersEmails(order);
		Map<String,Object> parametersMap = createNotificationEmailParams(order);
		String template = ORDER_NOTIFICATION_TEMPLATE;
		try {
			if(to.isEmpty()) {
				to = cc;
				cc = emptyList();
			}
			mailService.sendThymeleafTemplateMail(orgName, to, subject, cc, template, parametersMap);
		} catch (IOException | MessagingException e) {
			logger.error(e, e);
		}
	}





	private Map<String, Object> createNotificationEmailParams(OrdersEntity order) {
		Map<String,Object> params = createOrgPropertiesParams(order.getOrganizationEntity());
		String orderTime =
				DateTimeFormatter
				.ofPattern("dd/MM/YYYY - hh:mm")
				.format(order.getCreationDate());

		SubOrder subOrder = getSubOrder(order);
		changeShippingServiceName(subOrder);

		Optional<PaymentEntity> payment = paymentsRepo.findByMetaOrderId(order.getMetaOrder().getId());
		String operator = "";
		if (payment.isPresent()) {
			operator = payment.get().getOperator();
			operator = changeOperatorName(operator);
		}

		String orderPageUrl =
				domainService
				.buildDashboardOrderPageUrl(order.getId(), order.getOrganizationEntity().getId());
		String notes = order.getMetaOrder().getNotes();

		params.put("creationTime", orderTime);
		params.put("orderPageUrl", orderPageUrl);
		params.put("sub", subOrder);
		params.put("operator", operator);
		params.put("notes", notes);
		return params;
	}




	private Map<String, Object> createRejectionEmailParams(OrdersEntity order, String rejectionReason) {
		String message =
				ofNullable(rejectionReason)
				.orElse(DEFAULT_REJECTION_MESSAGE);

		SubOrder subOrder = getSubOrder(order);
		changeShippingServiceName(subOrder);

		Map<String,Object> params = createOrgPropertiesParams(order.getOrganizationEntity());
		params.put("id", order.getId().toString());
		params.put("rejectionReason", message);
		params.put("sub", subOrder);
		params.put("userName", order.getMetaOrder().getUser().getName());
		return params;
	}




	private List<String> getStoreManagersEmails(OrdersEntity order) {
		Long shopId =
				ofNullable(order)
				.map(OrdersEntity::getShopsEntity)
				.map(ShopsEntity::getId)
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, O$SHP$0002 , order.getId()));
		return empRoleRepo.findEmailOfEmployeeWithRoleAndShop(STORE_MANAGER.getValue(), shopId);
	}




	private List<String> getOrganizationManagersEmails(OrdersEntity order) {
		Long orgId =
				ofNullable(order)
				.map(OrdersEntity::getOrganizationEntity)
				.map(OrganizationEntity::getId)
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, O$ORG$0001 , order.getId()));
		return empRoleRepo.findEmailOfEmployeeWithRoleAndOrganization(ORGANIZATION_MANAGER.getValue(), orgId);
	}





	private void sendBillEmail(MetaOrderEntity order, Boolean yeshteryMetaorder) {
		String orgName = order.getOrganization().getName();

		Optional<String> email =
				ofNullable(order)
				.map(MetaOrderEntity::getUser)
				.map(UserEntity::getEmail);

		if(!email.isPresent()) {
			return;
		}

		String subject = format(BILL_EMAIL_SUBJECT, orgName);
		Map<String,Object> parametersMap = createBillEmailParams(order, yeshteryMetaorder);
		String template = ORDER_BILL_TEMPLATE;
		try {
			mailService.sendThymeleafTemplateMail(orgName, email.get(), subject,  template, parametersMap);
		} catch (MessagingException e) {
			logger.error(e, e);
		}
	}



	private Map<String, Object> createOrgPropertiesParams(OrganizationEntity org) {
		String domain = domainService.getBackendUrl();
		String orgDomain = domainService.getOrganizationDomainAndSubDir(org.getId());
		String orgLogo = domain + "/files/"+ orderEmailHelper.getOrganizationLogo(org);
		String orgName = org.getName();
		String year = LocalDateTime.now().getYear()+"";

		Map<String, Object> params = new HashMap<>();
		params.put("orgDomain", orgDomain);
		params.put("domain", domain);
		params.put("orgName", orgName);
		params.put("orgLogo", orgLogo);
		params.put("year", year);

		return params;
	}



	private Map<String, Object> createBillEmailParams(MetaOrderEntity order, Boolean yeshteryMetaorder) {
		Order orderResponse = this.getOrderResponse(order, yeshteryMetaorder);

		LocalDateTime orderTime = orderResponse.getCreationDate();
		String orderTimeStr =
				DateTimeFormatter
				.ofPattern("dd/MM/YYYY - hh:mm")
				.format(orderTime);

		normalizeOrderForEmailTemplate(orderResponse);

		AddressRepObj deliveryAddress = getBillDeliveryAddress(order);

		String shipppingServiceName = getShippingServiceName(orderResponse).orElse("N/A");

		Map<String, Object> params = createOrgPropertiesParams(order.getOrganization());
		params.put("creation_date", orderTimeStr);
		params.put("data", orderResponse);
		params.put("deliveryAddress", deliveryAddress);
		params.put("shipppingServiceName", shipppingServiceName);
		return params;
	}



	private Optional<String> getShippingServiceName(Order orderResponse) {
		return ofNullable(orderResponse)
				.map(Order::getSubOrders)
				.map(List::stream)
				.flatMap(Stream::findFirst)
				.flatMap(this::getShippingServiceName);
	}



	private Optional<String> getShippingServiceName(SubOrder subOrder) {
		return ofNullable(subOrder)
				.map(SubOrder::getShipment)
				.map(Shipment::getServiceId)
				.flatMap(shippingMgrService::getShippingServiceInfo)
				.map(ShippingServiceInfo::getName);
	}




	private AddressRepObj getBillDeliveryAddress(MetaOrderEntity metaOrder) {
		OrdersEntity subOrder =
				ofNullable(metaOrder)
				.map(MetaOrderEntity::getSubOrders)
				.map(Set::stream)
				.flatMap(Stream::findFirst)
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, O$SHP$0005, metaOrder.getId()));
		return getBillDeliveryAddress(subOrder);
	}



	private AddressRepObj getBillDeliveryAddress(OrdersEntity order){
		AddressRepObj userAddress = (AddressRepObj)order.getAddressEntity().getRepresentation();
		return getPickupShopAddress(order)
				.orElse(userAddress);
	}



	private Optional<AddressRepObj> getPickupShopAddress(OrdersEntity order) {
		return ofNullable(order)
				.filter(this::isPickupOrder)
				.flatMap(this::getPickupShop)
				.map(this::getPickupShopAdress);
	}



	private boolean isPickupOrder(OrdersEntity order){
		return ofNullable(order)
				.map(OrdersEntity::getShipment)
				.map(ShipmentEntity::getShippingServiceId)
				.map(shippingMgrService::isPickupService)
				.orElse(false);
	}



	private Optional<ShopsEntity> getPickupShop(OrdersEntity order){
		return ofNullable(order)
				.map(OrdersEntity::getShipment)
				.flatMap(shippingMgrService::getPickupShop);
	}




	private void normalizeOrderForEmailTemplate(Order orderResponse) {
		String operator = changeOperatorName(orderResponse.getOperator());
		orderResponse.setOperator(operator);

		orderResponse
				.getSubOrders()
				.stream()
				.forEach(this::changeShippingServiceName);
	}

	private String changeOperatorName(String operator) {
		if (operator.equals("COD")) {
			return "Cash on delivery";
		}
		return operator;
	}


	private void changeShippingServiceName(SubOrder subOrder) {
		ShopsEntity shop = shopsRepo.findById(subOrder.getShopId()).get();

		AddressRepObj shopAddress = (AddressRepObj) shop.getAddressesEntity().getRepresentation();
		String shopAreaNameString = createShopAreaNameString(shop);
		Shipment shipment = subOrder.getShipment();
		String serviceId = shipment.getServiceId();
		if (shippingMgrService.isPickupService(serviceId)) {
			shipment.setServiceName("Pickup at " + subOrder.getShopName() + shopAreaNameString);
			AddressRepObj address = subOrder.getDeliveryAddress();
			BeanUtils.copyProperties(address, shopAddress);
			subOrder.setDeliveryAddress(shopAddress);
			subOrder.setPickup(true);
		}
		subOrder.setShipment(shipment);
	}



	private String createShopAreaNameString(ShopsEntity shop) {
		return ofNullable(shop)
				.map(ShopsEntity::getAddressesEntity)
				.map(AddressesEntity::getAreasEntity)
				.map(AreasEntity::getName)
				.map(areaName -> " - " + areaName)
				.orElse("");
	}



	private AddressRepObj getPickupShopAdress(ShopsEntity shop){
		AddressRepObj shopAddress = (AddressRepObj) shop.getAddressesEntity().getRepresentation();
		if(isNull(shopAddress.getFirstName())){
			shopAddress.setFirstName(shop.getName());
		}
		return shopAddress;
	}


	private BigDecimal getMetaOrderTotal(MetaOrderEntity order) {
		return order
				.getSubOrders()
				.stream()
				.map(OrdersEntity::getAmount)
				.reduce(ZERO, BigDecimal::add)
				.setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}





	private void finalizeSubOrder(OrdersEntity order) {
		reduceStocks(order);
		clearOrderItemsFromCart(order);
		updateOrderStatus(order, FINALIZED);
		userService.updateUserByTierIdAndOrgId(0L, order.getUserId(), order.getOrganizationEntity().getId());
	}




	private void clearOrderItemsFromCart(OrdersEntity order) {
		Long userId = order.getUserId();
		List<Long> variantIds =
				order
				.getBasketsEntity()
				.stream()
				.map(BasketsEntity::getStocksEntity)
				.map(StocksEntity::getProductVariantsEntity)
				.map(ProductVariantsEntity::getId)
				.collect(toList());
		cartItemRepo.deleteByVariantIdInAndUser_Id(variantIds, userId);
	}





	private void reduceStocks(OrdersEntity orderEntity) {
	   orderEntity.getBasketsEntity().forEach(this::reduceItemStock);
	}



	private void reduceItemStock(BasketsEntity item) {
		int quantity =
				ofNullable(item)
				.map(BasketsEntity::getQuantity)
				.map(BigDecimal::intValue)
				.orElse(0);
		stockService.reduceStockBy(item.getStocksEntity(), quantity);
	}



	@Override
	public OrdersEntity updateOrderStatus(OrdersEntity orderEntity, OrderStatus newStatus) {
		OrderStatus currentStatus = findEnum(orderEntity.getStatus());

		if(!canOrderStatusChangeTo(currentStatus, newStatus)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$GNRL$0001, currentStatus.name(), newStatus.name());
		}
		orderEntity.setStatus(newStatus.getValue());
		return ordersRepository.save(orderEntity);
	}




	private MetaOrderEntity updateOrderStatus(MetaOrderEntity metaOrderEntity, OrderStatus newStatus) {
		OrderStatus currentStatus = findEnum(metaOrderEntity.getStatus());

		if(!canOrderStatusChangeTo(currentStatus, newStatus)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$GNRL$0001, currentStatus.name(), newStatus.name());
		}
		metaOrderEntity.setStatus(newStatus.getValue());
		return metaOrderRepo.save(metaOrderEntity);
	}




	private void returnOrderToStocks(OrdersEntity order) {
		order
		.getBasketsEntity()
		.stream()
		.forEach(this::incrementItemStock);
	}




	private void incrementItemStock(BasketsEntity item) {
		int quantity =
				ofNullable(item)
				.map(BasketsEntity::getQuantity)
				.map(BigDecimal::intValue)
				.orElse(0);
		stockService.incrementStockBy(item.getStocksEntity(), quantity);
	}






	private boolean canOrderStatusChangeTo(OrderStatus currentStatus, OrderStatus newStatus) {
		return ofNullable(currentStatus)
				.map(orderStateMachine::get)
				.orElse(emptySet())
				.contains(newStatus);
	}





	@Override
	@Transactional
	public DetailedOrderRepObject getOrderInfo(Long orderId, Integer detailsLevel) {

		BaseUserEntity user = securityService.getCurrentUser();
		Long orgId = securityService.getCurrentUserOrganizationId();
		boolean isNasnavAdmin = securityService.currentUserHasRole(NASNAV_ADMIN);
		Optional<OrdersEntity> order = empty();

		Integer finalDetailsLevel = getFinalDetailsLevel(detailsLevel);

		if (user instanceof UserEntity ) {
			order = ordersRepository.findByIdAndUserIdAndOrganizationEntity_Id(orderId, user.getId(), orgId);
		} else if ( isNasnavAdmin ) {
			order = ordersRepository.findFullDataById(orderId);
		} else {
			order = ordersRepository.findByIdAndOrganizationEntity_Id(orderId, orgId);
		}

		return order
				.map(o -> getDetailedOrderInfo(o, finalDetailsLevel))
				.orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, O$0001, orderId));
	}


	private Integer getFinalDetailsLevel(Integer detailsLevel) {
		return (detailsLevel == null || detailsLevel < 0 || detailsLevel > 3) ? ORDER_FULL_DETAILS_LEVEL : detailsLevel;
	}



	private DetailedOrderRepObject getDetailedOrderInfo(OrdersEntity order, Integer detailsLevel) {
	    Map<Long, List<BasketItem>> basketItemsMap = getBasketItemsDetailsMap( setOf(order) );
		Map<Long, BigDecimal> orderItemsQuantity = getOrderItemsQuantity( setOf(order) );
		Map<Long, String> paymentOperator = getPaymentOperators(detailsLevel, setOf(order));
		String phoneNumber = userRepo.findById(order.getUserId()).get().getPhoneNumber();
	    return getDetailedOrderInfo(order, detailsLevel, orderItemsQuantity, basketItemsMap, phoneNumber ,paymentOperator);
	}




	private DetailedOrderRepObject getDetailedOrderInfo(OrdersEntity order, Integer detailsLevel,
														Map<Long, BigDecimal> orderItemsQuantity,
														Map<Long, List<BasketItem>> basketItemsMap,
														String phoneNumber,
														Map<Long, String> paymentOperator) {
		List<BasketItem> basketItems =
				ofNullable(basketItemsMap)
				.map(map -> map.get(order.getId()) )
				.orElse(new ArrayList<>());

		DetailedOrderRepObject representation = new DetailedOrderRepObject();
		setOrderSummary(order, representation);

        if (detailsLevel >= 1){
        	setOrderDetails(order, phoneNumber, paymentOperator, representation);
		}

        if (detailsLevel >= 2 && orderItemsQuantity.get(order.getId()) != null){
			representation.setTotalQuantity(orderItemsQuantity.get(order.getId()).intValue());
		}

		if (detailsLevel == 3) {
			representation.setItems(basketItems);
		}

		representation.setPoints(getOrderPoints(order));
		if(!isNullOrEmpty(representation.getPoints())) {
			BigDecimal totalPointAmount = representation.getPoints().stream().map(point -> ofNullable(point.getAmount()).orElse(ZERO))
					.reduce(ZERO, BigDecimal::add);
			representation.setTotalPointAmount(totalPointAmount);
		}
		return representation;
	}

	private List<LoyaltyOrderDetailDTO> getOrderPoints(OrdersEntity order) {
		List<LoyaltyPointTransactionEntity>  points = loyaltyPointTransactionRepository.findByOrder_Id(order.getId());

		return points
				.stream()
				.map(point -> new LoyaltyOrderDetailDTO(point.getAmount(), point.getPoints()))
				.collect(Collectors.toList());
	}


	private void setOrderSummary(OrdersEntity entity, DetailedOrderRepObject obj) {
		Long metaOrderId =
				ofNullable(entity)
				.map(OrdersEntity::getMetaOrder)
				.map(MetaOrderEntity::getId)
				.orElse(null);

		obj.setOrderId(entity.getId() );
		obj.setUserId(entity.getUserId());
		obj.setShopId(entity.getShopsEntity().getId());
		obj.setCreatedAt(entity.getCreationDate());
		obj.setStatus(findEnum(entity.getStatus()).name());
		obj.setPaymentStatus(entity.getPaymentStatus().toString());
		obj.setTotal(entity.getAmount());
		obj.setMetaOrderId(metaOrderId);
		obj.setDiscount(entity.getDiscounts());

		CountriesEntity country = entity.getOrganizationEntity().getCountry();
		if (country != null) {
			obj.setCurrency(country.getCurrency());
		}
	}



	private void setOrderDetails(OrdersEntity entity, String phoneNumber, Map<Long, String> paymentOperator,
												   DetailedOrderRepObject obj) {
		String notes = ofNullable(entity.getMetaOrder())
				.map(MetaOrderEntity::getNotes)
				.orElse("");
		obj.setUserName(entity.getName());
		obj.setShopName(entity.getShopsEntity().getName());
		obj.setDeliveryDate(entity.getDeliveryDate());
		obj.setSubtotal(entity.getAmount());
		obj.setNotes(notes);
		if (entity.getShipment() != null) {
			String shippingStatus = ShippingStatus.getShippingStatusName(entity.getShipment().getStatus());
			obj.setShipping(entity.getShipment().getShippingFee());
			obj.setShippingStatus(shippingStatus);
			obj.setShippingService(entity.getShipment().getShippingServiceId());
			obj.setTrackNumber(entity.getShipment().getTrackNumber());
		}

		obj.setTotal(entity.getTotal());
		obj.setPaymentOperator(paymentOperator.get(entity.getId()));

		if (entity.getAddressEntity() != null) {
			AddressRepObj address = (AddressRepObj) entity.getAddressEntity().getRepresentation();
			if (address.getPhoneNumber() == null && phoneNumber != null) {
				address.setPhoneNumber(phoneNumber);
			}
			obj.setShippingAddress(address);
		}
	}



	private BasketItem readBasketItem(BasketsEntity entity, Map<Long, Optional<String>> variantsCoverImages){
		String itemDataStr = ofNullable(entity.getItemData()).orElse("{}");
		if(itemDataStr.equals("{}")){
			return createBasketItemWithThumbnail(entity, variantsCoverImages);
		}
		try {
			BasketItem item = parseItemData(itemDataStr);
			//before item data json , only price, discount, and some other stocks data - at the time of purchase-
			//were saved in the BASKETS table. to stay consistent with this older design, we set these basic
			//data from the BASKETS table directly and consider it the source of truth, while ignoring their saved
			// values in item data json.
			//as some other part of the code may be depending on the old assumptions
			setBasketItemStockData(entity, item);
			addThumbnailToBasketItem(variantsCoverImages, item, entity);
			return item;
		} catch (Throwable e) {
			logger.error(e,e);
			return createBasketItemWithThumbnail(entity, variantsCoverImages);
		}
	}



	private BasketItem parseItemData(String itemData) throws IOException {
		return objectMapper.readValue(itemData, BasketItem.class);
	}


	private BasketItem createBasketItemWithThumbnail(BasketsEntity entity, Map<Long, Optional<String>> variantsCoverImages) {
		BasketItem item = createBasketItemEntity(entity);
		addThumbnailToBasketItem(variantsCoverImages, item, entity);
		return item;
	}



	private BasketItem createBasketItemEntity(BasketsEntity entity) {
		BasketItem item = createBasketItemWithBasicData(entity);
		return addProductDataToBasketItem(entity, item);
	}



	private BasketItem addProductDataToBasketItem(BasketsEntity entity, BasketItem originalItem) {
		BasketItem item = new BasketItem();
		BeanUtils.copyProperties(originalItem, item);

		ProductVariantsEntity variant = entity.getStocksEntity().getProductVariantsEntity();
		ProductEntity product = variant.getProductEntity();
		BrandsEntity brand = product.getBrand();

		item.setProductId(product.getId());
		item.setName(product.getName());
		item.setPname(product.getPname());
		item.setProductType(product.getProductType());
		item.setBrandId(brand.getId());
		item.setBrandName(brand.getName());
		item.setBrandLogo(brand.getLogo());

		item.setVariantId(variant.getId());
		item.setVariantName(variant.getName());
		item.setVariantFeatures(parseVariantFeatures(variant, 0));
		item.setSku(variant.getSku());
		item.setProductCode(variant.getProductCode());

		return item;
	}



	private void addThumbnailToBasketItem(Map<Long, Optional<String>> variantsCoverImages, BasketItem item, BasketsEntity entity) {
		ProductVariantsEntity variant = entity.getStocksEntity().getProductVariantsEntity();
		String thumb = variantsCoverImages.get(variant.getId()).orElse(null);
		item.setThumb(thumb);
	}



	private BasketItem createBasketItemWithBasicData(BasketsEntity entity) {
		BasketItem item = new BasketItem();
		setBasketItemStockData(entity, item);
		return item;
	}



	private void setBasketItemStockData(BasketsEntity entity, BasketItem item) {
		BigDecimal price = entity.getPrice();
		BigDecimal discount = ofNullable(entity.getDiscount()).orElse(ZERO);
		BigDecimal totalPrice = price.multiply(entity.getQuantity());
		OrganizationEntity org = entity.getOrdersEntity().getOrganizationEntity();
		String currency = ofNullable(getTransactionCurrency(entity.getCurrency())).orElse(EGP).name();
		CountriesEntity country = org.getCountry();
		String currencyValue = ofNullable(country).map(CountriesEntity::getCurrency).orElse("");
		Boolean isReturnable = orderReturnService.isReturnable(entity);
		String unitName = getUnit(entity);
		Integer quantityLimit = settingRepo.findBySettingNameAndOrganization_Id(STOCK_ALERT_LIMIT.name(), org.getId())
				.map(SettingEntity::getSettingValue)
				.map(Integer::parseInt)
				.orElse(10);

		item.setId(entity.getId());
		item.setUnit(unitName);
		item.setOrderId(entity.getOrdersEntity().getId());
		item.setStockId(entity.getStocksEntity().getId());
		item.setQuantity(entity.getQuantity().intValueExact());
		item.setTotalPrice(totalPrice);
		item.setPrice(price);
		item.setDiscount(discount);
		item.setCurrency(currency);
		item.setIsReturnable(isReturnable);
		item.setCurrencyValue(currencyValue);

		if(entity.getStocksEntity().getQuantity() < quantityLimit) {
			item.setAvailableStock(entity.getStocksEntity().getQuantity());
		}
	}



	private String getUnit(BasketsEntity entity) {
		return ofNullable(entity)
				.map(BasketsEntity::getStocksEntity)
				.map(StocksEntity::getUnit)
				.map(StockUnitEntity::getName)
				.orElse("");
	}



	@Override
	public List<DetailedOrderRepObject> getOrdersList(OrderSearchParam params) throws BusinessException {
		OrderSearchParam finalParams = getFinalOrderSearchParams(params);
		Integer detailsLevel = finalParams.getDetails_level();

		List<OrdersEntity> ordersEntityList = em.createQuery(getOrderCriteriaQuery(finalParams))
												.setFirstResult(finalParams.getStart())
												.setMaxResults(finalParams.getCount())
												.getResultList();

		Set<OrdersEntity> orders = new HashSet<>();

		if ( detailsLevel >= 2) {
			orders = ordersEntityList.stream().collect(toSet());
		}

		Map<Long, List<BasketItem>> basketItemsDetailsMap = getBasketItemsDetailsMap(orders);

		Map<Long, BigDecimal> orderItemsQuantity = getOrderItemsQuantity(detailsLevel >= 2 ? orders : new HashSet<>());

		Map<Long, String> orderPhones = getCustomerPhones(orders);
		Map<Long, String> paymentOperator = getPaymentOperators(detailsLevel, orders);
		return ordersEntityList
				.stream()
				.map(order -> getDetailedOrderInfo(order, detailsLevel, orderItemsQuantity, basketItemsDetailsMap, orderPhones.get(order.getId()), paymentOperator))
				.collect(toList());
	}



	private Map<Long, String> getPaymentOperators(Integer detailsLevel, Set<OrdersEntity> orders) {
		if(detailsLevel < 2 || orders.isEmpty()){
			return emptyMap();
		}
		Set<Long> ordersIds = orders.stream().map(OrdersEntity::getId).collect(toSet());
		return ordersRepository
				.findPaymentOperatorByOrderIdIn(ordersIds)
				.stream()
				.filter(payOpr -> noneIsNull(payOpr, payOpr.getOrderId(), payOpr.getOperator()))
				.collect(toMap(OrderPaymentOperator::getOrderId, OrderPaymentOperator::getOperator, (p1, p2) -> p1));
	}



	private Map<Long, String> getCustomerPhones(Set<OrdersEntity> orders) {
		Set<Long> ordersIds = orders.stream().map(OrdersEntity::getId).collect(toSet());
		return !orders.isEmpty() ?
					ordersRepository
					.findUsersPhoneNumber(ordersIds)
					.stream()
					.collect(toMap(OrderPhoneNumberPair::getOrderId, pair -> ofNullable(pair.getPhoneNumber()).orElse("")))
				: new LinkedHashMap<>();
	}




	private Map<Long, List<BasketItem>> getBasketItemsDetailsMap(Set<OrdersEntity> orders) {
		Map<Long, Optional<String>> getVariantsImagesList = orderServiceHelper.getVariantsImagesList(orders);
		return orders
				.stream()
				.map(OrdersEntity::getBasketsEntity)
				.flatMap(Set::stream)
				.map(basket -> readBasketItem(basket, getVariantsImagesList))
				.collect( groupingBy(BasketItem::getOrderId));
	}




	private Map<Long, BigDecimal> getOrderItemsQuantity(Set<OrdersEntity> orders) {
		List<BasketsEntity> basketsEntities =
				orders
				.stream()
				.map(OrdersEntity::getBasketsEntity)
				.flatMap(Set::stream)
				.collect(toList());

		Map<Long, BigDecimal> ordersQuantities = new HashMap<>();
		for(BasketsEntity basket: basketsEntities) {
			Long orderId = basket.getOrdersEntity().getId();
			if (ordersQuantities.get(orderId) != null)
				ordersQuantities.put(orderId, ordersQuantities.get(orderId).add(basket.getQuantity()));
			else
				ordersQuantities.put(orderId, basket.getQuantity());
		}
		return ordersQuantities;
	}



	private OrderSearchParam getFinalOrderSearchParams(OrderSearchParam params) throws BusinessException {
		Integer detailsLevel = ofNullable(params.getDetails_level()).orElse(0);

		OrderSearchParam newParams = new OrderSearchParam();
		newParams.setStatus_id(getOrderStatusId(params.getStatus()));
		newParams.setDetails_level( detailsLevel);
		newParams.setUpdated_after( params.getUpdated_after() );
		newParams.setUpdated_before( params.getUpdated_before() );

		BaseUserEntity user = securityService.getCurrentUser();
		if (user instanceof EmployeeUserEntity) {
			newParams.setUser_id(params.getUser_id());
			limitSearchParamByUserRole(params, newParams, user);
		} else {
			newParams.setUser_id(user.getId());
			newParams.setOrg_id(user.getOrganizationId());
		}

		setOrderSearchStartAndCount(params, newParams);

		return newParams;
	}





	private void limitSearchParamByUserRole(OrderSearchParam params, OrderSearchParam newParams, BaseUserEntity user) {
		EmployeeUserEntity empUser = (EmployeeUserEntity)user;
		List<String> employeeUserRoles = userServicesHelper.getEmployeeUserRoles(empUser.getId());

		if ( collectionContainsAnyOf(employeeUserRoles, ORGANIZATION_ADMIN.name() ,ORGANIZATION_MANAGER.name(), ORGANIZATION_EMPLOYEE.name()) )  {
			newParams.setOrg_id(empUser.getOrganizationId());
		} else if ( collectionContainsAnyOf(employeeUserRoles, STORE_MANAGER.name(), STORE_EMPLOYEE.name())) {
			newParams.setShop_id(empUser.getShopId());
		} else {
			newParams.setOrg_id(params.getOrg_id());
			newParams.setShop_id(params.getShop_id());
		}
	}



	private void setOrderSearchStartAndCount(OrderSearchParam params, OrderSearchParam newParams) {
		if (params.getStart() == null || params.getStart() <= 0){
			newParams.setStart(0);
		}else{
			newParams.setStart(params.getStart());
		}

		if (params.getCount() == null || params.getCount() <= 0 || params.getCount() >= ORDER_DEFAULT_COUNT){
			newParams.setCount(ORDER_DEFAULT_COUNT);
		}else{
			newParams.setCount(params.getCount());
		}
	}



	private Integer getOrderStatusId(String status) throws BusinessException {
		if (status != null) {
			OrderStatus statusEnum = findEnum(status);
			if (statusEnum == null) {
				throw new BusinessException("Provided status (" + status + ") doesn't match any existing status!","INVALID PARAM: status",HttpStatus.BAD_REQUEST);
			}
			return findEnum(status).getValue();
		}

		return null;
	}





	private CriteriaQuery<OrdersEntity> getOrderCriteriaQuery(OrderSearchParam params) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<OrdersEntity> query = builder.createQuery(OrdersEntity.class);
		Root<OrdersEntity> root = query.from(OrdersEntity.class);
		root.fetch("metaOrder", LEFT);
		root.fetch("shipment", LEFT);
		root.fetch("addressEntity", LEFT)
				.fetch("areasEntity", LEFT)
				.fetch("citiesEntity", LEFT)
				.fetch("countriesEntity", LEFT);
		root.fetch("basketsEntity", LEFT)
				.fetch("stocksEntity", LEFT)
				.fetch("productVariantsEntity", LEFT)
				.fetch("productEntity", LEFT);
		root.fetch("organizationEntity", LEFT);

		Predicate[] predicatesArr = getOrderQueryPredicates(params, builder, root);

		query
		.where(predicatesArr)
		.orderBy(builder.desc(root.get("updateDate")));

		return query;
	}












	private Predicate[] getOrderQueryPredicates(OrderSearchParam params, CriteriaBuilder builder, Root<OrdersEntity> root) {
		List<Predicate> predicates = new ArrayList<>();

		predicates.add( builder.notEqual(root.get("status"), DISCARDED.getValue()) );

		if(params.getUser_id() != null)
			predicates.add( builder.equal(root.get("userId"), params.getUser_id()) );

		if(params.getOrg_id() != null)
			predicates.add( builder.equal(root.get("organizationEntity").get("id"), params.getOrg_id()) );

		if(params.getShop_id() != null)
			predicates.add( builder.equal(root.get("shopsEntity").get("id"), params.getShop_id()) );

		if(params.getStatus_id() != null)
			predicates.add( builder.equal(root.get("status"), params.getStatus_id()) );

		if(params.getUpdated_after() != null) {
			predicates.add( builder.greaterThanOrEqualTo( root.<LocalDateTime>get("updateDate"), builder.literal(readDate(params.getUpdated_after())) ) );
		}

		if(params.getUpdated_before() != null) {
			predicates.add( builder.lessThanOrEqualTo( root.<LocalDateTime>get("updateDate"), builder.literal(readDate(params.getUpdated_before()))) );
		}

		return predicates.stream().toArray( Predicate[]::new) ;
	}





	private LocalDateTime readDate(String dateStr) {
		return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss"));
	}




	private BusinessException getNoCurrentOrderFoundException() {
		return new BusinessException("User have no new orders!", "NOT FOUND", HttpStatus.NOT_FOUND);
	}





	//TODO the external stock check must be included in the checkout logic
	private void validateOrderForCheckout(OrdersEntity order) {
		validateOrderStatusForCheckOut(order);
		Long orgId = order.getOrganizationEntity().getId();
		//TODO add integration parameter for disabling this update if needed
		if(integrationService.hasActiveIntegration(orgId)) {
			updateOrderItemStocksFromExternalSys(order);
		}
		order.getBasketsEntity().forEach(this::validateBasketItem);
	}





	private void updateOrderItemStocksFromExternalSys(OrdersEntity order) {
		order.getBasketsEntity().forEach(this::updateItemStockFromExternalSys);
	}




	private void updateItemStockFromExternalSys(BasketsEntity item) {
		try {
			StockBasicData stockData = getItemStockData(item);
			fetchAndUpdateStockIfExists(item, stockData);
		} catch (Throwable e) {
			logger.error(e, e);
		}
	}





	private StockBasicData getItemStockData(BasketsEntity item) throws BusinessException {
		StockBasicData stockData = basketRepository.getItemStockBasicDataById(item.getId());
		Long variantId = stockData.getVariantId();
		Long shopId = stockData.getShopId();
		if(anyIsNull(variantId, shopId)) {
			throw new BusinessException(
					format("Basket item[%d] has invalid variant-shop data [%d - %d]", item.getId(), variantId, shopId)
					, "INVALID DATA"
					, INTERNAL_SERVER_ERROR);
		}
		return stockData;
	}





	private void fetchAndUpdateStockIfExists(BasketsEntity item, StockBasicData stockData)
			throws InvalidIntegrationEventException, BusinessException {
		Optional<Integer> extStock = integrationService.getExternalStock(stockData.getVariantId(), stockData.getShopId());
		if(extStock.isPresent()) {
			StocksEntity stock = item.getStocksEntity();
			stock.setQuantity(extStock.get());
			stockRepository.save(stock);
		}
	}







	private void validateOrderStatusForCheckOut(OrdersEntity order) {
		Integer status =
				ofNullable(order)
					.map(OrdersEntity::getStatus)
					.orElse(-1);
		if(!NEW.getValue().equals(status)) {
			throw new RuntimeBusinessException(
					format("Order with id[%d] has invalid status[%d] and can't be checked out!", order.getId(), status)
					, "Invalid Operation"
					, NOT_ACCEPTABLE);
		}
	};



	private void validateBasketItem(BasketsEntity item) {
		Integer quantity =
				ofNullable(item)
				.map(BasketsEntity::getQuantity)
				.map(BigDecimal::intValue)
				.orElse(0);
		if(quantity > stockService.getStockQuantity( item.getStocksEntity() )) {
			throwRuntimeInvalidOrderException(ERR_NO_ENOUGH_STOCK);
		}
	}



	@Override
	public void setOrderAsPaid(PaymentEntity payment, OrdersEntity order) {
		order.setPaymentStatus(PaymentStatus.PAID);
		order.setPaymentEntity(payment);
		ordersRepository.save(order);
	}



	private Map<String, String> parseVariantFeatures(ProductVariantsEntity variant, Integer returnedName) {
		return productService.parseVariantFeatures(variant, returnedName);
	}



	@Override
	@Transactional(rollbackFor = Throwable.class)
	public OrderConfirmResponseDTO confirmOrder(Long orderId, String pinCode, BigDecimal pointsAmount) {
		EmployeeUserEntity storeMgr = getAndValidateUser();
		OrdersEntity subOrder = getAndValidateOrderForConfirmation(orderId, storeMgr);
		if(!isNull(pinCode)) {
			Optional<LoyaltyPinsEntity> pinEntity = loyaltyPinsRepository.findByUser_IdAndShop_IdAndPin(subOrder.getUserId(), subOrder.getShopsEntity().getId(), pinCode);
			if(pinEntity.isEmpty()) {
				throw new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0017, pinEntity);
			}
		}

		confirmSubOrderAndMetaOrder(subOrder, pointsAmount);

		return  shippingMgrService
				.requestShipment(subOrder)
				.doOnNext(trackerData -> saveShipmentTracker(trackerData, subOrder))
				.map(OrderConfirmResponseDTO::new)
				.blockOptional(Duration.ofSeconds(120))
				.orElseThrow( () -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, O$SHP$0001, subOrder.getId()));
	}





	private void setPromoCodeAsUsed(MetaOrderEntity metaOrder) {
		metaOrder
		.getPromotions()
		.forEach(promo -> promoService.setPromotionAsUsed(promo, metaOrder.getUser()));
	}





	private OrdersEntity getAndValidateOrderForConfirmation(Long orderId, EmployeeUserEntity user) {
		OrdersEntity order;
		if(securityService.currentUserHasRole(ORGANIZATION_MANAGER)) {
			order = ordersRepository
						.findByIdAndOrganizationEntity_Id(orderId, user.getOrganizationId())
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$CFRM$0004, user.getOrganizationId(), orderId));
		}else {
			 order = ordersRepository
						.findByIdAndShopsEntity_Id(orderId, user.getShopId())
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$CFRM$0001, user.getShopId(), orderId));
		}

		OrderStatus status = order.getOrderStatus();
		if(FINALIZED != status) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CFRM$0002, orderId, status.name());
		}
		return order;
	}





	private OrdersEntity getAndValidateOrderForRejection(Long orderId, EmployeeUserEntity user) {
		OrdersEntity order;
		if(securityService.currentUserHasRole(ORGANIZATION_MANAGER)) {
			order = ordersRepository
						.findByIdAndOrganizationEntity_Id(orderId, user.getOrganizationId())
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$CFRM$0004, user.getOrganizationId(), orderId));
		}else {
			 order = ordersRepository
						.findByIdAndShopsEntity_Id(orderId, user.getShopId())
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$CFRM$0001, user.getShopId(), orderId));
		}

		OrderStatus status = order.getOrderStatus();
		if(FINALIZED != status) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RJCT$0002, orderId, status.name());
		}
		return order;
	}





	private void confirmSubOrderAndMetaOrder(OrdersEntity order, BigDecimal pointsAmount) {
		updateOrderStatus(order, STORE_CONFIRMED);

		loyaltyPointsService.createLoyaltyPointTransaction(order, pointsAmount);

		MetaOrderEntity metaOrder = order.getMetaOrder();
		if(isAllOtherOrdersConfirmed(order.getId(), metaOrder)) {
			updateOrderStatus(metaOrder, STORE_CONFIRMED);
		}
	}




	private void rejectSubOrderAndMetaOrder(OrdersEntity order) {
		updateOrderStatus(order, STORE_CANCELLED);
		returnOrderToStocks(order);

		MetaOrderEntity metaOrder = order.getMetaOrder();
		if(isAllOtherOrdersRejected(order.getId(), metaOrder)) {
			updateOrderStatus(metaOrder, STORE_CANCELLED);
		}
	}




	private void saveShipmentTracker(ShipmentTracker tracker, OrdersEntity order) {
		ShipmentEntity shipment =
				ofNullable(order)
				.map(OrdersEntity::getShipment)
				.orElseThrow( () -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, O$SHP$0001, order.getId()));

		shipment.setExternalId(tracker.getShipmentExternalId());
		shipment.setTrackNumber(tracker.getTracker());
		shipment.setStatus(REQUESTED.getValue());
		ShipmentEntity saved = shipmentRepo.save(shipment);
		order.setShipment(saved);
	}



	private EmployeeUserEntity getAndValidateUser() {
		BaseUserEntity user = securityService.getCurrentUser();
		if(!(user instanceof EmployeeUserEntity)) {
			throw new RuntimeBusinessException(FORBIDDEN, G$USR$0001);
		}
		return (EmployeeUserEntity)user;
	}




	private boolean isAllOtherOrdersConfirmed(Long orderId, MetaOrderEntity metaOrder) {
		return isAllOtherOrdersHaveStatus(orderId, metaOrder, STORE_CONFIRMED);
	}


	private boolean isAllOtherOrdersRejected(Long orderId, MetaOrderEntity metaOrder) {
		return isAllOtherOrdersHaveStatus(orderId, metaOrder, STORE_CANCELLED);
	}


	private boolean isAllOtherOrdersHaveStatus(Long orderId, MetaOrderEntity metaOrder, OrderStatus status) {
		return metaOrder
				.getSubOrders()
				.stream()
				.filter(ord -> !Objects.equals(ord.getId(), orderId))
				.allMatch(ord -> Objects.equals(status.getValue() , ord.getStatus()));
	}

	private boolean isAllOtherSubMetaOrdersHaveStatus(Long metaOrderId, Set<MetaOrderEntity> subMetaOrders, OrderStatus status) {
		return subMetaOrders
				.stream()
				.filter(ord -> !Objects.equals(ord.getId(), metaOrderId))
				.allMatch(ord -> Objects.equals(status.getValue() , ord.getStatus()));
	}


	public ArrayList<OrdersEntity> getOrdersForMetaOrder(Long metaOrderId) {
		if (metaOrderId == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(ordersRepository.findByMetaOrderId(metaOrderId));
	}



	private void cancelAbandonedOrders() {
		BaseUserEntity user = securityService.getCurrentUser();
		List<MetaOrderEntity> abandonedOrders =
				metaOrderRepo
				.findByUser_IdAndStatusAndPaymentStatusIn(
						user.getId()
						, CLIENT_CONFIRMED.getValue()
						, asList(ERROR.getValue(), FAILED.getValue()));

		List<MetaOrderEntity> noPaymentOrders =
				metaOrderRepo
				.findByUser_IdAndStatusAndNoPayment(
						user.getId()
						, CLIENT_CONFIRMED.getValue());

		abandonedOrders.addAll(noPaymentOrders);
		abandonedOrders.forEach(this::discardAbandonedOrder);
	}


	private void discardAbandonedOrder(MetaOrderEntity metaOrder) {
		updateOrderStatus(metaOrder, DISCARDED);
		metaOrder
		.getSubOrders()
		.forEach(subOrder -> updateOrderStatus(subOrder, DISCARDED));
	}



	@Override
	public MetaOrderEntity createMetaOrder(CartCheckoutDTO dto, OrganizationEntity org, BaseUserEntity user) {

		AddressesEntity userAddress = getAddressById(dto.getAddressId(), user.getId());

		CartItemsGroupedById checkOutData = getAndValidateCheckoutData(dto, org);
		MetaOrderEntity order = createOrder( checkOutData, userAddress, dto, org, (UserEntity) user);
		return order;
	}


	private AddressesEntity getAddressById(Long addressId, Long userId) {
		return addressRepo
						.findByIdAndUserId(addressId, userId)
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0002, addressId));
	}


	private CartItemsGroupedById getAndValidateCheckoutData(CartCheckoutDTO checkoutDto, OrganizationEntity org) {
		//TODO: this should be moved to checkOut main method, and then passes
		//the optimized cart to the rest of the logic.
		List<CartCheckoutData> userCartItems = getOptimizedCheckoutDataList(checkoutDto);

		validateCartCheckoutItems(userCartItems, org);

		shippingMgrService.validateCartForShipping(userCartItems, checkoutDto, org.getId());

		return userCartItems
				.stream()
				.collect(collectingAndThen(
						groupingBy(CartCheckoutData::getShopId)
						, CartItemsGroupedById::new));
	}

	private List<CartCheckoutData> getOptimizedCheckoutDataList(CartCheckoutDTO checkoutDto) {
		Cart optimizedCart = optimizeCartForCheckout(checkoutDto);
		return createCheckoutData(optimizedCart);
	}


	private CartItemsGroupedByOrgId getAndValidateCheckoutDataByOrgId(CartCheckoutDTO checkoutDto) {
		List<CartCheckoutData> userCartItems = getOptimizedCheckoutDataList(checkoutDto);

		CartItemsGroupedById result = userCartItems
				.stream()
				.collect(collectingAndThen(
						groupingBy(CartCheckoutData::getOrganizationId)
						, CartItemsGroupedById::new));

		return result
				.entrySet()
				.stream()
				.collect(collectingAndThen(toMap(e -> e.getKey(), e -> this.groupItemsByShopId(e.getValue())), CartItemsGroupedByOrgId::new));
	}

	private CartItemsGroupedById groupItemsByShopId(List<CartCheckoutData> value ) {
		return value.stream().collect(collectingAndThen(groupingBy(CartCheckoutData::getShopId), CartItemsGroupedById::new));
	}


	private Cart optimizeCartForCheckout(CartCheckoutDTO checkoutDto) {
		CartOptimizeResponseDTO optimizationResult =
				cartOptimizationService.optimizeCart(checkoutDto);
		if(optimizationResult.getTotalChanged()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CHK$0004, " prices");
		}
		if(optimizationResult.getItemsChanged()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CHK$0004, "s");
		}

		return optimizationResult.getCart();
	}






	@Override
	public List<CartCheckoutData> createCheckoutData(Cart optimizedCart) {
		List<Long> cartStocks =
				optimizedCart
				.getItems()
				.stream()
				.map(CartItem::getStockId)
				.collect(toList());
		Map<Long, Map<String, String>> variantsFeaturesMap = variantsRepo.findByStockIdIn(cartStocks)
				.stream()
				.collect(toMap(ProductVariantsEntity::getId, variant -> parseVariantFeatures(variant, 0)));
		Map<Long, StockAdditionalData> stockAdditionalDataCache =
				stockRepository
				.findAdditionalDataByStockIdIn(cartStocks)
				.stream()
				.collect(groupingBy(StockAdditionalData::getStockId))
				.entrySet()
				.stream()
				.map(stock -> this.createStockAdditionalDataEntry(stock, variantsFeaturesMap))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		return optimizedCart
				.getItems()
				.stream()
				.map(item -> createCartCheckoutData(item, stockAdditionalDataCache))
				.collect(toList());
	}




	private Map.Entry<Long, StockAdditionalData> createStockAdditionalDataEntry(Map.Entry<Long, List<StockAdditionalData>> entry,
																				Map<Long, Map<String, String>> features){
		StockAdditionalData data =
				entry
				.getValue()
				.stream()
				.findFirst()
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, G$STK$0001, entry.getKey()));
		data.setFeatures(features.get(data.getVariantId()));
		return new SimpleEntry<Long, StockAdditionalData>(entry.getKey(), data);
	}





	private CartCheckoutData createCartCheckoutData(CartItem item, Map<Long, StockAdditionalData> stockDataMap) {
		StockAdditionalData stockData =
				ofNullable(item)
				.map(CartItem::getStockId)
				.map(stockDataMap::get)
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, G$STK$0001, item.getStockId()));
		CartCheckoutData checkoutData = new CartCheckoutData();
		checkoutData.setCurrency(stockData.getCurrency());
		checkoutData.setFeatures(stockData.getFeatures());
		checkoutData.setId(item.getId());
		checkoutData.setOrganizationId(stockData.getOrganizationId());
		checkoutData.setPrice(item.getPrice());
		checkoutData.setProductName(stockData.getProductName());
		checkoutData.setQuantity(item.getQuantity());
		checkoutData.setShopAddress(stockData.getShopAddress());
		checkoutData.setShopId(stockData.getShopId());
		checkoutData.setStockId(item.getStockId());
		checkoutData.setVariantBarcode(stockData.getVariantBarcode());
		checkoutData.setDiscount(stockData.getDiscount());
		checkoutData.setWeight(item.getWeight());
		return checkoutData;
	}



	@Override
	@Transactional(rollbackFor = Throwable.class)
	public Order createOrder(CartCheckoutDTO dto) {
		BaseUserEntity user = securityService.getCurrentUser();
		if(user instanceof EmployeeUserEntity) {
			throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
		}
		OrganizationEntity org = securityService.getCurrentUserOrganization();
		cancelAbandonedOrders();

		validateCartCheckoutDTO(dto);



		MetaOrderEntity order = createMetaOrder(dto, org, user);

		return getOrderResponse(order, false);
	}

	@Override
	public Integer countOrdersByUserId(Long userId) {
		return ordersRepository.countAllByUserId(userId);
	}

	@Override
	public String trackOrder(Long orderId) {
		return shippingMgrService.getTrackingUrl(orderId);
	}


	@Override
	public DetailedOrderRepObject getYeshteryOrderInfo(Long orderId, Integer detailsLevel) throws BusinessException {
		return getOrderInfo(orderId, detailsLevel);
	}

	@Override
	public List<DetailedOrderRepObject> getYeshteryOrdersList(OrderSearchParam params) throws BusinessException {
		return getOrdersList(params);
	}

	@Override
	public MetaOrderEntity createYeshteryMetaOrder(CartCheckoutDTO dto) {
		OrganizationEntity org = securityService.getCurrentUserOrganization();
		UserEntity user = (UserEntity)securityService.getCurrentUser();

		Optional<PromotionsEntity> promotion=
				ofNullable(dto.getPromoCode())
						.flatMap(promoCode ->
								promoRepo
										.findByCodeAndOrganization_IdAndActiveNow(promoCode, org.getId()));
		BigDecimal subTotal = ZERO;
		BigDecimal shippingFeeTotal = ZERO;
		BigDecimal total = ZERO;
		BigDecimal discounts = ZERO;

		MetaOrderEntity order = new MetaOrderEntity();
		order.setOrganization(org);
		order.setUser(user);
		order.setStatus(CLIENT_CONFIRMED.getValue());
		order.setGrandTotal(total);
		order.setSubTotal(subTotal);
		order.setShippingTotal(shippingFeeTotal);
		order.setDiscounts(discounts);
		order.setNotes(dto.getNotes());

		metaOrderRepo.save(order);
		// 1- group Items per org
		CartItemsGroupedByOrgId checkOutData = getAndValidateCheckoutDataByOrgId(dto);

		// 2- create metaorder per org ... just call createOrder method
		Set<MetaOrderEntity> subMetaOrders = createMetaOrders(checkOutData, dto);

		// 3- link met orders with the main meta order
		subMetaOrders.forEach(order::addSubMetaOrder);
		order.setSubMetaOrders(subMetaOrders);
		// 4- calculate totals and discounts
		subTotal = subMetaOrders.stream().map(subMetaOrder->calculateSubTotal(subMetaOrder.getSubOrders())).reduce(ZERO, BigDecimal::add);
		shippingFeeTotal = subMetaOrders.stream().map(subMetaOrder->calculateShippingTotal(subMetaOrder.getSubOrders())).reduce(ZERO, BigDecimal::add);
		total = subMetaOrders.stream().map(subMetaOrder->calculateTotal(subMetaOrder.getSubOrders())).reduce(ZERO, BigDecimal::add);
		discounts = subMetaOrders.stream().map(subMetaOrder->calculateDiscounts(subMetaOrder.getSubOrders())).reduce(ZERO, BigDecimal::add);
		order.setGrandTotal(total);
		order.setSubTotal(subTotal);
		order.setShippingTotal(shippingFeeTotal);
		order.setDiscounts(discounts);

		promotion.ifPresent(order::addPromotion);
		order = metaOrderRepo.save(order);
		// 5- return the order info
		return order;
	}

	private Set<MetaOrderEntity> createMetaOrders(CartItemsGroupedByOrgId checkOutData, CartCheckoutDTO dto) {
		UserEntity user = (UserEntity)securityService.getCurrentUser();
		AddressesEntity address = getAddressById(dto.getAddressId(), user.getId());
		Set<MetaOrderEntity> yeshteryOrders = checkOutData
				.entrySet()
				.stream()
				.map(c -> createYeshteryOrder(c.getValue(), address, dto, c.getKey()))
				.collect(toSet());
		return yeshteryOrders;
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public Order createYeshteryOrder(CartCheckoutDTO dto) {
		BaseUserEntity user = securityService.getCurrentUser();
		if(user instanceof EmployeeUserEntity) {
			throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
		}

		cancelAbandonedOrders();

		validateCartCheckoutDTO(dto);

		MetaOrderEntity order = createYeshteryMetaOrder(dto);

		return getOrderResponse(order, true);
	}

	@Override
	public List<MetaOrderBasicInfo> getYeshteryMetaOrderList() {
		BaseUserEntity user = securityService.getCurrentUser();
		Long yeshteryOrgId = getYeshteryOrgId();

		return metaOrderRepo.getYeshteryMetaOrderList(user.getId(), yeshteryOrgId)
				.stream()
				.map(this::setOrderStatus)
				.map(this::setPaymentStatus)
				.collect(toList());
	}
	private MetaOrderEntity createYeshteryOrder(Map<Long, List<CartCheckoutData>> shopCartsMap, AddressesEntity address, CartCheckoutDTO dto, Long orgId) {
		OrganizationEntity org = organizationRepository.findOneById(orgId);
		return createYeshteryOrder(shopCartsMap, address, dto, org);
	}

	private MetaOrderEntity createYeshteryOrder(Map<Long, List<CartCheckoutData>> shopCartsMap, AddressesEntity address, CartCheckoutDTO dto, OrganizationEntity org) {
		UserEntity user = (UserEntity)securityService.getCurrentUser();
		Optional<PromotionsEntity> promotion=
				ofNullable(dto.getPromoCode())
						.flatMap(promoCode ->
								promoRepo
										.findByCodeAndOrganization_IdAndActiveNow(promoCode, org.getId()));

		List<CartItemsForShop> cartDividedByShop = groupCartItemsByShop(shopCartsMap, org);
		Set<OrdersEntity> subOrders = createYeshterySubOrders(cartDividedByShop, address, dto, org);

		BigDecimal subTotal = calculateSubTotal(subOrders);
		BigDecimal shippingFeeTotal = calculateShippingTotal(subOrders);
		BigDecimal total = calculateTotal(subOrders);
		BigDecimal discounts = calculateDiscounts(subOrders);


		MetaOrderEntity order = new MetaOrderEntity();
		order.setOrganization(org);
		order.setUser(user);
		order.setStatus(CLIENT_CONFIRMED.getValue());
		order.setGrandTotal(total);
		order.setSubTotal(subTotal);
		order.setShippingTotal(shippingFeeTotal);
		order.setDiscounts(discounts);
		order.setNotes(dto.getNotes());
		subOrders.forEach(order::addSubOrder);
		promotion.ifPresent(order::addPromotion);

		return metaOrderRepo.save(order);
	}


	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void cancelYeshteryOrder(Long metaOrderId) {
		cancelOrder(metaOrderId);
	}

	private void validateCartCheckoutDTO(CartCheckoutDTO dto){
		if (dto.getAddressId() == null) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0004);
		}
		if (dto.getServiceId() == null) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CHK$0002);
		}
	}



	private Order getOrderResponse(MetaOrderEntity order, boolean yeshteryMetaorder) {
		Order orderDto = setMetaOrderBasicData(order, yeshteryMetaorder);
		List<SubOrder> subOrders = new ArrayList<>();
		if (yeshteryMetaorder) {
			subOrders = createSubOrderDtoListForYeshteryMetaorder(order);
		}
		else {
			subOrders = createSubOrderDtoList(order);
		}
		Boolean isCancelable = isCancelable(order);

		orderDto.setSubOrders(subOrders);
		orderDto.setSubtotal(order.getSubTotal());
		orderDto.setShipping(order.getShippingTotal());
		orderDto.setTotal(order.getGrandTotal());
		orderDto.setDiscount(order.getDiscounts());
		orderDto.setIsCancelable(isCancelable);
		return orderDto;
	}


	private List<SubOrder> createSubOrderDtoList(MetaOrderEntity order) {
		return order
		.getSubOrders()
		.stream()
		.map(subOrder -> getSubOrder(subOrder))
		.collect(toList());
	}

	private List<SubOrder> createSubOrderDtoListForYeshteryMetaorder(MetaOrderEntity order) {
		return order
				.getSubMetaOrders()
				.stream()
				.map(MetaOrderEntity::getSubOrders)
				.flatMap(Set::stream)
				.map(subOrder -> getSubOrder(subOrder))
				.collect(toList());
	}



	private Order setMetaOrderBasicData(MetaOrderEntity metaOrder, boolean yeshteryMetaorder) {
		Order order = new Order();
		order.setUserId(metaOrder.getUser().getId());
		order.setUserName(metaOrder.getUser().getName());
		order.setOrderId(metaOrder.getId());
		order.setCurrency(getOrderCurrency(metaOrder));
		order.setCreationDate(metaOrder.getCreatedAt());
		order.setNotes(metaOrder.getNotes());

		String status = ofNullable(findEnum(metaOrder.getStatus()))
						.orElse(NEW)
						.name();
		order.setStatus(status);
		Optional<PaymentEntity> payment = Optional.empty();
		if (yeshteryMetaorder) {
			payment = paymentsRepo.findFirstByMetaOrderId(metaOrder.getId());
		} else {
			payment = paymentsRepo.findByMetaOrderId(metaOrder.getId());
		}
		if (payment.isPresent()) {
			PaymentStatus paymentStatus =
					payment
					.map(PaymentEntity::getStatus)
					.orElse(UNPAID);
			order.setOperator(payment.get().getOperator());
			order.setPaymentStatus(paymentStatus.name());
		}
		return order;
	}

	@Override
	public Order getYeshteryMetaOrder(Long orderId, boolean yeshteryMetaorder){
		BaseUserEntity user = securityService.getCurrentUser();
		Long orgId = securityService.getCurrentUserOrganizationId();
		boolean isNasnavAdmin = securityService.currentUserHasRole(NASNAV_ADMIN);

		Optional<MetaOrderEntity> order = empty();

		if (user instanceof UserEntity) {
			order = metaOrderRepo.findYeshteryMetaorderByIdAndUserIdAndOrganization_Id(orderId, user.getId(), orgId);
		} else if (isNasnavAdmin) {
			order = metaOrderRepo.findYeshteryMetaorderByMetaOrderId(orderId);
		} else {
			order = metaOrderRepo.findYeshteryMetaorderByIdAndOrganization_Id(orderId, orgId);
		}

		if (order.isPresent()) {
			return getOrderResponse(order.get(), yeshteryMetaorder);
		}
		throw new RuntimeBusinessException(NOT_FOUND, O$0001, orderId);
	}

	@Override
	public Order getMetaOrder(Long orderId, boolean yeshteryMetaorder){
		BaseUserEntity user = securityService.getCurrentUser();
		Long orgId = securityService.getCurrentUserOrganizationId();
		boolean isNasnavAdmin = securityService.currentUserHasRole(NASNAV_ADMIN);

		Optional<MetaOrderEntity> order = empty();;

		if (user instanceof UserEntity) {
			order = metaOrderRepo.findByIdAndUserIdAndOrganization_Id(orderId, user.getId(), orgId);
		} else if (isNasnavAdmin) {
			order = metaOrderRepo.findByMetaOrderId(orderId);
		} else {
			order = metaOrderRepo.findByIdAndOrganization_Id(orderId, orgId);
		}

		if (order.isPresent()) {
			return getOrderResponse(order.get(), yeshteryMetaorder);
		}
		throw new RuntimeBusinessException(NOT_FOUND, O$0001, orderId);
	}


	@Override
	public List<MetaOrderBasicInfo> getMetaOrderList() {
		BaseUserEntity user = securityService.getCurrentUser();
		return metaOrderRepo.getMetaOrderList(user.getId(), user.getOrganizationId())
							.stream()
							.map(this::setOrderStatus)
							.map(this::setPaymentStatus)
							.collect(toList());
	}


	private MetaOrderBasicInfo setOrderStatus(MetaOrderBasicInfo order) {
		order.setStatus(OrderStatus.findEnum(order.getStatusInt()).name());
		order.setStatusInt(null);
		return order;
	}



	private MetaOrderBasicInfo setPaymentStatus(MetaOrderBasicInfo order) {
		String payStatus =
				ofNullable(order.getPaymentStatusInt())
				.map(PaymentStatus::getPaymentStatus)
				.map(PaymentStatus::name)
				.orElse(UNPAID.name());
		order.setPaymentStatus(payStatus);
		order.setPaymentStatusInt(null);
		return order;
	}


	private TransactionCurrency getOrderCurrency(MetaOrderEntity order) {
		return order
				.getSubOrders()
				.stream()
				.findFirst()
				.map(OrdersEntity::getBasketsEntity)
				.orElse(new HashSet<BasketsEntity>())
				.stream()
				.findFirst()
				.map(BasketsEntity::getCurrency)
				.map(TransactionCurrency::getTransactionCurrency)
				.orElse(EGP);
	}


	private SubOrder getSubOrder(OrdersEntity order) {
		String status = ofNullable(findEnum(order.getStatus()))
				.orElse(NEW)
				.name();

		List<BasketItem> items = getBasketItemsDetailsMap( setOf(order) ).get(order.getId());

		Shipment shipmentDto = (Shipment) order.getShipment().getRepresentation();
		String shippingServiceName = shippingMgrService
				.getShippingServiceInfo(shipmentDto.getServiceId())
				.map(ShippingServiceInfo::getName)
				.orElse("");
		shipmentDto.setServiceName(shippingServiceName);

		Long totalQuantity =
				items
				.stream()
				.map(BasketItem::getQuantity)
				.reduce(0, Integer::sum)
				.longValue();

		ShopsEntity shop = order.getShopsEntity();
		SubOrder subOrder = new SubOrder();
		subOrder.setShopId(shop.getId());
		subOrder.setShopName(shop.getName());
		subOrder.setShopLogo(shop.getLogo());
		subOrder.setSubOrderId(order.getId());
		subOrder.setCreationDate(order.getCreationDate());
		subOrder.setStatus(status);
		subOrder.setDeliveryAddress((AddressRepObj)order.getAddressEntity().getRepresentation());
		subOrder.setItems(items);
		subOrder.setShipment(shipmentDto);
		subOrder.setTotalQuantity(totalQuantity);
		subOrder.setTotal(order.getTotal());
		subOrder.setSubtotal(order.getAmount());
		subOrder.setDiscount(order.getDiscounts());

		subOrder.setPoints(getOrderPoints(order));
		if(!isNullOrEmpty(subOrder.getPoints())) {
			BigDecimal totalPointAmount = subOrder.getPoints().stream().map(point -> ofNullable(point.getAmount()).orElse(ZERO))
					.reduce(ZERO, BigDecimal::add);
			subOrder.setTotalPointAmount(totalPointAmount);
		}
		return subOrder;
	}




	private MetaOrderEntity createOrder(Map<Long, List<CartCheckoutData>> shopCartsMap, AddressesEntity address, CartCheckoutDTO dto, OrganizationEntity org, UserEntity user) {
		return createOrder(shopCartsMap, address, dto, org.getId(), user);
	}

	private MetaOrderEntity createOrder(Map<Long, List<CartCheckoutData>> shopCartsMap, AddressesEntity address, CartCheckoutDTO dto, Long orgId, UserEntity user) {

		OrganizationEntity org = organizationRepository.findById(orgId).get();
		Optional<PromotionsEntity> promotion =
				ofNullable(dto.getPromoCode())
						.flatMap(promoCode ->
								promoRepo
										.findByCodeAndOrganization_IdAndActiveNow(promoCode, org.getId()));

		List<CartItemsForShop> cartDividedByShop = groupCartItemsByShop(shopCartsMap, org);
		Set<OrdersEntity> subOrders = createSubOrders(cartDividedByShop, address, dto, org);

		BigDecimal subTotal = calculateSubTotal(subOrders);
		BigDecimal shippingFeeTotal = calculateShippingTotal(subOrders);
		BigDecimal total = calculateTotal(subOrders);
		BigDecimal discounts = calculateDiscounts(subOrders);


		MetaOrderEntity order = new MetaOrderEntity();
		order.setOrganization(org);
		order.setUser(user);
		order.setStatus(CLIENT_CONFIRMED.getValue());
		order.setGrandTotal(total);
		order.setSubTotal(subTotal);
		order.setShippingTotal(shippingFeeTotal);
		order.setDiscounts(discounts);
		order.setNotes(dto.getNotes());
		subOrders.forEach(order::addSubOrder);
		promotion.ifPresent(order::addPromotion);

		return metaOrderRepo.save(order);
	}


	private BigDecimal calculateDiscounts(Set<OrdersEntity> subOrders) {
		return subOrders
				.stream()
				.map(OrdersEntity::getDiscounts)
				.map(discount -> ofNullable(discount).orElse(ZERO))
				.reduce(ZERO, BigDecimal::add);
	}


	private BigDecimal calculateShippingTotal(Set<OrdersEntity> subOrders) {
		return subOrders
				.stream()
				.map(OrdersEntity::getShipment)
				.map(ShipmentEntity::getShippingFee)
				.reduce(ZERO, BigDecimal::add);
	}


	private BigDecimal calculateSubTotal(Set<OrdersEntity> subOrders) {
		return subOrders
				.stream()
				.map(OrdersEntity::getAmount)
				.reduce(ZERO, BigDecimal::add);
	}


	private BigDecimal calculateTotal(Set<OrdersEntity> subOrders) {
		return subOrders
				.stream()
				.map(OrdersEntity::getTotal)
				.reduce(ZERO, BigDecimal::add);
	}


	private List<CartItemsForShop> groupCartItemsByShop(Map<Long, List<CartCheckoutData>> shopCartsMap, OrganizationEntity org) {
		Map<Long,ShopsEntity> shopCache = createOrganizationShopsCache(org.getId());
		return shopCartsMap
				.entrySet()
				.stream()
				.map(entry -> getCartItemsForShop(entry, shopCache, org))
				.collect(toList());
	}


	private CartItemsForShop getCartItemsForShop(Map.Entry<Long, List<CartCheckoutData>> entry, Map<Long,ShopsEntity> shopCache,
												 OrganizationEntity org) {
		Long orgId = org.getId();
		ShopsEntity shop =
				ofNullable(entry.getKey())
				.map(shopCache::get)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, S$0005 , entry.getKey(), orgId));
		return new CartItemsForShop(shop, entry.getValue());
	}


	private Map<Long, ShopsEntity> createOrganizationShopsCache(Long orgId) {
		return shopsRepo
		.findByOrganizationEntity_IdAndRemovedOrderByPriorityDesc(orgId, 0)
		.stream()
		.collect(toMap(ShopsEntity::getId, shop -> shop));
	}


	private Set<OrdersEntity> createSubOrders(List<CartItemsForShop> cartDividedByShop, AddressesEntity address,
			CartCheckoutDTO dto, OrganizationEntity org) {
		Set<OrdersEntity> subOrders =
				cartDividedByShop
				.stream()
				.map(cartItems -> createSubOrder(cartItems, address, dto, org))
				.collect(toSet());

		List<ShippingOfferDTO> shippingOffers =	getShippingOffersForCheckout(dto, subOrders, org.getId());

		addPromoDiscounts(dto, subOrders, org.getId());

		for(OrdersEntity subOrder : subOrders) {
			subOrder.setShipment(createShipment(subOrder, dto, shippingOffers));
			subOrder.setTotal(calculateTotal(subOrder));
		}
		return subOrders;
	}

	private Set<OrdersEntity> createYeshterySubOrders(List<CartItemsForShop> cartDividedByShop, AddressesEntity address,
											  CartCheckoutDTO dto, OrganizationEntity org) {
		Set<OrdersEntity> subOrders =
				cartDividedByShop
						.stream()
						.map(cartItems -> createSubOrder(cartItems, address, dto, org))
						.collect(toSet());
		Long yeshteryOrgId = getYeshteryOrgId();
		List<ShippingOfferDTO> shippingOffers =	getShippingOffersForCheckout(dto, subOrders, yeshteryOrgId);

		addPromoDiscounts(dto, subOrders, org.getId());

		for(OrdersEntity subOrder : subOrders) {
			subOrder.setShipment(createShipment(subOrder, dto, shippingOffers));
			subOrder.setTotal(calculateTotal(subOrder));
		}
		return subOrders;
	}

	private Long getYeshteryOrgId() {
		return ofNullable(organizationRepository.findByPname(YESHTERY_PNAME))
				.map(OrganizationEntity::getId)
				.orElseThrow();
	}

	private List<ShippingOfferDTO> getShippingOffersForCheckout(CartCheckoutDTO dto, Set<OrdersEntity> subOrders, Long orgId) {
		return subOrders
				.stream()
				.map(subOrder -> shippingMgrService.createShippingDetailsFromOrder(subOrder, dto.getAdditionalData()))
				.collect(collectingAndThen(toList(), list -> shippingMgrService.getOffersFromOrganizationShippingServices(list, orgId)));
	}


	private void addPromoDiscounts(CartCheckoutDTO dto, Set<OrdersEntity> subOrders, Long orgId) {
		OrdersEntity suborder = subOrders.stream().findFirst().get();
		Long userId = suborder.getUserId();
		BigDecimal subTotal =
				subOrders
				.stream()
				.map(OrdersEntity::getAmount)
				.reduce(ZERO, BigDecimal::add);

		var promoItems = getPromoItems(subOrders);
		var promoDiscount = promoService.calculateAllApplicablePromos(promoItems, subTotal, dto.getPromoCode(), orgId).getTotalDiscount();

		if(promoDiscount.compareTo(ZERO) == 0) {
			return;
		}

		BigDecimal calculatedPromotionDiscount =
				addPromoDiscountAndGetItsCalculatedTotal(promoDiscount, subOrders);

		BigDecimal calculationError = promoDiscount.subtract(calculatedPromotionDiscount);

		subOrders
		.stream()
		.findFirst()
		.ifPresent(subOrder -> addToSubOrderDiscounts(subOrder, calculationError));
	}



	private List<PromoItemDto> getPromoItems(Set<OrdersEntity> subOrders) {
		return subOrders
				.stream()
				.map(OrdersEntity::getBasketsEntity)
				.flatMap(Set::stream)
				.map(this::toPromoItemDto)
				.collect(toUnmodifiableList());
	}



	private PromoItemDto toPromoItemDto(BasketsEntity orderItem) {
		var promoItem = new PromoItemDto();
		promoItem.setPrice(orderItem.getPrice());
		promoItem.setDiscount(orderItem.getDiscount());
		promoItem.setItemData(orderItem.getItemData());

		var stock = ofNullable(orderItem).map(BasketsEntity::getStocksEntity);
		var variant = stock.map(StocksEntity::getProductVariantsEntity);
		var product = variant.map(ProductVariantsEntity::getProductEntity);

		ofNullable(orderItem.getQuantity()).map(BigDecimal::intValue).ifPresent(promoItem::setQuantity);
		stock.map(StocksEntity::getId).ifPresent(promoItem::setStockId);
		variant.map(ProductVariantsEntity::getId).ifPresent(promoItem::setVariantId);
		product.map(ProductEntity::getId).ifPresent(promoItem::setProductId);
		product.map(ProductEntity::getBrand).ifPresent(brand -> promoItem.setBrandId(brand.getId()));
		product.map(ProductEntity::getProductType).ifPresent(promoItem::setProductType);
		variant.map(ProductVariantsEntity::getWeight).ifPresent(promoItem::setWeight);
		stock.map(StocksEntity::getUnit).map(StockUnitEntity::getName).ifPresent(promoItem::setUnit);

		return promoItem;
	}


	private BigDecimal  addPromoDiscountAndGetItsCalculatedTotal(BigDecimal promoDiscount, Set<OrdersEntity> subOrders) {
		BigDecimal subTotal =
				subOrders
				.stream()
				.map(OrdersEntity::getAmount)
				.reduce(ZERO, BigDecimal::add);

		return subOrders
				.stream()
				.map(subOrder -> addPromoDiscount(promoDiscount, subOrder, subTotal))
				.reduce(ZERO, BigDecimal::add);
	}


	private BigDecimal addPromoDiscount(BigDecimal promoDiscount, OrdersEntity subOrder
				, BigDecimal subTotal) {
		BigDecimal proportion = subOrder.getAmount().divide(subTotal, 2, FLOOR);
		BigDecimal subOrderPromoDiscount = proportion.multiply(promoDiscount).setScale(2, FLOOR);
		addToSubOrderDiscounts(subOrder, subOrderPromoDiscount);

		return subOrderPromoDiscount;
	}


	private void addToSubOrderDiscounts(OrdersEntity subOrder, BigDecimal discount) {
		BigDecimal subOrderTotalDiscount = subOrder.getDiscounts().add(discount);
		subOrder.setDiscounts(subOrderTotalDiscount);
	}

	private BigDecimal calculateTotal(OrdersEntity subOrder) {
		BigDecimal shippingFee =
				ofNullable(subOrder.getShipment())
				.map(ShipmentEntity::getShippingFee)
				.orElse(ZERO);
		BigDecimal subTotal = ofNullable(subOrder.getAmount()).orElse(ZERO);
		BigDecimal discount = ofNullable(subOrder.getDiscounts()).orElse(ZERO);
		return subTotal.add(shippingFee).subtract(discount);
	}

	private OrdersEntity createSubOrder(CartItemsForShop cartItems, AddressesEntity shippingAddress, CartCheckoutDTO dto,
										OrganizationEntity org) {
		Long orgId = org.getId();
		Map<Long, StocksEntity> stocksCache = createStockCache(cartItems, orgId);

		OrdersEntity subOrder =  createSubOrder(shippingAddress, cartItems, org);
		saveOrderItemsIntoSubOrder(cartItems, stocksCache, subOrder);
		subOrder.setAmount(calculateSubTotal(subOrder));
		return ordersRepository.save(subOrder);
	}


	private void saveOrderItemsIntoSubOrder(CartItemsForShop cartItems, Map<Long, StocksEntity> stocksCache,
			OrdersEntity subOrder) {
		cartItems
		.getCheckOutData()
		.stream()
		.map(data -> createBasketItemEntity(data, subOrder, stocksCache))
		.forEach(subOrder::addBasketItem);
	}


	private BigDecimal calculateSubTotal(OrdersEntity subOrder) {
		BigDecimal value =  subOrder
				.getBasketsEntity()
				.stream()
				.map(this::calcBasketItemValue)
				.reduce(ZERO, BigDecimal::add);
		if (value.precision() > 10) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0017);
		}
		return value;
	}


	private BigDecimal calcBasketItemValue(BasketsEntity item) {
		return item.getPrice().multiply(item.getQuantity());
	}


	private Map<Long, StocksEntity> createStockCache(CartItemsForShop cartItems, Long orgId) {
		Set<Long> itemStocks =
				cartItems
				.getCheckOutData()
				.stream()
				.map(CartCheckoutData::getStockId)
				.collect(toSet());

		Map<Long, StocksEntity> stocksCache =
				stockRepository
				.findByIdInAndOrganizationEntity_Id(itemStocks, orgId)
				.stream()
				.collect(groupingBy(StocksEntity::getId))
				.entrySet()
				.stream()
				.map(this::createStockEntityWithIdEntry)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		return stocksCache;
	}


	private Map.Entry<Long, StocksEntity> createStockEntityWithIdEntry(Map.Entry<Long, List<StocksEntity>> entry){
		Long stockId = entry.getKey();
		StocksEntity stock =
				entry
				.getValue()
				.stream()
				.findFirst()
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, P$STO$0001, stockId));
		return new SimpleEntry<Long, StocksEntity>(stockId, stock);
	}



	private BasketsEntity createBasketItemEntity(CartCheckoutData data, OrdersEntity subOrder, Map<Long, StocksEntity> stocksCache) {
		StocksEntity stock =
				ofNullable(data)
				.map(CartCheckoutData::getStockId)
				.map(stocksCache::get)
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, P$STO$0001, data.getStockId()));

		if(stock.getQuantity() < data.getQuantity()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0003);
		}
		if (new BigDecimal(data.getQuantity()).precision() > 10) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0016);
		}
		BigDecimal discount = ofNullable(data.getDiscount()).orElse(ZERO);
		BigDecimal totalPrice = data.getPrice().subtract(discount);

		if (totalPrice.precision() > 10) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0017);
		}
		BasketsEntity basket = new BasketsEntity();
		basket.setStocksEntity(stock);
		basket.setPrice(totalPrice);
		basket.setQuantity(new BigDecimal(data.getQuantity()));
		basket.setCurrency(data.getCurrency());
		basket.setDiscount(data.getDiscount());
		basket.setOrdersEntity(subOrder);
		basket.setItemData(serializeItemData(basket));
		return basket;
	}



	private String serializeItemData(BasketsEntity basket) {
		try {
			return objectMapper.writeValueAsString(createBasketItemEntity(basket));
		} catch (Throwable e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, O$NEW$0001);
		}
	}


	private OrdersEntity createSubOrder(AddressesEntity shippingAddress,
										CartItemsForShop cartItems, OrganizationEntity org) {
		UserEntity user = (UserEntity) securityService.getCurrentUser();

		OrdersEntity subOrder = new OrdersEntity();
		subOrder.setName(user.getName());
		subOrder.setUserId(user.getId());
		subOrder.setShopsEntity(cartItems.getShop());
		subOrder.setOrganizationEntity(org);
		subOrder.setAddressEntity(shippingAddress);
		subOrder.setStatus(CLIENT_CONFIRMED.getValue());
		subOrder.setDiscounts(ZERO);
		return subOrder;
	}

	private ShipmentEntity createShipment(OrdersEntity subOrder, CartCheckoutDTO dto, List<ShippingOfferDTO> shippingOffers) {
		ShipmentEntity shipment = new ShipmentEntity();
		shipment.setSubOrder(subOrder);
		shipment.setStatus(DRAFT.getValue());
		shipment.setShippingServiceId(dto.getServiceId());
		if ( dto.getAdditionalData() == null || dto.getAdditionalData().isEmpty()) {
			shipment.setParameters("{}");
		} else {
			JSONObject additionalData = new JSONObject(dto.getAdditionalData());
			shipment.setParameters(additionalData.toString());
		}
		ShipmentDTO shipmentDTO =
				shippingOffers
				.stream()
				.filter(offer -> Objects.equals(offer.getServiceId(), dto.getServiceId()))
				.findFirst()
				.map(ShippingOfferDTO::getShipments)
				.orElse(emptyList())
				.stream()
				.filter(shp -> Objects.equals(shp.getSubOrderId(), subOrder.getId()))
				.findFirst()
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$SHP$0003));

		shipment.setShippingFee(shipmentDTO.getShippingFee());
		shipment.setFrom( toLocalDateTime(shipmentDTO.getEta().getFrom()));
		shipment.setTo( toLocalDateTime(shipmentDTO.getEta().getTo()));

		return shipmentRepo.save(shipment);
	}


	private void validateCartCheckoutItems(List<CartCheckoutData> userCartItems, OrganizationEntity org) {
 		if (userCartItems.isEmpty()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CHK$0001);
		}
		Integer currency = (userCartItems.stream().findFirst().orElse(new CartCheckoutData())).getCurrency();
		for(CartCheckoutData item : userCartItems) {
			if (item.getQuantity() == null || item.getQuantity() <= 0) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0002);
			}

			if (!Objects.equals(item.getCurrency(), currency)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0004);
			}
			if (!Objects.equals(item.getOrganizationId(), org.getId())) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0005);
			}
		}
	}

	public OrderValue getMetaOrderTotalValue(long metaOrderId) {
		ArrayList<MetaOrderEntity> metaOrders = new ArrayList<>();
		Optional<MetaOrderEntity> metaOrder = metaOrderRepo.findById(metaOrderId);
		if (metaOrder.isEmpty()) {
			return null;
		}
		metaOrders.add(metaOrder.get());
		Set<MetaOrderEntity> subMetas = metaOrder.get().getSubMetaOrders();
		if (subMetas != null && !subMetas.isEmpty()) {
			metaOrders.addAll(subMetas);
		}
		OrderService.OrderValue oValue = new OrderService.OrderValue();
		oValue.amount = new BigDecimal(0);
		oValue.currency = null;

		for(MetaOrderEntity moe: metaOrders) {
			oValue.amount = oValue.amount.add(moe.getGrandTotal());
			if (oValue.currency == null) {
				oValue.currency = getOrderCurrency(moe);
			} else {
				if (oValue.currency != getOrderCurrency(moe)) {
					logger.error("Mismatched order currencies for meta order: ({})", metaOrderId);
					return null;
				}
			}
		}
		return oValue;
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void rejectOrder(OrderRejectDTO dto) {
		validateOrderRejectRequest(dto);
		EmployeeUserEntity storeMgr = getAndValidateUser();
		OrdersEntity subOrder = getAndValidateOrderForRejection(dto.getSubOrderId(), storeMgr);

		rejectSubOrderAndMetaOrder(subOrder);

		sendRejectionEmailToCustomer(subOrder, dto.getRejectionReason());
	}

	private void sendRejectionEmailToCustomer(OrdersEntity subOrder, String rejectionReason) {
		String orgName = subOrder.getOrganizationEntity().getName();
		String to = subOrder.getMetaOrder().getUser().getEmail();
		String subject = format(ORDER_REJECT_SUBJECT, orgName);
		List<String> bcc = getOrganizationManagersEmails(subOrder);
		Map<String,Object> parametersMap = createRejectionEmailParams(subOrder, rejectionReason);
		String template = ORDER_REJECT_TEMPLATE;
		try {
			mailService.sendThymeleafTemplateMail(orgName, asList(to), subject, emptyList(), bcc, template, parametersMap);
		} catch (IOException | MessagingException e) {
			logger.error(e, e);
		}
	}



	private void validateOrderRejectRequest(OrderRejectDTO dto) {
		if(anyIsNull(dto, dto.getSubOrderId())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RJCT$0001);
		}
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void cancelOrder(Long metaOrderId) {
		MetaOrderEntity order =
				metaOrderRepo
				.findFullDataById(metaOrderId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$GNRL$0002, metaOrderId));
		validateOrderForCancellation(order);

		cancelMetaOrderAndSubOrders(order);
		try {
			order.getSubOrders().forEach(this::sendOrderCancellationNotificationEmailToStoreManager);
		}catch(Throwable t) {
			logger.error(t,t);
		}
	}


	private void sendOrderCancellationNotificationEmailToStoreManager(OrdersEntity order) {
		Long orderId = order.getId();

		String orgName = securityService.getCurrentUserOrganization().getName();
		List<String> to = getStoreManagersEmails(order);
		String subject = format("Order[%d] was Cancelled!", orderId);
		List<String> cc = getOrganizationManagersEmails(order);
		Map<String,Object> parametersMap = createCancellationNotificationEmailParams(order);
		String template = ORDER_CANCEL_NOTIFICATION_TEMPLATE;
		try {
			if(to.isEmpty()) {
				to = cc;
				cc = emptyList();
			}
			mailService.sendThymeleafTemplateMail(orgName, to, subject, cc, template, parametersMap);
		} catch (IOException | MessagingException e) {
			logger.error(e, e);
		}
	}



	private Map<String, Object> createCancellationNotificationEmailParams(OrdersEntity order) {
		Map<String,Object> params = new HashMap<>();
		String updateTime =
				DateTimeFormatter
				.ofPattern("dd/MM/YYYY - hh:mm")
				.format(order.getUpdateDate());

		SubOrder subOrder = getSubOrder(order);
		changeShippingServiceName(subOrder);

		String domain = domainService.getCurrentServerDomain();
		String orgLogo = domain + "/files/" + orderEmailHelper.getOrganizationLogo(order.getOrganizationEntity());

		String orderPageUrl =
				domainService
				.buildDashboardOrderPageUrl(order.getId(), order.getOrganizationEntity().getId());

		params.put("domain", domain);
		params.put("orgLogo", orgLogo);
		params.put("updateTime", updateTime);
		params.put("orderPageUrl", orderPageUrl);
		params.put("sub", subOrder);
		return params;
	}




	private void cancelMetaOrderAndSubOrders(MetaOrderEntity order) {
		if( Objects.equals(order.getStatus(), FINALIZED.getValue())){
			order.getSubOrders().forEach(this::returnOrderToStocks);
		}
		updateOrderStatus(order, CLIENT_CANCELLED);
		order.getSubOrders().forEach(sub -> updateOrderStatus(sub, CLIENT_CANCELLED));
	}




	private void validateOrderForCancellation(MetaOrderEntity order) {
		Long currentUser = securityService.getCurrentUser().getId();
		if(!Objects.equals(order.getUser().getId(), currentUser)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$GNRL$0003, order.getId());
		}
		OrderStatus status = getCancelOrderStatus(order);
		if(!isCancelable(order)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CNCL$0002, order.getId(), status.toString());
		}
	}


	private Boolean isCancelable(MetaOrderEntity order) {
		List<OrderStatus> acceptedStatuses = asList(CLIENT_CONFIRMED, FINALIZED);
		OrderStatus status = getCancelOrderStatus(order);
		boolean areAllSubOrdersFinalized =
				order
						.getSubOrders()
						.stream()
						.map(OrdersEntity::getStatus)
						.map(OrderStatus::findEnum)
						.allMatch(acceptedStatuses::contains);
		return acceptedStatuses.contains(status) && areAllSubOrdersFinalized;
	}




	private OrderStatus getCancelOrderStatus(MetaOrderEntity order) {
		return ofNullable(order.getStatus())
						.map(OrderStatus::findEnum)
						.orElse(CLIENT_CONFIRMED);
	}
}


@Data
@AllArgsConstructor
class CartItemsForShop{
	private ShopsEntity shop;
	private List<CartCheckoutData> checkOutData;
}



class CartItemsGroupedById extends HashMap<Long, List<CartCheckoutData>>{

	private static final long serialVersionUID = 166855415L;

	public CartItemsGroupedById(Map<Long, List<CartCheckoutData>> map) {
		super(map);
	}
}


class CartItemsGroupedByOrgId extends HashMap<Long, CartItemsGroupedById>{

	private static final long serialVersionUID = 166855415L;

	public CartItemsGroupedByOrgId(Map<Long, CartItemsGroupedById> map) {
		super(map);
	}
}



@Data
@AllArgsConstructor
class SubOrderAndSubTotalPair{
	private Long id;
	private BigDecimal resultPlusReminder;
}



@Data
@AllArgsConstructor
class ReturnRequestBasketItem{
	private Long orderItemId;
	private Integer returnedQuantity;
	private Integer receivedQuantity;
}


@Data
class ReturnShipment{
	private String trackNumber;
	private List<ReturnShipmentItem> items;
}


@Data
class ReturnShipmentItem{
	private String thumb;
	private BigDecimal price;
	private Map<String, String> variantFeatures;
	private String name;
	private String currency;
	private Integer quantity;
	private Integer receivedQuantity;
	private String sku;
	private String productCode;
}

@Data
@AllArgsConstructor
class CartItemsForOrg{
	private OrganizationEntity organization;
	private List<CartItemsForShop> checkOutData;
}
