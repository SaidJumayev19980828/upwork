package com.nasnav.service;

import com.nasnav.AppConfig;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.request.OrderRejectDTO;
import com.nasnav.dto.request.ReturnRequestRejectDTO;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.order.returned.ReceivedBasketItem;
import com.nasnav.dto.request.order.returned.ReceivedItem;
import com.nasnav.dto.request.order.returned.ReceivedItemsDTO;
import com.nasnav.dto.request.order.returned.ReturnRequestItemsDTO;
import com.nasnav.dto.request.shipping.ShipmentDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.dto.response.OrderConfrimResponseDTO;
import com.nasnav.dto.response.ReturnRequestDTO;
import com.nasnav.dto.response.ReturnRequestItemDTO;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.dto.response.navbox.*;
import com.nasnav.enumerations.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.exceptions.StockValidationException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.exceptions.InvalidIntegrationEventException;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.*;
import com.nasnav.request.OrderSearchParam;
import com.nasnav.response.OrderResponse;
import com.nasnav.response.ReturnRequestsResponse;
import com.nasnav.service.helpers.EmployeeUserServiceHelper;
import com.nasnav.service.model.cart.ShopFulfillingCart;
import com.nasnav.service.model.mail.MailAttachment;
import com.nasnav.shipping.model.ReturnShipmentTracker;
import com.nasnav.shipping.model.ShipmentTracker;
import com.nasnav.shipping.model.ShippingDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.stream.Stream;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.commons.utils.EntityUtils.*;
import static com.nasnav.commons.utils.MapBuilder.buildMap;
import static com.nasnav.commons.utils.MathUtils.calculatePercentage;
import static com.nasnav.constatnts.EmailConstants.*;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.*;
import static com.nasnav.enumerations.OrderFailedStatus.INVALID_ORDER;
import static com.nasnav.enumerations.OrderStatus.NEW;
import static com.nasnav.enumerations.OrderStatus.findEnum;
import static com.nasnav.enumerations.OrderStatus.*;
import static com.nasnav.enumerations.PaymentStatus.*;
import static com.nasnav.enumerations.ReturnRequestStatus.*;
import static com.nasnav.enumerations.Roles.*;
import static com.nasnav.enumerations.ShippingStatus.DRAFT;
import static com.nasnav.enumerations.ShippingStatus.REQUSTED;
import static com.nasnav.enumerations.TransactionCurrency.*;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.range;
import static javax.persistence.criteria.JoinType.INNER;
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

	private static final Long NON_EXISTING_ORDER_ID = -1L;
	public static final int MAX_RETURN_TIME_WINDOW = 14;

	private static final Set<OrderStatus> metaOrderAcceptableStatusForReturn =
		setOf(DELIVERED,STORE_CONFIRMED,STORE_PREPARED,DISPATCHED,FINALIZED);

	private final OrdersRepository ordersRepository;

	private final BasketRepository basketRepository;

	private final StockRepository stockRepository;

	private final StockService stockService;

	private final EmployeeUserServiceHelper employeeUserServiceHelper;
	
	private Logger logger = LogManager.getLogger();
	
	@Autowired
	private EntityManager em;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private ShopsRepository shopsRepo;

	@Autowired
	private AddressRepository addressRepo;

	@Autowired
	private IntegrationService integrationService;
	
	@Autowired
	private CartItemRepository cartItemRepo;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private MetaOrderRepository metaOrderRepo;
	
	@Autowired
	private ShippingManagementService shippingMgrService;
	
	@Autowired
	private ShipmentRepository shipmentRepo;

	@Autowired
	private PaymentsRepository paymentsRepo;
	
	@Autowired
	private ProductImageService imgService;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private DomainService domainService;
	
	@Autowired
	private AppConfig appConfig;
	
	@Autowired
	private RoleEmployeeUserRepository empRoleRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private OrganizationImagesRepository orgImagesRepo;
	
	@Autowired
	private PromotionsService promoService;
	
	@Autowired
	private PromotionRepository promoRepo;
	@Autowired
	private CartOptimizationService cartOptimizationService;
	
	@Autowired
	private ReturnRequestRepository returnRequestRepo;

	@Autowired
	private ReturnRequestItemRepository returnRequestItemRepo;
	
	private Map<OrderStatus, Set<OrderStatus>> orderStateMachine;
	private Map<ReturnRequestStatus, Set<ReturnRequestStatus>> orderReturnStateMachine;
	private Set<OrderStatus> orderStatusForCustomers;
	private Set<OrderStatus> orderStatusForManagers;
	

	@Autowired
	public OrderServiceImpl(OrdersRepository ordersRepository, BasketRepository basketRepository,
							StockRepository stockRepository ,StockService stockService, UserRepository userRepository,
	                        EmployeeUserServiceHelper employeeUserServiceHelper, EmployeeUserRepository employeeUserRepository,
							ProductRepository productRepository) {
		this.ordersRepository = ordersRepository;
		this.stockRepository = stockRepository;
		this.basketRepository = basketRepository;
		this.stockService = stockService;
		this.employeeUserServiceHelper = employeeUserServiceHelper;
		setOrderStatusPermissions();
		
		buildOrderStatusTransitionMap();
		buildOrderReturnStatusTransitionMap();
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


	private void buildOrderReturnStatusTransitionMap() {
		orderReturnStateMachine = new HashMap<>();
		buildMap(orderReturnStateMachine)
				.put(ReturnRequestStatus.NEW	, setOf(CONFIRMED, REJECTED))
				.put(CONFIRMED					, setOf(RECEIVED, REJECTED));
	}
	

	public OrderValue getOrderValue(OrdersEntity order) {
		TransactionCurrency currency = null;
		BigDecimal amount = new BigDecimal(0);

		List<BasketsEntity> basketsEntity = basketRepository.findByOrdersEntity_Id(order.getId());
		for (BasketsEntity basketEntity : basketsEntity) {
			// basket item holds a sum for all items
			amount = amount.add(basketEntity.getPrice());
			if (currency == null) {
				currency = basketEntity.getStocksEntity().getCurrency();
			} else if (currency != basketEntity.getStocksEntity().getCurrency()) {
				currency = UNSPECIFIED;
			}
		}
		OrderValue value = new OrderValue();
		value.amount = amount;
		value.currency = currency;
		return value;
	}
	
	
	public OrderResponse createNewOrder(OrderJsonDto orderJson) throws BusinessException {

		validateOrderCreation(orderJson);

		Map<Long, List<BasketItemDTO>> groupedBaskets = groupBasketsByShops(orderJson.getBasket());

		List<OrderRepresentationObject> ordersList = new ArrayList<>();
		for(Long shopId : groupedBaskets.keySet())
			ordersList.add(createNewOrderAndBasketItems(orderJson, groupedBaskets.get(shopId), shopId));
		return new OrderResponse(ordersList, (ordersList.stream()
													   .map(order -> order.getPrice())
													   .reduce(ZERO, BigDecimal::add)));
	}
	
	
	
	
	
	@Transactional(rollbackFor = Throwable.class)
	public OrderResponse updateExistingOrder(OrderJsonDto orderJson) throws BusinessException {

		validateOrderUpdate(orderJson);

		return updateOrder(orderJson);
	}

	
	

	private void validateOrderUpdate(OrderJsonDto order) throws BusinessException {

		List<String> possibleStatusList = getPossibleOrderStatus();
		if(!possibleStatusList.contains( order.getStatus() ))
			throwInvalidOrderException(ERR_INVALID_ORDER_STATUS);

		if( !isNewOrder(order) && isNullOrZero(order.getId()) )
			throwInvalidOrderException(ERR_UPDATED_ORDER_WITH_NO_ID);

		if(isNewOrder(order)){
			if (order.getBasket() != null)
				validateBasketItems(order);
		}

		if(isCustomerUser()) {
			validateOrderUpdateForUser(order);
		}else {
			validateOrderUpdateForManager(order.getId());
		}
	}





	private void validateOrderUpdateForUser(OrderJsonDto order)
			throws BusinessException {
		Long userId = securityService.getCurrentUser().getId();
		Long orderId = order.getId();
		OrdersEntity orderEntity = ordersRepository.findById(orderId)
											.orElseThrow(() -> getInvalidOrderException(ERR_ORDER_NOT_EXISTS, orderId));		
		
		if( !Objects.equals(userId, orderEntity.getUserId()) ) {
			throwInvalidOrderException(ERR_ORDER_NOT_OWNED_BY_USER, userId, orderId);
		}
		
		if( isCheckedOutOrder(order)) {
			validateConfirmedOrder(orderId);
		}
	}





	private void validateOrderUpdateForManager(Long orderId) throws BusinessException {
		BaseUserEntity user = securityService.getCurrentUser();
		OrdersEntity orderEntity = ordersRepository.findById(orderId)
												.orElseThrow(() -> getInvalidOrderException(ERR_ORDER_NOT_EXISTS));	
		
		if( !isSameOrgForOrderAndManager(orderEntity) ){
			throwForbiddenOrderException(ERR_ORDER_NOT_OWNED_BY_ADMIN, orderId, user.getId());
		}
		
		
		if(isStoreManager() && !isOrgManager()) {
			if( !isSameShopForOrderAndManager(orderEntity) ){
				throwForbiddenOrderException(ERR_ORDER_NOT_OWNED_BY_SHOP_MANAGER, orderId, user.getId());
			}
		}
	}





	private boolean isSameOrgForOrderAndManager(OrdersEntity orderEntity) {
		Long userOrgId = securityService.getCurrentUserOrganizationId();
		return Objects.equals(userOrgId, getOrderOrgId(orderEntity));
	}





	private boolean isSameShopForOrderAndManager(OrdersEntity orderEntity) throws BusinessException {
		BaseUserEntity user = securityService.getCurrentUser();
		
		if(!(user instanceof EmployeeUserEntity)) {
			throwForbiddenOrderException(ERR_NOT_EMP_ACCOUNT, user.getId());
		}
		
		EmployeeUserEntity manager = (EmployeeUserEntity)user;
		return Objects.equals(manager.getShopId(), getOrderShopId(orderEntity));
	}





	private Object getOrderShopId(OrdersEntity orderEntity) {
		return ofNullable(orderEntity)
					.map(OrdersEntity::getShopsEntity)
					.map(ShopsEntity::getId)
					.orElse(-1L);
	}





	private Long getOrderOrgId(OrdersEntity orderEntity) {
		return ofNullable(orderEntity)
						.map(OrdersEntity::getOrganizationEntity)
						.map(OrganizationEntity::getId)
						.orElse(-1L);
	}





	private Boolean isStoreManager() {
		return securityService.currentUserHasRole(STORE_MANAGER);
	}
	
	
	
	private Boolean isOrgManager() {
		return securityService.currentUserHasRole(ORGANIZATION_MANAGER);
	}





	private void validateConfirmedOrder(Long orderId) throws BusinessException {
		Integer basketCount = basketRepository.findByOrdersEntity_Id( orderId ).size();
		if(Objects.equals(basketCount, 0)) {
			throwInvalidOrderException(ERR_ORDER_CONFIRMED_WITH_EMPTY_BASKET, orderId);
		}
	}





	private boolean isCheckedOutOrder(OrderJsonDto order) {
		return Objects.equals( CLIENT_CONFIRMED.toString(), order.getStatus());
	}





	private Boolean isCustomerUser() {
		return securityService.currentUserHasRole(CUSTOMER);
	}





	private void validateOrderCreation(OrderJsonDto order) throws BusinessException {
		if( !isCustomerUser()) {
			BaseUserEntity user = securityService.getCurrentUser();
			throwForbiddenOrderException(ERR_MANAGER_CANNOT_CREATE_ORDERS, user.getId());
		}
		
		//TODO: can be removed, there is no reason for not-accepting empty baskets at the first place
		//it can be handled at confirmation
		if ( isNullOrEmpty(order.getBasket()) ) {
			throwInvalidOrderException(ERR_NEW_ORDER_WITH_EMPTY_BASKET);
		}

		validateBasketItems(order);
	}





	private boolean isNewOrder(OrderJsonDto order) {
		return Objects.equals(order.getStatus(), NEW.toString() );
	}
	
	
	
	
	

	private List<String> getPossibleOrderStatus() {
		List<String> possibleStatusList = Arrays.asList( OrderStatus.values() )
												.stream()
												.map(OrderStatus::toString)
												.collect(toList());
		return possibleStatusList;
	}
	
	
	
	

	private void validateBasketItems(OrderJsonDto order) throws BusinessException {	
		List<BasketItemDTO> basket = order.getBasket();
		
		for(BasketItemDTO item: basket) {
			validateBasketItem(item);
		}
	}
	


	private Map<Long, List<BasketItemDTO>> groupBasketsByShops(List<BasketItemDTO> baskets) {

		Map<Long,List<StocksEntity>> shopStocksMap = baskets.stream()
				.map(basket -> stockRepository.getOne(basket.getStockId()))
				.collect(groupingBy(stock -> stock.getShopsEntity().getId()));

		Map<Long, List<BasketItemDTO>> groupedBaskets = new HashMap<>();

		for(Long shopId : shopStocksMap.keySet()) {
			List<Long> stocks = shopStocksMap.get(shopId).stream().map(StocksEntity::getId).collect(toList());

			List<BasketItemDTO> shopBaskets= baskets.stream()
													.filter(basket -> stocks.contains(basket.getStockId()))
												    .collect(toList());

			groupedBaskets.put(shopId, shopBaskets);
		}

		return groupedBaskets;
	}
	
	
	

	private List<StocksEntity> getBasketStocks(List<BasketItemDTO> basket) {
		if(basket == null) {
			return new ArrayList<>();
		}
		
		List<Long> itemStockIds = basket.stream()
										.map(BasketItemDTO::getStockId)
										.collect(toList());
		
		List<StocksEntity> stocks =  (List<StocksEntity>) stockRepository.findAllById(itemStockIds);
		return stocks;
	}
	
	
	
	
	
	private void validateBasketItem(BasketItemDTO item) throws BusinessException {
		if(item == null) {
			throwInvalidOrderException(ERR_NULL_ITEM);
		}
		
		if(item.getQuantity() == null || item.getQuantity() <= 0) {
			throwInvalidOrderException(ERR_INVALID_ITEM_QUANTITY);
		}		
		
		if(item.getStockId() == null) {
			throwInvalidOrderException(ERR_NON_EXISTING_STOCK_ID);
		}
		
		Optional<StocksEntity> stocks = stockRepository.findById(item.getStockId());
		
		if(!stocks.isPresent()) {
			throwInvalidOrderException(ERR_NON_EXISTING_STOCK_ID);
		}
		
		if(item.getQuantity() > stockService.getStockQuantity( stocks.get() )) {
			throwInvalidOrderException(ERR_NO_ENOUGH_STOCK);
		}		
	}
	
	
	
	
	
	
	private void throwInvalidOrderException(String msg, Object... msgParams) throws BusinessException {
		throw getInvalidOrderException(msg, msgParams);
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
	
	
	
	
	
	private void throwForbiddenOrderException(String msg, Object... msgParams) throws BusinessException {
		throw getForbiddenOrderException(msg, msgParams);
	}
	
	
	
	private BusinessException getForbiddenOrderException(String msg, Object... msgParams) {
		String error = OrderFailedStatus.INVALID_ORDER.toString();
		return new BusinessException( format(msg, msgParams), error, HttpStatus.FORBIDDEN);
	}
	
	
	
	

	private OrderRepresentationObject createNewOrderAndBasketItems(OrderJsonDto order, List<BasketItemDTO> basketItems, Long shopId) throws BusinessException {
		
		OrdersEntity orderEntity = createNewOrderEntity(order, basketItems, shopId);
		orderEntity = ordersRepository.save(orderEntity);


		addItemsToBasket(basketItems, orderEntity);

		OrderRepresentationObject orderRepObj = new OrderRepresentationObject(orderEntity.getId(), orderEntity.getShopsEntity().getId(),
																				orderEntity.getAmount(), basketItems, HttpStatus.CREATED);
		orderRepObj.setItems(basketItems);
		return orderRepObj;
	}
	
	
	
	

	private OrdersEntity createNewOrderEntity(OrderJsonDto order, List<BasketItemDTO> basketItems, Long shopId) throws BusinessException {
		BaseUserEntity user = securityService.getCurrentUser();
		OrganizationEntity org = securityService.getCurrentUserOrganization();
		ShopsEntity shop = shopsRepo.findById(shopId).get();

		OrdersEntity orderEntity = new OrdersEntity();

		if (order.getAddressId() != null) {
			orderEntity.setAddressEntity(getOrderDeliveryAddress(order));
		}
		orderEntity.setAmount( calculateBasketTotalValue(basketItems) );
		// TODO ordersEntity.setPayment_type(payment_type);
		orderEntity.setShopsEntity(shop);
		orderEntity.setStatus( NEW.getValue() );
		orderEntity.setUserId( user.getId() );
		orderEntity.setName(user.getName());
		orderEntity.setOrganizationEntity( org );
		return orderEntity;
	}

	AddressesEntity getOrderDeliveryAddress(OrderJsonDto order) throws BusinessException {
		AddressesEntity address = null;
		Long userId = securityService.getCurrentUser().getId();
		if (order.getAddressId() != null) {
			address = addressRepo
						.findByIdAndUserId(order.getAddressId(), userId)
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0002, order.getAddressId()));
		}
		return address;
	}
	

	private ShopsEntity getOrderShop(OrderJsonDto order) {
		List<StocksEntity> stocksEntites = getBasketStocks( order.getBasket() );
		
		ShopsEntity shop = stocksEntites.stream()
										.map(StocksEntity::getShopsEntity)
										.findFirst()
										.orElse(null);
		return shop;
	}
	

	


	private OrderResponse updateOrder(OrderJsonDto orderJsonDto) throws BusinessException {

		OrderStatus newStatus = ofNullable(orderJsonDto.getStatus())
										.map(OrderStatus::findEnum)
										.orElse(NEW);
		OrdersEntity orderEntity =  
				ordersRepository
				.findById( orderJsonDto.getId() )
				.orElseThrow(() -> getInvalidOrderException(ERR_ORDER_NOT_EXISTS));
		OrderStatus oldStatus = orderEntity.getOrderStatus();
		OrderResponse orderResponse = updateCurrentOrderStatus(orderJsonDto, orderEntity, newStatus);
		
		if ( newStatus.equals(NEW)) {
			if (orderJsonDto.getAddressId() != null) {
				orderEntity.setAddressEntity(getOrderDeliveryAddress(orderJsonDto));
			}
			orderResponse = updateOrderBasket(orderJsonDto, orderEntity);

		}else if(newStatus.equals(CLIENT_CONFIRMED) 
					&& Objects.equals(oldStatus, NEW)) {
			reduceStocks(orderEntity);
		}
		
		return orderResponse;
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
			sendBillEmail(order);
		}catch(Throwable t) {
			logger.error(t,t);
		}
	}
	
	
	
	
	
	private void sendNotificationEmailToStoreManager(OrdersEntity order) {
		Long orderId = order.getId();
		
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
			mailService.sendThymeleafTemplateMail(to, subject, cc, template, parametersMap);
		} catch (IOException | MessagingException e) {
			logger.error(e, e);
		}
	}
	
	
	
	
	
	private Map<String, Object> createNotificationEmailParams(OrdersEntity order) {
		Map<String,Object> params = new HashMap<>();
		String orderTime = 
				DateTimeFormatter
				.ofPattern("dd/MM/YYYY - hh:mm")
				.format(order.getCreationDate());

		String domain = appConfig.environmentHostName;
		String orgDomain = domainService.getOrganizationDomainAndSubDir(order.getOrganizationEntity().getId());
		String orgLogo = domain + "/files/" + getOrganizationLogo(order.getOrganizationEntity());

		List<BasketItemDetails> basketItemsDetails = 
				getBasketItemsDetailsMap( setOf(order.getId()) )
				.get(order.getId());
		
		SubOrder subOrder = getSubOrder(order, getVariantsImagesList(basketItemsDetails));
		changeShippingServiceName(subOrder);

		Optional<PaymentEntity> payment = paymentsRepo.findByMetaOrderId(order.getMetaOrder().getId());
		String operator = "";
		if (payment.isPresent()) {
			operator = payment.get().getOperator();
			operator = changeOperatorName(operator);
		}

		params.put("domain", domain);
		params.put("orgDomain", orgDomain);
		params.put("orgLogo", orgLogo);
		params.put("creationTime", orderTime);
		params.put("orderPageUrl", buildDashboardPageUrl(order));
		params.put("sub", subOrder);
		params.put("operator", operator);
		return params;
	}
	
	
	
	
	private Map<String, Object> createRejectionEmailParams(OrdersEntity order, String rejectionReason) {
		Map<String,Object> params = new HashMap<>();
		String message =
				ofNullable(rejectionReason)
				.orElse(DEFAULT_REJECTION_MESSAGE);

		List<BasketItemDetails> basketItemsDetails =
				getBasketItemsDetailsMap( setOf(order.getId()) )
						.get(order.getId());

		SubOrder subOrder = getSubOrder(order, getVariantsImagesList(basketItemsDetails));
		changeShippingServiceName(subOrder);

		String domain = domainService.getCurrentServerDomain();
		String orgDomain = domainService.getOrganizationDomainAndSubDir(order.getOrganizationEntity().getId());
		String orgLogo = domain + "/files/" + getOrganizationLogo(order.getOrganizationEntity());
		String orgName = order.getMetaOrder().getOrganization().getName();

		params.put("domain", domain);
		params.put("orgDomain", orgDomain);
		params.put("orgLogo", orgLogo);
		params.put("id", order.getId().toString());
		params.put("rejectionReason", message);
		params.put("sub", subOrder);
		params.put("userName", order.getMetaOrder().getUser().getName());
		params.put("orgName", orgName);
		return params;
	}


	private Map<String, Object> createRejectionEmailParams(ReturnRequestEntity returnRequest, String rejectionReason) {
		Map<String,Object> params = new HashMap<>();
		String message =
				ofNullable(rejectionReason)
						.orElse(DEFAULT_REJECTION_MESSAGE);
		params.put("id", returnRequest.getId().toString());
		params.put("rejectionReason", message);
		params.put("returnRequest", returnRequest);
		return params;
	}


	
	
	private String buildDashboardPageUrl(OrdersEntity order) {
		Long orgId = order.getOrganizationEntity().getId();
		String domain = domainService
							.getOrganizationDomainOnly(orgId)
							.stream()
							.findFirst()
							.orElse("");
		String path = appConfig.dashBoardOrderPageUrl.replace("{order_id}", order.getId().toString());
		return format("%s/%s", domain, path);
	}



	private String buildDashboardPageUrl(ReturnRequestEntity returnRequest) {
		Long orgId = returnRequest.getMetaOrder().getOrganization().getId();
		String domain = domainService
							.getOrganizationDomainOnly(orgId).stream()
							.findFirst()
							.orElse("");
		String path = "dashboard/return-requests";
		return format("%s/%s/%d", domain, path, returnRequest.getId());
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





	private void sendBillEmail(MetaOrderEntity order) {
		Long orderId = order.getId();
		
		Optional<String> email = 
				ofNullable(order)
				.map(MetaOrderEntity::getUser)
				.map(UserEntity::getEmail);
		
		if(!email.isPresent()) {
			return;
		}
		
		String subject = format(BILL_EMAIL_SUBJECT, orderId);
		Map<String,Object> parametersMap = createBillEmailParams(order);
		String template = ORDER_BILL_TEMPLATE;
		try {
			mailService.sendThymeleafTemplateMail(email.get(), subject,  template, parametersMap);
		} catch (MessagingException e) {
			logger.error(e, e);
		}
	}





	private Map<String, Object> createBillEmailParams(MetaOrderEntity order) {
		LocalDateTime orderTime = 
				order
				.getSubOrders()
				.stream()
				.map(OrdersEntity::getCreationDate)
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(now());
		String orderTimeStr = 
				DateTimeFormatter
				.ofPattern("dd/MM/YYYY - hh:mm")
				.format(orderTime);

		String domain = appConfig.environmentHostName;
		String orgDomain = domainService.getOrganizationDomainAndSubDir(order.getOrganization().getId());

		String orgLogo = domain + "/files/"+ getOrganizationLogo(order.getOrganization());

		String orgName = order.getOrganization().getName();

		Order orderResponse = this.getOrderResponse(order);
		normalizeOrderForEmailTemplate(orderResponse);

		Map<String, Object> params = new HashMap<>();
		params.put("orgDomain", orgDomain);
		params.put("domain", domain);
		params.put("orgName", orgName);
		params.put("creation_date", orderTimeStr);
		params.put("orgLogo", orgLogo);
		params.put("data", orderResponse);
		return params;
	}


	private String getOrganizationLogo(OrganizationEntity org) {
		return getOrganizationLogo(ofNullable(org));
	}



	private String getOrganizationLogo(Optional<OrganizationEntity> org) {
		return org
				.map(OrganizationEntity::getId)
				.map(orgId -> orgImagesRepo.findByOrganizationEntityIdAndShopsEntityNullAndTypeOrderByIdDesc(orgId, 1))
				.map(List::stream)
				.flatMap(Stream::findFirst)
				.map(OrganizationImagesEntity::getUri)
				.orElse("nasnav-logo.png");
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
		String shopAreaName =
				shop
				.getAddressesEntity()
				.getAreasEntity()
				.getName();
		Shipment shipment = subOrder.getShipment();
		if (shipment.getServiceName().equals("PICKUP_POINTS") || shipment.getServiceName().equals("PICKUP")) {
			shipment.setServiceName("Pickup at " + subOrder.getShopName() + " - " + shopAreaName);
			AddressRepObj address = subOrder.getDeliveryAddress();
			BeanUtils.copyProperties(address, shopAddress);
			subOrder.setDeliveryAddress(shopAddress);
			subOrder.setPickup(true);
		}
		subOrder.setShipment(shipment);
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




	private OrderResponse updateOrderBasket(OrderJsonDto order, OrdersEntity orderEntity) throws BusinessException {

		basketRepository.deleteByOrderIdIn(asList(order.getId()));
		
		orderEntity.setAmount( calculateOrderTotalValue(order) );
		orderEntity.setShopsEntity( getOrderShop( order) );
		if (order.getAddressId() != null) {
			if (orderEntity.getOrderStatus().equals(NEW))
				orderEntity.setAddressEntity(addressRepo.findById(order.getAddressId()).get());
		}
		orderEntity = ordersRepository.save(orderEntity);

		addItemsToBasket(order, orderEntity);

		return new OrderResponse(orderEntity.getId(), orderEntity.getAmount());
	}
	
	

	
	private OrderResponse updateCurrentOrderStatus(OrderJsonDto orderJsonDto, OrdersEntity orderEntity,
												   OrderStatus newStatus) throws BusinessException {

		OrderStatus currentOrderStatus = findEnum(orderEntity.getStatus());
		validateNewOrderStatus(newStatus, currentOrderStatus);
		
		if (newStatus == OrderStatus.CLIENT_CONFIRMED) {
			//TODO: WHY ???
			orderEntity.setCreationDate( now() );
		}
		orderEntity.setStatus(newStatus.getValue());
		orderEntity = ordersRepository.save(orderEntity);
		
		return new OrderResponse(orderEntity.getId(), orderEntity.getAmount());
	}





	private void validateNewOrderStatus(OrderStatus newStatus, OrderStatus currentStatus) throws BusinessException {	
		
		if(newStatus == null || !canOrderStatusChangeTo(currentStatus, newStatus)) {
			throwInvalidOrderException(ERR_INVALID_ORDER_STATUS_UPDATE, currentStatus.name(), newStatus.name());
		};		
		
		boolean isCustomer = securityService.currentUserHasRole(Roles.CUSTOMER);
		if(!isCustomer) {
			validateManagerCanSetStatus(newStatus); 
		}else {
			validateCustomerCanSetStatus(newStatus);
		}
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



	private ReturnRequestEntity updateReturnRequestStatus(ReturnRequestEntity request, ReturnRequestStatus newStatus){
		ReturnRequestStatus currentStatus = ReturnRequestStatus.findEnum(request.getStatus());

		if(!canOrderReturnStatusChangeTo(currentStatus, newStatus)){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0018, currentStatus.name(), newStatus.name());
		}
		request.setStatus(newStatus.getValue());
		return returnRequestRepo.save(request);
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


	private boolean canOrderReturnStatusChangeTo(ReturnRequestStatus currentStatus, ReturnRequestStatus newStatus) {
		return ofNullable(currentStatus)
				.map(orderReturnStateMachine::get)
				.orElse(emptySet())
				.contains(newStatus);
	}
	

	private void validateManagerCanSetStatus(OrderStatus newStatus) throws BusinessException {
		if( !canManagerSetOrderStatusTo(newStatus)) {
			throw new BusinessException(
					format(ERR_ORDER_STATUS_NOT_ALLOWED_FOR_ROLE, newStatus.name(), "*MANAGER")
					,"INVALID_PARAM: status"
					, NOT_ACCEPTABLE);
		}
	}





	private boolean canManagerSetOrderStatusTo(OrderStatus newStatus) {
		return orderStatusForManagers.contains(newStatus);
	}





	private void validateCustomerCanSetStatus(OrderStatus newStatus) throws BusinessException {
		if( !canCustomerSetOrderStatusTo(newStatus)) {
			throw new BusinessException(
					format(ERR_ORDER_STATUS_NOT_ALLOWED_FOR_ROLE, newStatus.name(), Roles.CUSTOMER.name())
					,"INVALID_PARAM: status"
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}





	private boolean canCustomerSetOrderStatusTo(OrderStatus newStatus) {
		return orderStatusForCustomers.contains(newStatus);
	}


	private void addItemsToBasket(List<BasketItemDTO> basketItems, OrdersEntity orderEntity) throws BusinessException {
		if(basketItems == null) {
			return;
		}

		for (BasketItemDTO basketItem : basketItems) {
			BasketsEntity itemEntity = toBasketEntity(basketItem, orderEntity);
			basketRepository.save(itemEntity);
		}
	}


	private void addItemsToBasket(OrderJsonDto orderJsonDto, OrdersEntity orderEntity) throws BusinessException {
		List<BasketItemDTO> basketItems = orderJsonDto.getBasket();
		if(basketItems == null) {
			return;
		}
		
		for (BasketItemDTO basketItem : basketItems) {
			BasketsEntity itemEntity = toBasketEntity(basketItem, orderEntity);
			basketRepository.save(itemEntity);
		}
	}
	
	
	
	
	private BasketsEntity toBasketEntity(BasketItemDTO item, OrdersEntity order) throws BusinessException {
		StocksEntity stock = Optional.ofNullable(item)
									.map(BasketItemDTO::getStockId)
									.flatMap(stockRepository::findById)
									.orElseThrow(() ->  getInvalidOrderException(ERR_NON_EXISTING_STOCK_ID));
		
		BasketsEntity basketsEntity = new BasketsEntity();
		basketsEntity.setStocksEntity(stock);
		basketsEntity.setOrdersEntity(order);
		// TODO make sure price here means item price multiplied by quantity
		basketsEntity.setPrice(new BigDecimal(item.getQuantity()).multiply(stock.getPrice()));
		basketsEntity.setQuantity(new BigDecimal(item.getQuantity()));

		// TODO how currency determined for specific order
		basketsEntity.setCurrency(EGP.getValue());

		return basketsEntity;
	}
	
	
	

	private BigDecimal calculateOrderTotalValue(OrderJsonDto order) {
		List<BasketItemDTO> basket = order.getBasket();
		if(basket == null) {
			return BigDecimal.ZERO;
		}
		
		return basket.stream()
					.map(this::getBasketItemValue)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
	}


	private BigDecimal calculateBasketTotalValue(List<BasketItemDTO> basket) {
		if(basket == null) {
			return BigDecimal.ZERO;
		}

		return basket.stream()
				.map(this::getBasketItemValue)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
	
	
	
	private BigDecimal getBasketItemValue(BasketItemDTO item) {
		BigDecimal quantity = ofNullable( item.getQuantity() )
								.map(q -> q.toString())	//it is preferred to create Bigdecimals from strings
								.map(BigDecimal::new)
								.orElse(ZERO);
		
		return ofNullable(item)
				.map(BasketItemDTO::getStockId)
				.flatMap(stockRepository::findById)
				.map(StocksEntity::getPrice)
				.filter(Objects::nonNull)
				.map(price -> price.multiply(quantity))
				.orElseThrow( () ->  new RuntimeException(ERR_CALC_ORDER_FAILED));
	}
	
	



	@Override
	@Transactional
	public DetailedOrderRepObject getOrderInfo(Long orderId, Integer detailsLevel) throws BusinessException {
		
		BaseUserEntity user = securityService.getCurrentUser();
		Long orgId = securityService.getCurrentUserOrganizationId();
		boolean isNasnavAdmin = securityService.currentUserHasRole(NASNAV_ADMIN);
		Optional<OrdersEntity> order = Optional.empty();;

		Integer finalDetailsLevel = getFinalDetailsLevel(detailsLevel);

		if (user instanceof UserEntity ) {
			order = ordersRepository.findByIdAndUserIdAndOrganizationEntity_Id(orderId, user.getId(), orgId);
		} else if ( isNasnavAdmin ) {
			order = ordersRepository.findFullDataById(orderId);
		} else {
			order = ordersRepository.findByIdAndOrganizationEntity_Id(orderId, orgId);
		}

		if (order.isPresent()) {
			return getDetailedOrderInfo(order.get(), finalDetailsLevel);
		}

		throwInvalidOrderException( INVALID_ORDER.toString() );
		
		return null;
	}
	
	private Integer getFinalDetailsLevel(Integer detailsLevel) {
		return (detailsLevel == null || detailsLevel < 0 || detailsLevel > 3) ? ORDER_FULL_DETAILS_LEVEL : detailsLevel;
	}
	
	
	
	private DetailedOrderRepObject getDetailedOrderInfo(OrdersEntity order, Integer detailsLevel) {
	    Map<Long, List<BasketItemDetails>> basketItemsDetailsMap = getBasketItemsDetailsMap( setOf(order.getId()) );
		Map<Long, BigDecimal> orderItemsQuantity = getOrderItemsQuantity( setOf(order.getId()) );
		Map<Long, String> paymentOperator = getPaymentOperators(detailsLevel, setOf(order.getId()));
		String phoneNumber = userRepo.findById(order.getUserId()).get().getPhoneNumber();
	    return getDetailedOrderInfo(order, detailsLevel, orderItemsQuantity, basketItemsDetailsMap, phoneNumber ,paymentOperator);
	}
	
	
	

	private DetailedOrderRepObject getDetailedOrderInfo(OrdersEntity order, Integer detailsLevel,
														Map<Long, BigDecimal> orderItemsQuantity,
														Map<Long, List<BasketItemDetails>> basketItemsDetailsMap,
														String phoneNumber,
														Map<Long, String> paymentOperator) {
		DetailedOrderRepObject representation = new DetailedOrderRepObject();

		List<BasketItemDetails> basketItemsDetails = 
				ofNullable(basketItemsDetailsMap)
				.map(map -> map.get(order.getId()) )
				.orElse(new ArrayList<>());
		Map<Long, Optional<String>> variantsImagesList = getVariantsImagesList(basketItemsDetails);
		
		BeanUtils.copyProperties(getOrderSummary(order), representation);
		if (detailsLevel == null){
			detailsLevel = 0;
		}

        if (detailsLevel >= 1){
			BeanUtils.copyProperties(
					getOrderDetails(order, phoneNumber, paymentOperator)
					, representation
					, new String[]{"orderId", "userId", "shopId", "createdAt", "status", "paymentStatus", "metaOrderId", "discount"});
		}

        if (detailsLevel == 2 && orderItemsQuantity.get(order.getId()) != null){
			representation.setTotalQuantity(orderItemsQuantity.get(order.getId()).intValue());
		}

		if (detailsLevel == 3) {
			List<BasketItem> itemsList = getBasketItems(basketItemsDetails, variantsImagesList);
			representation.setItems(itemsList);
			
			if (itemsList.size() > 0) {
				Integer totalQuantity = itemsList.stream()
												 .map(BasketItem::getQuantity)
												 .reduce(0, Integer::sum);
				representation.setCurrency(itemsList.get(0).getCurrency());
				representation.setTotalQuantity( totalQuantity);
			}
		}
		
		return representation;
	}
	
	
	
	

	private DetailedOrderRepObject getOrderSummary(OrdersEntity entity) {
		Long metaOrderId = 
				ofNullable(entity)
				.map(OrdersEntity::getMetaOrder)
				.map(MetaOrderEntity::getId)
				.orElse(null);
		
		DetailedOrderRepObject obj = new DetailedOrderRepObject();
		obj.setOrderId(entity.getId() );
		obj.setUserId(entity.getUserId());
		obj.setShopId(entity.getShopsEntity().getId());
		obj.setCreatedAt(entity.getCreationDate());
		obj.setStatus(findEnum(entity.getStatus()).name());
		obj.setPaymentStatus(entity.getPaymentStatus().toString());
		obj.setTotal(entity.getAmount());
		obj.setMetaOrderId(metaOrderId);
		obj.setDiscount(entity.getDiscounts());
		return obj;
	}



	private DetailedOrderRepObject getOrderDetails(OrdersEntity entity, String phoneNumber, Map<Long, String> paymentOperator) {
		DetailedOrderRepObject obj = new DetailedOrderRepObject();

		obj.setUserName(entity.getName());
		obj.setShopName(entity.getShopsEntity().getName());
		obj.setDeliveryDate(entity.getDeliveryDate());
		obj.setSubtotal(entity.getAmount());
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

		return obj;
	}
	
	

	private List<BasketItem> getBasketItems(List<BasketItemDetails> itemsDetailsList, Map<Long, Optional<String>> variantsImagesList) {
		return itemsDetailsList.stream()
							.map(basket -> toBasketItem(basket, variantsImagesList))
							.collect(toList());
	}
	
	
	
	

	private BasketItem toBasketItem(BasketItemDetails itemDetails, Map<Long, Optional<String>> variantsCoverImages) {
		BigDecimal price = itemDetails.getPrice();
		BigDecimal discount = ofNullable(itemDetails.getDiscount()).orElse(ZERO);
		BigDecimal totalPrice = price.subtract(discount).multiply(itemDetails.getQuantity());
		BigDecimal discountPercentage = calculatePercentage(discount, price);

		BasketItem item = new BasketItem();
		item.setProductId(itemDetails.getProductId());
		item.setName(itemDetails.getProductName());
		item.setPname(itemDetails.getProductPname());
		item.setStockId(itemDetails.getStockId());
		item.setQuantity(itemDetails.getQuantity().intValue());
		item.setTotalPrice(totalPrice);
		item.setPrice(price);
		item.setDiscount(discountPercentage);
		item.setThumb( variantsCoverImages.get(itemDetails.getVariantId()).orElse(null));
		item.setCurrency(ofNullable(getTransactionCurrency(itemDetails.getCurrency())).orElse(EGP).name());
		item.setUnit(itemDetails.getUnit());
		item.setProductCode(itemDetails.getProductCode());
		item.setSku(itemDetails.getSku());

		return item;
	}


	private BasketItem toBasketItem(BasketsEntity entity, Map<Long, Optional<String>> variantsCoverImages) {
		ProductVariantsEntity variant = entity.getStocksEntity().getProductVariantsEntity();
		ProductEntity product = variant.getProductEntity();
		BigDecimal price = entity.getPrice();
		BigDecimal discount = ofNullable(entity.getDiscount()).orElse(ZERO);
		BigDecimal totalPrice = price.multiply(entity.getQuantity());
		BigDecimal discountPercentage = calculatePercentage(discount, price);
		String thumb = variantsCoverImages.get(variant.getId()).orElse(null);
		String currency = ofNullable(getTransactionCurrency(entity.getCurrency())).orElse(EGP).name();
		CountriesEntity country = entity.getOrdersEntity().getOrganizationEntity().getCountry();
		String currencyValue = "";
		if (country != null) {
			currencyValue = country.getCurrency();
		}
		Boolean isReturnable = this.isReturnable(entity);

		BasketItem item = new BasketItem();
		item.setId(entity.getId());
		item.setProductId(product.getId());
		item.setName(product.getName());
		item.setPname(product.getPname());
		item.setStockId(entity.getStocksEntity().getId());
		item.setQuantity(entity.getQuantity().intValueExact());
		item.setTotalPrice(totalPrice);
		item.setPrice(price);
		item.setDiscount(discountPercentage);
		item.setBrandId(product.getBrandId());
		item.setVariantFeatures(parseVariantFeatures(variant.getFeatureSpec(), 0));
		item.setThumb(thumb);
		item.setCurrency(currency);
		item.setProductType(product.getProductType());
		item.setVariantId(variant.getId());
		item.setVariantName(variant.getName());
		item.setIsReturnable(isReturnable);
		item.setCurrencyValue(currencyValue);
		item.setSku(variant.getSku());
		item.setProductCode(variant.getProductCode());
		String unitName = ofNullable(entity)
				.map(BasketsEntity::getStocksEntity)
				.map(StocksEntity::getUnit)
				.map(StockUnitEntity::getName)
				.orElse("");

		item.setUnit(unitName);

		return item;
	}
	
	
	
	@Override
	public List<DetailedOrderRepObject> getOrdersList(OrderSearchParam params) throws BusinessException {
		OrderSearchParam finalParams = getFinalOrderSearchParams(params);
		Integer detailsLevel = finalParams.getDetails_level();

		List<OrdersEntity> ordersEntityList = em.createQuery(getOrderCriteriaQuery(finalParams))
												.setFirstResult(finalParams.getStart())
												.setMaxResults(finalParams.getCount())
												.getResultList();
		
		Set<Long> ordersIds = new HashSet<>();

		if ( detailsLevel >= 2) {
			ordersIds = ordersEntityList.stream().map(OrdersEntity::getId).collect(toSet());
		}

		Map<Long, List<BasketItemDetails>> basketItemsDetailsMap = getBasketItemsDetailsMap(detailsLevel == 3 ? ordersIds : new HashSet<>() );

		Map<Long, BigDecimal> orderItemsQuantity = getOrderItemsQuantity(detailsLevel == 2 ? ordersIds : new HashSet<>());

		Map<Long, String> orderPhones = getCustomerPhones(ordersIds);
		Map<Long, String> paymentOperator = getPaymentOperators(detailsLevel, ordersIds);
		return ordersEntityList
				.stream()
				.map(order -> getDetailedOrderInfo(order, detailsLevel, orderItemsQuantity, basketItemsDetailsMap, orderPhones.get(order.getId()), paymentOperator))
				.collect(toList());
	}



	private Map<Long, String> getPaymentOperators(Integer detailsLevel, Set<Long> ordersIds) {
		if(detailsLevel < 2 || ordersIds.isEmpty()){
			return emptyMap();
		}
		return ordersRepository
				.findPaymentOperatorByOrderIdIn(ordersIds)
				.stream()
				.filter(payOpr -> noneIsNull(payOpr, payOpr.getOrderId(), payOpr.getOperator()))
				.collect(toMap(OrderPaymentOperator::getOrderId, OrderPaymentOperator::getOperator));
	}



	private Map<Long, String> getCustomerPhones(Set<Long> ordersIds) {
		return !ordersIds.isEmpty() ?
					ordersRepository.findUsersPhoneNumber(ordersIds)
					.stream()
					.collect(toMap(OrderPhoneNumberPair::getOrderId, pair -> ofNullable(pair.getPhoneNumber()).orElse("")))
				: new LinkedHashMap<>();
	}




	private Map<Long, List<BasketItemDetails>> getBasketItemsDetailsMap(Set<Long> ordersIds) {
		Set<Long> nonEmptyOrdersIds = ofNullable(ordersIds)
											.filter(set -> !set.isEmpty())
											.orElse( setOf(NON_EXISTING_ORDER_ID) );
		
		List<BasketItemDetails> basketData = em.createNamedQuery("Basket", BasketItemDetails.class)
												.setParameter("orderId", nonEmptyOrdersIds)
												.getResultList();
		return  basketData
				.stream()
				.filter(Objects::nonNull)
				.collect( groupingBy(BasketItemDetails::getOrderId));
	}


	
	
	private Map<Long, BigDecimal> getOrderItemsQuantity(Set<Long> orderIds) {
		List<BasketsEntity> basketsEntities = 
				basketRepository
				.findByOrdersEntity_IdIn(orderIds)
				.stream()
				.collect( toList());

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
		List<String> employeeUserRoles = employeeUserServiceHelper.getEmployeeUserRoles(empUser.getId());
		
		if ( collectionContainsAnyOf(employeeUserRoles, "ORGANIZATION_ADMIN", "ORGANIZATION_MANAGER", "ORGANIZATION_EMPLOYEE") )  {
			newParams.setOrg_id(empUser.getOrganizationId());
		} else if ( collectionContainsAnyOf(employeeUserRoles, "STORE_ADMIN", "STORE_MANAGER", "STORE_EMPLOYEE")) {
			newParams.setShop_id(empUser.getShopId());
		} else {
			newParams.setOrg_id(params.getOrg_id());
			newParams.setShop_id(params.getShop_id());
		}
	}


	private void setOrderSearchStartAndCount(OrderSearchParam params, OrderSearchParam newParams) {
		if (params.getStart() == null || params.getStart() <= 0)
			newParams.setStart(0);
		else
			newParams.setStart(params.getStart());

		if (params.getCount() == null || params.getCount() <= 0 || params.getCount() >= ORDER_DEFAULT_COUNT)
			newParams.setCount(ORDER_DEFAULT_COUNT);
		else
			newParams.setCount(params.getCount());
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
		
		Predicate[] predicatesArr = getOrderQueryPredicates(params, builder, root);

		query
		.where(predicatesArr)
		.orderBy(builder.desc(root.get("updateDate")));
		
		return query;
	}



	private ReturnRequestsResponse getReturnRequestCriteriaQuery(ReturnRequestSearchParams params, CriteriaBuilder builder) {

		CriteriaQuery<ReturnRequestEntity> query = builder.createQuery(ReturnRequestEntity.class).distinct(true);
		Root<ReturnRequestEntity> root = query.from(ReturnRequestEntity.class);
		root.fetch("metaOrder", LEFT);
		root.fetch("createdByUser", LEFT);
		root.fetch("createdByEmployee", LEFT);

		Predicate[] predicatesArr = getReturnRequestQueryPredicates(params, builder, root);

		query.where(predicatesArr);

		javax.persistence.criteria.Order order = builder.desc(root.get("id"));

		query.orderBy(order);

		Set<ReturnRequestDTO> returnRequestDTOS = em.createQuery(query)
				.setFirstResult(params.getStart())
				.setMaxResults(params.getCount())
				.getResultStream()
				.map(request -> (ReturnRequestDTO)request.getRepresentation())
				.collect(toSet());

		if (!returnRequestDTOS.isEmpty()) {
			addItemsCount(returnRequestDTOS);
		}
		Long count = getOrderReturnRequestsCount(builder, predicatesArr);

		return new ReturnRequestsResponse(count, returnRequestDTOS);
	}


	private void addItemsCount(Set<ReturnRequestDTO> returnRequestDTOS) {
		Set<Long> returnRequestIds = returnRequestDTOS
				.stream()
				.map(ReturnRequestDTO::getId)
				.collect(toSet());
		Map<Long, Long> itemsCountMap = returnRequestRepo.getReturnRequestsItemsCount(returnRequestIds)
				.stream()
				.collect(toMap(p -> p.getFirst(), p -> p.getSecond()));
		returnRequestDTOS.stream().forEach(r -> r.setItemsCount(itemsCountMap.get(r.getId())));
	}



	private Predicate[] getReturnRequestQueryPredicates(ReturnRequestSearchParams params, CriteriaBuilder builder, Root<ReturnRequestEntity> root) {
		List<Predicate> predicates = new ArrayList<>();
		Long currentOrgId = securityService.getCurrentUserOrganizationId();
		Path<Map<String, String>> returnedItems = root.join("returnedItems", INNER);

		Predicate orgId = builder.equal(root.get("metaOrder").get("organization").get("id"), currentOrgId);
        predicates.add(orgId);

        parseTimeString(params.getDate_from())
			.map(from -> builder.greaterThanOrEqualTo(root.get("createdOn"), from))
			.ifPresent(predicates::add);

		parseTimeString(params.getDate_to())
				.map(to -> builder.lessThanOrEqualTo(root.get("createdOn"), to))
				.ifPresent(predicates::add);


		if (params.getStatus() != null) {
			Predicate status = builder.equal(root.get("status"), params.getStatus().getValue());
			predicates.add(status);
		}

		if (params.getMeta_order_id() != null) {
			Predicate metaOrderId = builder.equal(root.get("metaOrder").get("id"), params.getMeta_order_id());
			predicates.add(metaOrderId);
		}

		if (securityService.currentUserHasRole(STORE_MANAGER) && !securityService.currentUserHasRole(ORGANIZATION_ADMIN)) {
			Long shopId = ((EmployeeUserEntity)securityService.getCurrentUser()).getShopId();
			params.setShop_id(shopId);
		}

		if(params.getShop_id() != null) {
			Predicate shopId = builder.equal(returnedItems.get("basket").get("stocksEntity").get("shopsEntity").get("id"), params.getShop_id());
			predicates.add(shopId);
		}


		return predicates.stream().toArray( Predicate[]::new) ;
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
	
	
	
	

	@Override
	public DetailedOrderRepObject getCurrentOrder(Integer detailsLevel) throws BusinessException {
		BaseUserEntity user = securityService.getCurrentUser();
		
		OrdersEntity entity = ordersRepository.findFirstByUserIdAndStatusOrderByUpdateDateDesc( user.getId(), NEW.getValue() )
											 .orElseThrow(() -> getNoCurrentOrderFoundException() );
		
		return getDetailedOrderInfo(entity, ORDER_FULL_DETAILS_LEVEL);
	}


	
	
	
	private BusinessException getNoCurrentOrderFoundException() {
		return new BusinessException("User have no new orders!", "NOT FOUND", HttpStatus.NOT_FOUND);
	}



	@Override
	@Transactional
	public void deleteCurrentOrders() {
		BaseUserEntity user = securityService.getCurrentUser();
		
		List<Long> userNewOrders = 
				ordersRepository.findByUserIdAndStatus(user.getId(), NEW.getValue())
								.stream()
								.map(OrdersEntity::getId)
								.collect(toList());
		
		basketRepository.deleteByOrderIdIn(userNewOrders);		
		ordersRepository.deleteByStatusAndUserId( NEW.getValue(), user.getId());
	}


	@Override
	@Transactional
	public void deleteOrders(List<Long> orderIds) throws BusinessException {
		Long orgId = securityService.getCurrentUserOrganizationId();

		validateOrdersDeletionIds(orderIds, orgId);

		basketRepository.deleteByOrderIdInAndOrganizationIdAndStatus(orderIds, orgId, NEW.getValue());
		ordersRepository.deleteByStatusAndIdInAndOrgId( NEW.getValue(), orderIds, orgId);
	}

	private void validateOrdersDeletionIds(List<Long> orderIds, Long orgId) throws BusinessException {
		List<OrdersEntity> orders = ordersRepository.getOrdersIn(orderIds);

		for(OrdersEntity order : orders) {
			if (!order.getOrganizationEntity().getId().equals(orgId))
				throw new BusinessException("Provided order ("+order.getId()+") doesn't belong to current organization",
						"INVALID_PARAM: order_id", NOT_ACCEPTABLE);
			if (order.getStatus() != 0)
				throw new BusinessException("Provided order ("+order.getId()+") is not a new order and can't be deleted",
						"INVALID_PARAM: order_id", NOT_ACCEPTABLE);
		}

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


	
	@Override
	public Cart getUserCart(Long userId) {
		return new Cart(toCartItemsDto(cartItemRepo.findCurrentCartItemsByUser_Id(userId)));
	}


	@Override
	public Cart getCart() {
		BaseUserEntity user = securityService.getCurrentUser();
		if(user instanceof EmployeeUserEntity) {
			throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
		}

		return getUserCart(user.getId());
	}


	@Override
	public Cart addCartItem(CartItem item){
		BaseUserEntity user = securityService.getCurrentUser();
		if(user instanceof EmployeeUserEntity) {
			throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
		}

		Long orgId = securityService.getCurrentUserOrganizationId();
		StocksEntity stock = 
				ofNullable(item.getStockId())
				.map(id -> stockRepository.findByIdAndOrganizationEntity_Id(id, orgId))
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE ,P$STO$0001,item.getStockId()));
		validateCartItem(stock, item);

		CartItemEntity cartItem = 
				ofNullable(cartItemRepo.findByStock_IdAndUser_Id(stock.getId(), user.getId()))
				.orElse(new CartItemEntity());

		if (item.getQuantity().equals(0)) {
			if (cartItem.getId() != null) {
				return deleteCartItem(cartItem.getId());
			} else {
				return getUserCart(user.getId());
			}
		}

		cartItem.setUser((UserEntity) user);
		cartItem.setStock(stock);
		cartItem.setQuantity(item.getQuantity());
		cartItem.setCoverImage(getItemCoverImage(item.getCoverImg(), stock));
		cartItemRepo.save(cartItem);

		return getUserCart(user.getId());
	}


	private String getItemCoverImage(String coverImage, StocksEntity stock) {
		if (coverImage != null) {
			return coverImage;
		}
		Long productId = stock.getProductVariantsEntity().getProductEntity().getId();
		Long variantId = stock.getProductVariantsEntity().getId();
		return ofNullable(imgService.getProductsAndVariantsImages(asList(productId), asList(variantId))
									.stream()
									.findFirst())
				.get()
				.orElse(new ProductImageDTO())
				.getImagePath();
	}

	@Override
	public Cart deleteCartItem(Long itemId){
		BaseUserEntity user = securityService.getCurrentUser();
		if(user instanceof EmployeeUserEntity) {
			throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
		}

		cartItemRepo.deleteByIdAndUser_Id(itemId, user.getId());

		return getUserCart(user.getId());
	}


	private void validateCartItem(StocksEntity stock, CartItem item) {
		if (item.getQuantity() == null || item.getQuantity() < 0) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0002);
		}

		if (item.getQuantity() > stock.getQuantity()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0003);
		}

	}

	private List<CartItem> toCartItemsDto(List<CartItemData> cartItems) {
		return cartItems
				.stream()
				.map(this::createCartItemDto)
				.collect(toList());
	}



	
	
	private CartItem createCartItemDto(CartItemData itemData) {
		CartItem itemDto = new CartItem();
		
		Map<String,String> variantFeatures = parseVariantFeatures(itemData.getFeatureSpec(), 0);
		
		itemDto.setBrandId(itemData.getBrandId());
		itemDto.setBrandLogo(itemData.getBrandLogo());
		itemDto.setBrandName(itemData.getBrandName());
		
		itemDto.setCoverImg(itemData.getCoverImg());
		itemDto.setPrice(itemData.getPrice());
		itemDto.setQuantity(itemData.getQuantity());
		itemDto.setVariantFeatures(variantFeatures);
		itemDto.setName(itemData.getProductName());
		
		itemDto.setId(itemData.getId());
		itemDto.setProductId(itemData.getProductId());
		itemDto.setVariantId(itemData.getVariantId());
		itemDto.setVariantName(itemData.getVariantName());
		itemDto.setProductType(itemData.getProductType());
		itemDto.setStockId(itemData.getStockId());
		itemDto.setDiscount(itemData.getDiscount());
		
		return itemDto;
	}
	
	
	


	private Map<String, String> parseVariantFeatures(String featureSpec, Integer returnedName) {
		return productService.parseVariantFeatures(featureSpec, returnedName);
	}





	@Override
	@Transactional(rollbackFor = Throwable.class)
	public OrderConfrimResponseDTO confrimOrder(Long orderId) {
		EmployeeUserEntity storeMgr = getAndValidateUser();
		OrdersEntity subOrder = getAndValidateOrderForConfirmation(orderId, storeMgr);
		
		confirmSubOrderAndMetaOrder(subOrder);
		
		return  shippingMgrService
				.requestShipment(subOrder)
				.doOnNext(trackerData -> saveShipmentTracker(trackerData, subOrder))
				.map(trackerData -> new OrderConfrimResponseDTO(trackerData.getAirwayBillFile()))
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





	private void confirmSubOrderAndMetaOrder(OrdersEntity order) {
		updateOrderStatus(order, STORE_CONFIRMED);
		
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
		shipment.setStatus(REQUSTED.getValue());
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
	
	
	

	public ArrayList<OrdersEntity> getOrdersForMetaOrder(Long metaOrderId) {
		if (metaOrderId == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(ordersRepository.findByMetaOrderId(metaOrderId));
	}
	
	
	
	
	
	@Override
	@Transactional(rollbackFor = Throwable.class)
	public Order checkoutCart(CartCheckoutDTO dto) throws IOException {
		BaseUserEntity user = securityService.getCurrentUser();
		if(user instanceof EmployeeUserEntity) {
			throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
		}
		
		cancelAbandonedOrders();

		validateCartCheckoutDTO(dto);

		MetaOrderEntity order = createMetaOrder(dto);
		
		return getOrderResponse(order);
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



	private MetaOrderEntity createMetaOrder(CartCheckoutDTO dto) {
		BaseUserEntity user = securityService.getCurrentUser();
		AddressesEntity userAddress = 
				addressRepo
				.findByIdAndUserId(dto.getAddressId(), user.getId())
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0002, dto.getAddressId()));

		CartItemsGroupedByShopId checkOutData = getAndValidateCheckoutData(dto); 
		MetaOrderEntity order = createOrder( checkOutData, userAddress, dto );
		return order;
	}





	private CartItemsGroupedByShopId getAndValidateCheckoutData(CartCheckoutDTO checkoutDto) {
		//TODO: this should be moved to checkOut main method, and then passes
		//the optimized cart to the rest of the logic.
		Cart optimizedCart = optimizeCartForCheckout(checkoutDto); 
		List<CartCheckoutData> userCartItems = createCheckoutData(optimizedCart);
		
		validateCartCheckoutItems(userCartItems);
		
		shippingMgrService.validateCartForShipping(userCartItems, checkoutDto);

		return userCartItems
				.stream()
				.collect(collectingAndThen(
							groupingBy(CartCheckoutData::getShopId)
							, CartItemsGroupedByShopId::new));
	}

	


	private Cart optimizeCartForCheckout(CartCheckoutDTO checkoutDto) {
		CartOptimizeResponseDTO optimizationResult = 
				cartOptimizationService.optimizeCart(checkoutDto);
		if(optimizationResult.getTotalChanged()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CHK$0004);
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
		Map<Long, StockAdditionalData> stockAdditionalDataCache = 
				stockRepository
				.findAdditionalDataByStockIdIn(cartStocks)
				.stream()
				.collect(groupingBy(StockAdditionalData::getStockId))
				.entrySet()
				.stream()
				.map(this::createStockAdditionalDataEntry)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		
		return optimizedCart
				.getItems()
				.stream()
				.map(item -> createCartCheckoutData(item, stockAdditionalDataCache))
				.collect(toList());
	}


	
	
	private Map.Entry<Long, StockAdditionalData> createStockAdditionalDataEntry(Map.Entry<Long, List<StockAdditionalData>> entry){
		StockAdditionalData data = 
				entry
				.getValue()
				.stream()
				.findFirst()
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, G$STK$0001, entry.getKey()));
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
		checkoutData.setFeatureSpec(stockData.getVariantSpecs());
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
		return checkoutData;
	}



	private Order getOrderResponse(MetaOrderEntity order) {
		Map<Long, Optional<String>> variantsCoverImages = getVariantsImagesList(order);

		Order orderDto = setMetaOrderBasicData(order);

		List<SubOrder> subOrders = 	createSubOrderDtoList(order, variantsCoverImages);
		Boolean isCancelable = isCancelable(order);

		orderDto.setSubOrders(subOrders);
		orderDto.setSubtotal(order.getSubTotal());
		orderDto.setShipping(order.getShippingTotal());
		orderDto.setTotal(order.getGrandTotal());
		orderDto.setDiscount(order.getDiscounts());
		orderDto.setIsCancelable(isCancelable);
		return orderDto;
	}


	private Map<Long, Optional<String>> getVariantsImagesList(MetaOrderEntity order) {
		List<Long> variantsIds = 
				order
				.getSubOrders()
				.stream()
				.map(OrdersEntity::getBasketsEntity)
				.flatMap(Set::stream)
				.map(BasketsEntity::getStocksEntity)
				.map(StocksEntity::getProductVariantsEntity)
				.map(ProductVariantsEntity::getId)
				.collect(toList());
		return imgService.getVariantsCoverImages(variantsIds);
	}



	private Map<Long, Optional<String>> getVariantsImagesList(List<BasketItemDetails> basketItems) {
		List<Long> variantsIds =
				basketItems
				.stream()
				.map(BasketItemDetails::getVariantId)
				.collect(toList());
		return imgService.getVariantsCoverImages(variantsIds);
	}



	private List<SubOrder> createSubOrderDtoList(MetaOrderEntity order, Map<Long, Optional<String>> variantsCoverImages) {
		return order
		.getSubOrders()
		.stream()
		.map(subOrder -> getSubOrder(subOrder, variantsCoverImages))
		.collect(toList());
	}



	private Order setMetaOrderBasicData(MetaOrderEntity metaOrder) {
		Order order = new Order();
		order.setUserId(metaOrder.getUser().getId());
		order.setUserName(metaOrder.getUser().getName());
		order.setOrderId(metaOrder.getId());
		order.setCurrency(getOrderCurrency(metaOrder));
		order.setCreationDate(metaOrder.getCreatedAt());

		String status = ofNullable(findEnum(metaOrder.getStatus()))
						.orElse(NEW)
						.name();
		order.setStatus(status);

		Optional<PaymentEntity> payment = paymentsRepo.findByMetaOrderId(metaOrder.getId());
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
	public Order getMetaOrder(Long orderId){
		BaseUserEntity user = securityService.getCurrentUser();
		Long orgId = securityService.getCurrentUserOrganizationId();
		boolean isNasnavAdmin = securityService.currentUserHasRole(NASNAV_ADMIN);

		Optional<MetaOrderEntity> order = Optional.empty();;

		if (user instanceof UserEntity) {
			order = metaOrderRepo.findByIdAndUserIdAndOrganization_Id(orderId, user.getId(), orgId);
		} else if (isNasnavAdmin) {
			order = metaOrderRepo.findByMetaOrderId(orderId);
		} else {
			order = metaOrderRepo.findByIdAndOrganization_Id(orderId, orgId);
		}

		if (order.isPresent()) {
			return getOrderResponse(order.get());
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

	




	private SubOrder getSubOrder(OrdersEntity order, Map<Long, Optional<String>> variantsCoverImages) {

		String status = 
				ofNullable(findEnum(order.getStatus()))
				.orElse(NEW)
				.name();
		
		List<BasketItem> items =
				order
				.getBasketsEntity()
				.stream()
				.map(b -> toBasketItem(b, variantsCoverImages))
				.collect(toList());
		
		Shipment shipmentDto = (Shipment) order.getShipment().getRepresentation();
		
		Long totalQuantity = 
				items
				.stream()
				.map(BasketItem::getQuantity)
				.reduce(0, Integer::sum)
				.longValue();
		
		SubOrder subOrder = new SubOrder();
		subOrder.setShopId(order.getShopsEntity().getId());
		subOrder.setShopName(order.getShopsEntity().getName());
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
		return subOrder;
	}


	private void validateCartCheckoutDTO(CartCheckoutDTO dto){
		if (dto.getAddressId() == null) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0004);
		}
		if (dto.getServiceId() == null) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CHK$0002);
		}
	}


	private MetaOrderEntity createOrder(Map<Long, List<CartCheckoutData>> shopCartsMap, AddressesEntity address, CartCheckoutDTO dto) {
		OrganizationEntity org = securityService.getCurrentUserOrganization();
		UserEntity user = (UserEntity)securityService.getCurrentUser();
		Optional<PromotionsEntity> promotion= 
				ofNullable(dto.getPromoCode())
				.flatMap(promoCode -> 
							promoRepo
							.findByCodeAndOrganization_IdAndActiveNow(promoCode, org.getId()));
		
		List<CartItemsForShop> cartDividedByShop = groupCartItemsByShop(shopCartsMap);
		Set<OrdersEntity> subOrders = createSubOrders(cartDividedByShop, address, dto);
		
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


	private List<CartItemsForShop> groupCartItemsByShop(Map<Long, List<CartCheckoutData>> shopCartsMap) {
		OrganizationEntity org = securityService.getCurrentUserOrganization();
		Map<Long,ShopsEntity> shopCache = createOrganizationShopsCache(org);
		return shopCartsMap
				.entrySet()
				.stream()
				.map(entry -> getCartItemsForShop(entry, shopCache))
				.collect(toList());
	}


	

	private CartItemsForShop getCartItemsForShop(Map.Entry<Long, List<CartCheckoutData>> entry, Map<Long,ShopsEntity> shopCache) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		ShopsEntity shop = 
				ofNullable(entry.getKey())
				.map(shopCache::get)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, S$0005 , entry.getKey(), orgId));
		return new CartItemsForShop(shop, entry.getValue());
	}


	private Map<Long, ShopsEntity> createOrganizationShopsCache(OrganizationEntity org) {
		return shopsRepo
		.findByOrganizationEntity_IdAndRemoved(org.getId(), 0)
		.stream()
		.collect(toMap(ShopsEntity::getId, shop -> shop));
	}





	private Set<OrdersEntity> createSubOrders(List<CartItemsForShop> cartDividedByShop, AddressesEntity address,
			CartCheckoutDTO dto) {
		Set<OrdersEntity> subOrders = 
				cartDividedByShop
				.stream()
				.map(cartItems -> createSubOrder(cartItems, address, dto))
				.collect(toSet());
		
		List<ShippingOfferDTO> shippingOffers =
				subOrders
				.stream()
				.map(subOrder -> shippingMgrService.createShippingDetailsFromOrder(subOrder, dto.getAdditionalData()))
				.collect(collectingAndThen(toList(), shippingMgrService::getOffersFromOrganizationShippingServices));
		
		addPromoDiscounts(dto, subOrders);
		
		for(OrdersEntity subOrder : subOrders) {
			subOrder.setShipment(createShipment(subOrder, dto, shippingOffers));
			subOrder.setTotal(calculateTotal(subOrder));
		}
		return subOrders;
	}





	private void addPromoDiscounts(CartCheckoutDTO dto, Set<OrdersEntity> subOrders) {
		BigDecimal subTotal = 
				subOrders
				.stream()
				.map(OrdersEntity::getAmount)
				.reduce(ZERO, BigDecimal::add);
		
		BigDecimal promoDiscount = 
				ofNullable(dto)
				.map(CartCheckoutDTO::getPromoCode)
				.map(promo -> promoService.calcPromoDiscount(promo, subTotal))
				.orElse(ZERO);
		
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





	private OrdersEntity createSubOrder(CartItemsForShop cartItems,
								 AddressesEntity shippingAddress, CartCheckoutDTO dto) {
		Map<Long, StocksEntity> stocksCache = createStockCache(cartItems);
		
		OrdersEntity subOrder =  createSubOrder(shippingAddress, cartItems);
		saveOrderItemsIntoSubOrder(cartItems, stocksCache, subOrder);
		subOrder.setAmount(calculateSubTotal(subOrder));
		return ordersRepository.save(subOrder);
	}





	private void saveOrderItemsIntoSubOrder(CartItemsForShop cartItems, Map<Long, StocksEntity> stocksCache,
			OrdersEntity subOrder) {
		cartItems
		.getCheckOutData()
		.stream()
		.map(data -> createBasketItem(data, subOrder, stocksCache))
		.forEach(subOrder::addBasketItem);
	}





	private BigDecimal calculateSubTotal(OrdersEntity subOrder) {
		return subOrder
				.getBasketsEntity()
				.stream()
				.map(this::calcBasketItemValue)
				.reduce(ZERO, BigDecimal::add);
	}
	
	
	
	private BigDecimal calcBasketItemValue(BasketsEntity item) {
		return item.getPrice().multiply(item.getQuantity());
	}





	private Map<Long, StocksEntity> createStockCache(CartItemsForShop cartItems) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		List<Long> itemStocks = 
				cartItems
				.getCheckOutData()
				.stream()
				.map(CartCheckoutData::getStockId)
				.collect(toList());
		
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

	
	

	private BasketsEntity createBasketItem(CartCheckoutData data, OrdersEntity subOrder, Map<Long, StocksEntity> stocksCache) {
		StocksEntity stock = 
				ofNullable(data)
				.map(CartCheckoutData::getStockId)
				.map(stocksCache::get)
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, P$STO$0001, data.getStockId()));
		
		if(stock.getQuantity() < data.getQuantity()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0003);
		}
		BigDecimal discount = ofNullable(data.getDiscount()).orElse(ZERO);
		BigDecimal totalPrice = data.getPrice().subtract(discount);
		BasketsEntity basket = new BasketsEntity();
		basket.setStocksEntity(stock);
		basket.setPrice(totalPrice);
		basket.setQuantity(new BigDecimal(data.getQuantity()));
		basket.setCurrency(data.getCurrency());
		basket.setDiscount(data.getDiscount());
		basket.setOrdersEntity(subOrder);

		return basket;
	}


	private OrdersEntity createSubOrder(AddressesEntity shippingAddress,
										CartItemsForShop cartItems) {
		UserEntity user = (UserEntity) securityService.getCurrentUser();
		OrganizationEntity org = securityService.getCurrentUserOrganization();

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


	private BigDecimal calculateDiscounts(CartItemsForShop cartItems) {
		return cartItems
				.getCheckOutData()
				.stream()
				.map(item -> calculateCartItemDiscount(item))
				.reduce(ZERO, BigDecimal::add);
	}





	private BigDecimal calculateCartItemDiscount(CartCheckoutData item) {
		BigDecimal quantity = 
				ofNullable(item)
				.map(CartCheckoutData::getQuantity)
				.map(BigDecimal::new)
				.orElse(ZERO);
		return ofNullable(item.getDiscount())
				.orElse(ZERO)
				.multiply(quantity);
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
		shipment.setFrom(shipmentDTO.getEta().getFrom());
		shipment.setTo(shipmentDTO.getEta().getTo());

		return shipmentRepo.save(shipment);
	}


	private void validateCartCheckoutItems(List<CartCheckoutData> userCartItems) {
		Long orgId = securityService.getCurrentUserOrganizationId();
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
			if (!Objects.equals(item.getOrganizationId(), orgId)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0005);
			}
		}
	}

	public OrderValue getMetaOrderTotalValue(long metaOrderId) {
		OrderService.OrderValue oValue = new OrderService.OrderValue();
		Optional<MetaOrderEntity> metaOrder = metaOrderRepo.findById(metaOrderId);
		if (!metaOrder.isPresent()) {
			return null;
		}
		oValue.amount = metaOrder.get().getGrandTotal();
		oValue.currency = getOrderCurrency(metaOrder.get());
		return oValue;
	}


	


	private Cart replaceCart(Cart newCart) {
		BaseUserEntity user = securityService.getCurrentUser();
		cartItemRepo.deleteByUser_Id(user.getId());
		return saveCart(newCart);
	}
	
	
	
	
	
	
	//TODO: this implementation should be more efficient in accessing the database and 
	//addCartItem should be the one depending on it instead.
	//also it can be the foundation for POST /cart api that saves the whole cart at once.
	private Cart saveCart(Cart cart) {
		BaseUserEntity user = securityService.getCurrentUser();
		return cart
				.getItems()
				.stream()
				.map(this::addCartItem)
				.reduce((first, second) -> second)
				.orElseGet(() -> getUserCart(user.getId()));
	}


	
	
	@Override
	public List<ShopFulfillingCart> getShopsThatCanProvideCartItems(){
		Long userId = securityService.getCurrentUser().getId();
		return cartItemRepo
				.getAllCartStocks(userId)
				.stream()
				.collect(groupingBy(CartItemStock::getShopId))
				.entrySet()
				.stream()
				.map(this::createShopFulfillingCart)
				.collect(toList());
	}
	
	
	
	
	
	@Override
	public List<ShopFulfillingCart> getShopsThatCanProvideWholeCart(){
		//it uses an additional query but gives more insurance than calculating variants from
		//cartItemsStocks
		Set<Long> cartItemVariants =
				getCart()
				.getItems()
				.stream()
				.map(CartItem::getVariantId)
				.collect(toSet());
		return getShopsThatCanProvideCartItems()
				.stream()
				.filter(shop -> hasAllCartVariants(shop, cartItemVariants))
				.collect(toList());
	}



	private boolean hasAllCartVariants(ShopFulfillingCart shop, Set<Long> cartItemVariants) {
		List<Long> shopCartVariants =
				ofNullable(shop)
				.map(ShopFulfillingCart::getCartItems)
				.orElse(emptyList())
				.stream()
				.map(CartItemStock::getVariantId)
				.collect(toList());
		return cartItemVariants
				.stream()
				.allMatch(shopCartVariants::contains);
	}




	@Override
	public BigDecimal calculateCartTotal() {
		return  calculateCartTotal(getCart());
	}
	
	
	
	
	
	private ShopFulfillingCart createShopFulfillingCart(
				Map.Entry<Long, List<CartItemStock>> shopWithStocks) {
		Long shopId = shopWithStocks.getKey();
		List<CartItemStock> itemStocks = shopWithStocks.getValue();
		Long cityId = 
				itemStocks
				.stream()
				.map(CartItemStock::getShopCityId)
				.findFirst()
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, ADDR$ADDR$0005));
		return new ShopFulfillingCart(shopId, cityId, itemStocks); 
	}
	
	
	
	
	private BigDecimal calculateCartTotal(Cart cart) {
		return ofNullable(cart)
				.map(Cart::getItems)
				.map(this::calculateCartTotal)
				.orElse(ZERO);
	}
	
	
	
	
	private BigDecimal calculateCartTotal(List<CartItem> cartItems) {
		return  cartItems
				.stream()
				.map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
				.reduce(ZERO, BigDecimal::add);
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
		
		String to = subOrder.getMetaOrder().getUser().getEmail();
		String subject = ORDER_REJECT_SUBJECT;
		List<String> bcc = getOrganizationManagersEmails(subOrder);
		Map<String,Object> parametersMap = createRejectionEmailParams(subOrder, rejectionReason);
		String template = ORDER_REJECT_TEMPLATE;
		try {
			mailService.sendThymeleafTemplateMail(asList(to), subject, emptyList(), bcc, template, parametersMap);
		} catch (IOException | MessagingException e) {
			logger.error(e, e);
		}
	}


	private void sendRejectionEmailToCustomer(ReturnRequestEntity request, String rejectionReason, Long orgId) {

		String to = request.getMetaOrder().getUser().getEmail();
		String subject = ORDER_RETURN_REJECT_SUBJECT;
		List<String> bcc = empRoleRepo.findEmailOfEmployeeWithRoleAndOrganization(ORGANIZATION_MANAGER.getValue(), orgId);
		Map<String,Object> parametersMap = createRejectionEmailParams(request, rejectionReason);
		String template = ORDER_RETURN_REJECT_TEMPLATE;
		try {
			mailService.sendThymeleafTemplateMail(asList(to), subject, emptyList(), bcc, template, parametersMap);
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
			mailService.sendThymeleafTemplateMail(to, subject, cc, template, parametersMap);
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

		List<BasketItemDetails> basketItemsDetails =
				getBasketItemsDetailsMap( setOf(order.getId()) )
						.get(order.getId());

		SubOrder subOrder = getSubOrder(order, getVariantsImagesList(basketItemsDetails));
		changeShippingServiceName(subOrder);

		String domain = domainService.getCurrentServerDomain();
		String orgLogo = domain + "/files/" + getOrganizationLogo(order.getOrganizationEntity());

		params.put("domain", domain);
		params.put("orgLogo", orgLogo);
		params.put("updateTime", updateTime);
		params.put("orderPageUrl", buildDashboardPageUrl(order));
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


	@Override
	@Transactional
	public void rejectReturnRequest(ReturnRequestRejectDTO dto) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		ReturnRequestEntity returnRequest =
				returnRequestRepo
				.findByIdAndOrganizationId(dto.getReturnRequestId(), orgId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0017, dto.getReturnRequestId()));

		updateReturnRequestStatus(returnRequest, REJECTED);

		sendRejectionEmailToCustomer(returnRequest, dto.getRejectionReason(), orgId);
	}




	@Override
	@Transactional
	public void receiveItems(ReceivedItemsDTO returnedItemsDTO) {

		validateReturnedItemsDTO(returnedItemsDTO);

		List<ReceivedItem> returnRequestItems = returnedItemsDTO.getReturnedItems();
		List<ReceivedBasketItem> returnBasketItems = returnedItemsDTO.getBasketItems();

		ReturnRequestEntity returnRequest;

		if (isOnlyReturnedBasketItemsProvided(returnRequestItems, returnBasketItems)) {
			List<ReturnRequestBasketItem> items = getReturnRequestBasketItemList(returnedItemsDTO);
			returnRequest = createReturnRequest(items);
		}else {
			List<ReturnRequestItemEntity> returnRequestItemEntities =
					getAndValidateReturnRequestItemEntities(returnRequestItems);
			ReturnRequestEntity requestEntity = returnRequestItemEntities.get(0).getReturnRequest();
			assignReturnBasketItemsToReturnRequest(requestEntity, returnBasketItems);
			returnRequest = receiveReturnRequestItems(returnRequestItems, returnRequestItemEntities);
		}

		if (returnRequest != null) {
            returnRequest.setStatus(RECEIVED.getValue());
            returnRequest = returnRequestRepo.save(returnRequest);
			increaseReturnRequestStock(returnRequest);

			sendItemReceivedEmailToCustomer(returnRequest.getId());
			//TODO refund ??
		}
	}



	private void sendItemReceivedEmailToCustomer(Long returnRequestId) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		ReturnRequestEntity request =
				returnRequestRepo
						.findByReturnRequestId(returnRequestId, orgId)
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0017, returnRequestId));
		Optional<String> email =
				ofNullable(request)
						.map(ReturnRequestEntity::getMetaOrder)
						.map(MetaOrderEntity::getUser)
						.map(UserEntity::getEmail);
		if(!email.isPresent()) {
			return;
		}
		String subject = format(ORDER_RETURN_RECEIVE_SUBJECT);
		Map<String,Object> parametersMap = createReturnItemsReceiptionEmailParams(request);
		String template = ORDER_RETURN_RECEIVED_TEMPLATE;
		try {
			mailService.sendThymeleafTemplateMail(email.get(), subject,  template, parametersMap);
		} catch (MessagingException e) {
			logger.error(e, e);
		}
	}




	private Map<String, Object> createReturnItemsReceiptionEmailParams(ReturnRequestEntity request) {
		Optional<UserEntity> user =  getCustomerName(request);
		String userName = user.map(UserEntity::getFirstName).orElse("Dear Customer");
		String creationDate =
				DateTimeFormatter.ofPattern("dd/MM/YYYY hh:mm").format(request.getCreatedOn());

		Optional<OrganizationEntity> org =
				ofNullable(request)
						.map(ReturnRequestEntity::getMetaOrder)
						.map(MetaOrderEntity::getOrganization);
		String domain = appConfig.environmentHostName;
		String orgLogo = domain + "/files/" + getOrganizationLogo(org);
		String orgDomain = domainService.getOrganizationDomainAndSubDir(org.get().getId());
		String orgName = org.map(OrganizationEntity::getName).orElse("Nasnav");
		String shippingService = getShippingService(request);
		AddressRepObj pickupAddr = getPickupAddress(request);
		String phone =
				ofNullable(pickupAddr)
					.map(AddressRepObj::getPhoneNumber)
						.orElseGet(() -> user.map(UserEntity::getPhoneNumber).orElse(""));

		List<ReturnShipment> returnShipmentsData = getReturnShipmentsData(request);

		Map<String, Object> params = new HashMap<>();
		params.put("orgDomain", orgDomain);
		params.put("orgLogo", orgLogo);
		params.put("orgName", orgName);
		params.put("userName", userName);
		params.put("requestId", request.getId());
		params.put("creationDate", creationDate);
		params.put("pickupAddr", pickupAddr);
		params.put("phone", phone);
		params.put("shippingService", shippingService);
		params.put("returnShipments", returnShipmentsData);
		return params;
	}




	private List<ReturnRequestBasketItem> getReturnRequestBasketItemList(ReceivedItemsDTO returnedItemsDTO) {
		return ofNullable(returnedItemsDTO.getBasketItems())
				.orElse(emptyList())
				.stream()
				.map(item -> new ReturnRequestBasketItem(item.getOrderItemId(), item.getReceivedQuantity(), item.getReceivedQuantity()))
				.collect(toList());
	}


	private void validateReturnedItemsDTO(ReceivedItemsDTO returnedItemsDTO) {
		boolean hasReturnItems = !isNullOrEmpty(returnedItemsDTO.getReturnedItems());
		boolean hasBasketItems = !isNullOrEmpty(returnedItemsDTO.getBasketItems());
		boolean hasEitherReturnItemsOrBasketItems =  hasReturnItems ^  hasBasketItems;
		if (!hasEitherReturnItemsOrBasketItems) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0003);
		}
		if (hasReturnItems) {
			validateReturnedItemsList(returnedItemsDTO.getReturnedItems());
		}
		if (hasBasketItems) {
			validateReturnedBasketItem(returnedItemsDTO.getBasketItems());
		}
	}

	

	private void validateReturnedItemsList(List<ReceivedItem> returnRequestItems) {
		for(ReceivedItem item : returnRequestItems) {
			if (Objects.equals(item.getReturnRequestItemId(), null)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0004);
			}
			if (Objects.equals(item.getReceivedQuantity(), null)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0005);
			}
		}
	}

	
	

	private void validateReturnedBasketItem(List<ReceivedBasketItem> returnBasketItems) {
		for(ReceivedBasketItem item : returnBasketItems) {
			if (Objects.equals(item.getOrderItemId(), null)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0006);
			}
			if (Objects.equals(item.getReceivedQuantity(), null)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0007);
			}
		}
	}


	
	
	private Map<Long, BasketsEntity> getBasketsMap(List<Long> ids) {
		if(ids.isEmpty()){
			return emptyMap();
		}
		Long orgId = securityService.getCurrentUserOrganizationId();
		return basketRepository
				.findByIdIn(ids, orgId)
				.stream()
				.collect(toMap(BasketsEntity::getId, b -> b));
	}


	
	
	private void validateBasketsItemsMetaOrder(Collection<BasketsEntity> basketsEntities, MetaOrderEntity metaOrder) {
		validateBasketIemsFromSameMetaOrder(basketsEntities, metaOrder);
		validateItemsBelongToCustomer(metaOrder);
		validateBasketItemsAreReturnable(basketsEntities, metaOrder);
	}



	private void validateBasketItemsAreReturnable(Collection<BasketsEntity> basketsEntities, MetaOrderEntity metaOrder) {
		boolean allReturnable =
				basketsEntities
				.stream()
				.allMatch(this::isReturnable);
		if(!allReturnable){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0022);
		}
	}




	private boolean isReturnable(BasketsEntity basketsEntity) {
		return  isWithinReturnTimeWindow(basketsEntity)
					&& hasProperOrderStatusForReturn(basketsEntity);
	}



	private boolean isWithinReturnTimeWindow(BasketsEntity basketsEntity) {
		LocalDateTime orderCreationTime =
				ofNullable(basketsEntity)
				.map(BasketsEntity::getOrdersEntity)
				.map(OrdersEntity::getMetaOrder)
				.map(MetaOrderEntity::getCreatedAt)
				.orElse(LocalDateTime.MIN);
		Long orderAge = Duration.between(orderCreationTime, now()).toDays();
		return orderAge <= MAX_RETURN_TIME_WINDOW;
	}



	private Boolean hasProperOrderStatusForReturn(BasketsEntity basketsEntity) {
		return ofNullable(basketsEntity)
				.map(BasketsEntity::getOrdersEntity)
				.map(OrdersEntity::getStatus)
				.map(OrderStatus::findEnum)
				.map(metaOrderAcceptableStatusForReturn::contains)
				.orElse(false);
	}


	private void validateItemsBelongToCustomer(MetaOrderEntity metaOrder) {
		Long orderCreatorId = metaOrder.getUser().getId();
		BaseUserEntity user = securityService.getCurrentUser();
		if(user instanceof  UserEntity && !Objects.equals( orderCreatorId, user.getId())){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0015);
		}
	}

	

	private void validateBasketIemsFromSameMetaOrder(Collection<BasketsEntity> basketsEntities, MetaOrderEntity metaOrder) {
		boolean hasSameMetaOrder =
				basketsEntities
				.stream()
				.map(BasketsEntity::getOrdersEntity)
				.map(OrdersEntity::getMetaOrder)
				.allMatch(m -> m.getId().equals(metaOrder.getId()));
		if (!hasSameMetaOrder) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0009);
		}
	}



	
	private void validateAllBasketsExisting(List<Long> ids, Map<Long, BasketsEntity> basketsMap) {
		Set<Long> fetchedBasketsIds = basketsMap.keySet();
		ids.removeAll(fetchedBasketsIds);
		if (!ids.isEmpty()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0008, ids.toString());
		}
	}


	private boolean isOnlyReturnedItemsProvided(List<ReceivedItem> returnRequestItems, List<ReceivedBasketItem> returnBasketItems) {
		return !isNullOrEmpty(returnRequestItems) && isNullOrEmpty(returnBasketItems);
	}



	private boolean isOnlyReturnedBasketItemsProvided(List<ReceivedItem> returnRequestItems, List<ReceivedBasketItem> returnBasketItems) {
		return isNullOrEmpty(returnRequestItems) && !isNullOrEmpty(returnBasketItems);
	}



	private boolean areBothReturnedItemsAndBasketItemsProvided(List<ReceivedItem> returnRequestItems, List<ReceivedBasketItem> returnBasketItems) {
		return !isNullOrEmpty(returnRequestItems) && !isNullOrEmpty(returnBasketItems);
	}

	
	
	
	
	private ReturnRequestEntity assignReturnBasketItemsToReturnRequest(ReturnRequestEntity requestEntity,
																			List<ReceivedBasketItem> receivedBasketItems) {
		if(receivedBasketItems.isEmpty()){
			return requestEntity;
		}
		MetaOrderEntity metaOrder = requestEntity.getMetaOrder();
		Map<Long, BasketsEntity> basketsCache = createOrderItemsEntityCache(receivedBasketItems);
		receivedBasketItems
				.stream()
				.map(this::createReturnRequestBasketItem)
				.collect(collectingAndThen(
							toList()
						, items -> createAndValidateReturnItemEntities(items, basketsCache, metaOrder)))
				.forEach(requestEntity::addItem);

		return requestEntity;
	}




	private Map<Long, BasketsEntity> createOrderItemsEntityCache(List<ReceivedBasketItem> receivedBasketItems) {
		List<Long> returnBasketIds =
				receivedBasketItems
				.stream()
				.map(ReceivedBasketItem::getOrderItemId)
				.collect(toList());

		Map<Long, BasketsEntity> basketsCache = getBasketsMap(returnBasketIds);
		return basketsCache;
	}


	private ReturnRequestBasketItem createReturnRequestBasketItem(ReceivedBasketItem item) {
		Integer receivecedQty = item.getReceivedQuantity();
		return new ReturnRequestBasketItem(item.getOrderItemId(), receivecedQty, receivecedQty);
	}



	private List<ReturnRequestItemEntity> getAndValidateReturnRequestItemEntities (List<ReceivedItem> returnRequestItems) {
		List<Long> returnItemsIds = returnRequestItems
				.stream()
				.map(ReceivedItem::getReturnRequestItemId)
				.collect(toList());

		List<ReturnRequestItemEntity> returnRequestItemEntities = returnRequestItemRepo.findByIdIn(returnItemsIds);
		validateAllReturnItemsExisting(returnItemsIds, returnRequestItemEntities);
		validateAllReturnItemsHasSameReturnRequest(returnRequestItemEntities);
		validateAllReturnItemsAreNotReceivedBefore(returnRequestItemEntities);
		return returnRequestItemEntities;
	}




	private void validateAllReturnItemsAreNotReceivedBefore(List<ReturnRequestItemEntity> items) {
		boolean someItemAlreadyReceived =
				!items
				.stream()
				.allMatch(item -> allIsNull(item.getReceivedBy(), item.getReceivedOn()));
		boolean isStoreManager = securityService.currentUserHasRole(STORE_MANAGER);
		if(someItemAlreadyReceived && isStoreManager){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0020);
		}
	}



	private void validateAllReturnItemsExisting(List<Long> ids, List<ReturnRequestItemEntity> returnRequestItemEntities) {
		List<Long> existingIds = returnRequestItemEntities
				.stream()
				.map(ReturnRequestItemEntity::getId)
				.collect(toList());

		ids.removeAll(existingIds);

		if(!ids.isEmpty()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0001, ids.toString());
		}
	}

	
	
	

	private void validateAllReturnItemsHasSameReturnRequest(List<ReturnRequestItemEntity> returnRequestItemEntities) {
		if(!returnRequestItemEntities.isEmpty()) {
			ReturnRequestEntity returnRequestEntity = returnRequestItemEntities
											.stream()
											.findFirst()
											.get()
											.getReturnRequest();

			boolean hasSameReturnRequest = returnRequestItemEntities
											.stream()
											.map(ReturnRequestItemEntity::getReturnRequest)
											.allMatch(r -> Objects.equals(r,returnRequestEntity));
			if(!hasSameReturnRequest) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0010);
			}
		}
	}


	
	
	private ReturnRequestEntity createReturnRequest(List<ReturnRequestBasketItem> returnedItems) {
		List<Long> returnBasketIds =
				returnedItems
						.stream()
						.map(ReturnRequestBasketItem::getOrderItemId)
						.collect(toList());
		Map<Long, BasketsEntity> basketsMap = getBasketsMap(returnBasketIds);

		MetaOrderEntity metaOrder = getMetaOrderFromBasketItems(returnBasketIds, basketsMap);
		validateMetaOrderForReturn(metaOrder);

		List<ReturnRequestItemEntity> returnItemsEntities =
				createAndValidateReturnItemEntities(returnedItems, basketsMap, metaOrder);

		ReturnRequestEntity returnRequest = new ReturnRequestEntity();
		returnRequest.setMetaOrder(metaOrder);
		returnRequest.setStatus(ReturnRequestStatus.NEW.getValue());
		returnItemsEntities.forEach(returnRequest::addItem);
		setReturnRequestCreator(returnRequest);

		return returnRequest;
	}



	private void validateMetaOrderForReturn(MetaOrderEntity metaOrder) {
		OrderStatus status = OrderStatus.findEnum(metaOrder.getStatus());
		boolean hasProperStatus =
				metaOrderAcceptableStatusForReturn
				.contains(status);
		if(!hasProperStatus){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0021, status.name());
		}
	}


	private MetaOrderEntity getMetaOrderFromBasketItems(List<Long> returnBasketIds, Map<Long, BasketsEntity> basketsMap) {
		return basketsMap
				.values()
				.stream()
				.filter(Objects::nonNull)
				.findFirst()
				.map(BasketsEntity::getOrdersEntity)
				.map(OrdersEntity::getMetaOrder)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0008, returnBasketIds.toString()));
	}




	private List<ReturnRequestItemEntity> createAndValidateReturnItemEntities(
			List<ReturnRequestBasketItem> returnedItems, Map<Long, BasketsEntity> basketsCache, MetaOrderEntity metaOrder) {
		List<Long> returnBasketIds =
				returnedItems
						.stream()
						.map(ReturnRequestBasketItem::getOrderItemId)
						.collect(toList());

		validateReturnBasketItemsIsNew(returnBasketIds);
		validateReturnBasketItemQuantity(basketsCache, returnedItems);
		validateBasketsItemsMetaOrder(basketsCache.values(), metaOrder);
		validateAllBasketsExisting(returnBasketIds, basketsCache);

		return returnedItems
					.stream()
					.map(item -> createReturnRequestItemEntity(item, basketsCache))
					.collect(toList());
	}



	private void setReturnRequestCreator(ReturnRequestEntity returnRequest) {
		BaseUserEntity user = securityService.getCurrentUser();
		if(user instanceof EmployeeUserEntity) {
			EmployeeUserEntity emp = (EmployeeUserEntity)user;
			returnRequest.setCreatedByEmployee(emp);
		}else if(user instanceof UserEntity) {
			UserEntity customer = (UserEntity)user;
			returnRequest.setCreatedByUser(customer);
		}
	}
	
	


	private void validateReturnBasketItemsIsNew(List<Long> basketItemsIds) {
		List<ReturnRequestItemEntity> items = returnRequestItemRepo.findByBasket_IdIn(basketItemsIds);
		if (!items.isEmpty())
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0012, items.get(0).getBasket().getId());
	}

	
	
	
	private ReturnRequestItemEntity createReturnRequestItemEntity(ReturnRequestBasketItem itemDto
				, Map<Long,BasketsEntity> basketCache) {
		Integer returnedQty = itemDto.getReturnedQuantity();
		Integer receivedQty = itemDto.getReceivedQuantity();
		BasketsEntity basket =
				ofNullable(itemDto.getOrderItemId())
				.map(basketCache::get)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0008));

		ReturnRequestItemEntity item = new ReturnRequestItemEntity();
		item.setBasket(basket);
		item.setReturnedQuantity(returnedQty);

		BaseUserEntity user = securityService.getCurrentUser();
		if(securityService.currentUserIsCustomer()) {
			UserEntity customer = (UserEntity)user;
			item.setCreatedByUser(customer);
		}else if(user instanceof EmployeeUserEntity) {
			EmployeeUserEntity emp = (EmployeeUserEntity)user;
			item.setCreatedByEmployee(emp);
			item.setReceivedBy(emp);
			item.setReceivedOn(now());
			item.setReceivedQuantity(receivedQty);
		}

		return item;
	}



	private void increaseReturnRequestStock(ReturnRequestEntity requestEntity) {
		for(ReturnRequestItemEntity item : requestEntity.getReturnedItems()) {
			Integer receivedQuantity = item.getReceivedQuantity();
			StocksEntity stock = item.getBasket().getStocksEntity();
			stockService.incrementStockBy(stock, receivedQuantity);
		}
	}




	private ReturnRequestEntity receiveReturnRequestItems(List<ReceivedItem> returnRequestItems
							,List<ReturnRequestItemEntity> returnRequestItemEntities) {
		Map<Long, ReturnRequestItemEntity> returnRequestItemEntityMap =
				returnRequestItemEntities
				.stream()
				.collect(toMap(ReturnRequestItemEntity::getId, e -> e));

		validateReturnRequestItemQuantity(returnRequestItemEntityMap, returnRequestItems);

		for (ReceivedItem item : returnRequestItems) {
			ReturnRequestItemEntity itemEntity = returnRequestItemEntityMap.get(item.getReturnRequestItemId());
			BaseUserEntity emp = securityService.getCurrentUser();

			itemEntity.setReceivedBy((EmployeeUserEntity)emp);
			itemEntity.setReceivedOn(now());
			itemEntity.setReceivedQuantity(item.getReceivedQuantity());
		}
		return returnRequestItemEntities.get(0).getReturnRequest();
	}


	
	private void validateReturnRequestItemQuantity(Map<Long, ReturnRequestItemEntity> requestItemEntityMap, List<ReceivedItem> returnedItems) {
		for (ReceivedItem item : returnedItems) {
			Integer basketQuantity = requestItemEntityMap.get(item.getReturnRequestItemId()).getBasket().getQuantity().intValue();
			if(basketQuantity < item.getReceivedQuantity()) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0002);
			}
		}
	}

	
	
	
	private void validateReturnBasketItemQuantity(Map<Long, BasketsEntity> basketsMap, List<ReturnRequestBasketItem> returnedBasketItems) {
		for (ReturnRequestBasketItem item : returnedBasketItems) {
			Long id = item.getOrderItemId();
			Integer returnedQty =  item.getReturnedQuantity();
			Integer basketQuantity = 
					ofNullable(basketsMap.get(id))
					.map(BasketsEntity::getQuantity)
					.map(BigDecimal::intValue)
					.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0014, id));
			
			if(returnedQty <= 0) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0013);
			}else if(basketQuantity < returnedQty) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0002);
			}
		}
	}





	@Override
	public Long createReturnRequest(ReturnRequestItemsDTO itemsList) {
		List<ReturnRequestBasketItem> returnedItems =
				itemsList
				.getItemList()
				.stream()
				.map(item -> new ReturnRequestBasketItem(item.getOrderItemId(), item.getReturnedQuantity(), 0))
				.collect(toList());
		ReturnRequestEntity request = createReturnRequest(returnedItems);
		request = returnRequestRepo.save(request);

		sendOrderReturnNotificationEmail(request.getId());
		return request.getId();
	}



	@Override
	public ReturnRequestsResponse getOrderReturnRequests(ReturnRequestSearchParams params) {
		setOrderReturnDefaultParams(params);
		CriteriaBuilder builder = em.getCriteriaBuilder();
		return getReturnRequestCriteriaQuery(params, builder);
	}



	private void setOrderReturnDefaultParams(ReturnRequestSearchParams params) {
		if(params.getStart() == null || params.getStart() < 0){
			params.setStart(0);
		}
		if(params.getCount() == null || (params.getCount() < 1)){
			params.setCount(10);
		} else if (params.getCount() > 1000) {
			params.setCount(1000);
		}
	}


	private Long getOrderReturnRequestsCount(CriteriaBuilder builder, Predicate[] predicatesArr) {
		CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
		countQuery.select(  builder.count(
										(Expression<?>) countQuery
												.from(ReturnRequestEntity.class)
												.join("returnedItems", INNER)
												.alias("generatedAlias1")) )
				  .where(predicatesArr);
		Long count = em.createQuery(countQuery).getSingleResult();
		return count;
	}


	public ReturnRequestDTO getOrderReturnRequest(Long id){
		Long orgId = securityService.getCurrentUserOrganizationId();
		ReturnRequestEntity returnRequestEntity =
				returnRequestRepo
				.findByReturnRequestId(id, orgId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0017, id));

		ReturnRequestDTO dto = (ReturnRequestDTO)returnRequestEntity.getRepresentation();

		Set<ReturnRequestItemDTO> requestItems = returnRequestEntity
				.getReturnedItems()
				.stream()
				.map(i -> (ReturnRequestItemDTO)i.getRepresentation())
				.collect(toSet());

		requestItems = setReturnRequestItemVariantsAdditionalData(requestItems);
		dto.setReturnedItems(requestItems);
		if (!requestItems.isEmpty()) {
			dto.setAddress(requestItems.stream().findFirst().get().getAddress());
		}
		return dto;
	}



	private Set<ReturnRequestItemDTO> setReturnRequestItemVariantsAdditionalData(Set<ReturnRequestItemDTO> requestItems) {
		List<Long> variantsIds = requestItems
				.stream()
				.map(ReturnRequestItemDTO::getVariantId)
				.collect(toList());

		Map<Long, Optional<String>> images = imgService.getVariantsCoverImages(variantsIds);
		for(ReturnRequestItemDTO dto : requestItems) {
			Map<String, String> variantFeatures = parseVariantFeatures(dto.getFeatureSpec(), 0);
			dto.setVariantFeatures(variantFeatures);
			Optional<String> image = images.get(dto.getVariantId());
			if(image.isPresent()){
				dto.setCoverImage(image.get());
			}
		}
		return requestItems;
	}






	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void confirmReturnRequest(Long id) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		ReturnRequestEntity request =
				returnRequestRepo
				.findByReturnRequestId(id, orgId)
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0017, id));
		updateReturnRequestStatus(request, CONFIRMED);
		List<ReturnShipmentTracker> trackers =
				shippingMgrService
				.requestReturnShipments(request)
				.collectList()
				.blockOptional()
				.orElseThrow( () -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, O$SHP$0004, request.getId()));
		sendReturnRequestConfirmationEmail(request, trackers);
	}



	private void sendReturnRequestConfirmationEmail(ReturnRequestEntity request, List<ReturnShipmentTracker> trackers) {
		Optional<String> email =
				ofNullable(request)
						.map(ReturnRequestEntity::getMetaOrder)
						.map(MetaOrderEntity::getUser)
						.map(UserEntity::getEmail);
		if(!email.isPresent()) {
			return;
		}
		String subject = format(ORDER_RETURN_CONFIRM_SUBJECT);
		Map<String,Object> parametersMap = createReturnConfirmEmailParams(request, trackers);
		List<MailAttachment> airwayBills = createReturnConfirmMailAttachments(trackers);
		String template = ORDER_RETURN_CONFIRM_TEMPLATE;
		try {
			mailService.sendThymeleafTemplateMail(email.get(), subject,  template, parametersMap, airwayBills);
		} catch (MessagingException e) {
			logger.error(e, e);
		}
	}




	private Map<String, Object> createReturnConfirmEmailParams(ReturnRequestEntity request, List<ReturnShipmentTracker> trackers) {
		Optional<UserEntity> user =  getCustomerName(request);
		String userName = user.map(UserEntity::getFirstName).orElse("Dear Customer");
		Optional<OrganizationEntity> org =
				ofNullable(request)
						.map(ReturnRequestEntity::getMetaOrder)
						.map(MetaOrderEntity::getOrganization);
		String domain = appConfig.environmentHostName;
		String orgLogo = domain + "/files/" + getOrganizationLogo(org);
		String orgDomain = domainService.getOrganizationDomainAndSubDir(org.get().getId());
		String orgName = org.map(OrganizationEntity::getName).orElse("Nasnav");
		String msg = getReturnConfirmationEmailMsg(trackers);
		String shippingService = getShippingService(request);
		AddressRepObj pickupAddr = getPickupAddress(request);
		String phone =
				ofNullable(pickupAddr)
						.map(AddressRepObj::getPhoneNumber)
						.orElseGet(() -> user.map(UserEntity::getPhoneNumber).orElse(""));
		List<ReturnShipment> returnShipmentsData = getReturnShipmentsData(request);

		Map<String, Object> params = new HashMap<>();
		params.put("domain", domain);
		params.put("orgDomain", orgDomain);
		params.put("orgLogo", orgLogo);
		params.put("orgName", orgName);
		params.put("userName", userName);
		params.put("requestId", request.getId());
		params.put("msg", msg);
		params.put("phone", phone);
		params.put("pickupAddr", pickupAddr);
		params.put("shippingService", shippingService);
		params.put("returnShipments", returnShipmentsData);
		return params;
	}



	private void sendOrderReturnNotificationEmail(Long returnRequestId) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		ReturnRequestEntity request =
				returnRequestRepo
						.findByReturnRequestId(returnRequestId, orgId)
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0017, returnRequestId));
		List<String> emails = empRoleRepo.findEmailOfEmployeeWithRoleAndOrganization(ORGANIZATION_MANAGER.getValue(), orgId);
		if(emails.isEmpty()) {
			return;
		}
		String subject = format(ORDER_RETURN_NOTIFY_SUBJECT, returnRequestId);
		Map<String,Object> parametersMap = createOrderReturnNotificationEmailParams(request);
		String template = ORDER_RETURN_NOTIFICATION_TEMPLATE;
		try {
			mailService.sendThymeleafTemplateMail(emails ,subject, emptyList(),  template, parametersMap);
		} catch (MessagingException | IOException e) {
			logger.error(e, e);
		}
	}




	private Map<String, Object> createOrderReturnNotificationEmailParams(ReturnRequestEntity request) {
		Optional<UserEntity> user =  getCustomerName(request);
		String userName = user.map(UserEntity::getFirstName).orElse("Dear Customer");
		String creationDate =
				DateTimeFormatter.ofPattern("dd/MM/YYYY hh:mm").format(request.getCreatedOn());

		Optional<OrganizationEntity> org =
				ofNullable(request)
						.map(ReturnRequestEntity::getMetaOrder)
						.map(MetaOrderEntity::getOrganization);

		String domain = appConfig.environmentHostName;
		String orgLogo = domain + "/files/" + getOrganizationLogo(org);
		String orgName = org.map(OrganizationEntity::getName).orElse("Nasnav");
		String shippingService = getShippingService(request);
		AddressRepObj pickupAddr = getPickupAddress(request);
		String returnOrderPageUrl = buildDashboardPageUrl(request);
		String phone =
				ofNullable(pickupAddr)
						.map(AddressRepObj::getPhoneNumber)
						.orElseGet(() -> user.map(UserEntity::getPhoneNumber).orElse(""));
		List<ReturnShipment> returnShipmentsData = getReturnShipmentsData(request);

		Map<String, Object> params = new HashMap<>();
		params.put("orgLogo", orgLogo);
		params.put("orgName", orgName);
		params.put("userName", userName);
		params.put("requestId", request.getId());
		params.put("creationDate", creationDate);
		params.put("pickupAddr", pickupAddr);
		params.put("phone", phone);
		params.put("shippingService", shippingService);
		params.put("returnShipments", returnShipmentsData);
		params.put("returnOrderPageUrl", returnOrderPageUrl);
		return params;
	}




	private List<ReturnShipment> getReturnShipmentsData(ReturnRequestEntity request) {
		Map<Long, Optional<String>> variantsCoverImages =
				ofNullable(request)
				.map(ReturnRequestEntity::getMetaOrder)
				.map(this::getVariantsImagesList)
				.orElse(emptyMap());

		List<ReturnShipment> returnShipmentsData =
				request
				.getReturnedItems()
				.stream()
				.collect(
						collectingAndThen(
								groupingBy(this::getTrackNumber)
								, itmsGroupedByShipment -> createReturnShipmentData(itmsGroupedByShipment, variantsCoverImages)));
		return returnShipmentsData;
	}




	private  String getTrackNumber(ReturnRequestItemEntity itm) {
		return ofNullable(itm)
				.map(ReturnRequestItemEntity::getReturnShipment)
				.map(ReturnShipmentEntity::getTrackNumber)
				.orElse("N/A");
	}





	private AddressRepObj getPickupAddress(ReturnRequestEntity request) {
		return ofNullable(request)
				.map(ReturnRequestEntity::getMetaOrder)
				.map(MetaOrderEntity::getSubOrders)
				.map(Set::stream)
				.flatMap(Stream::findFirst)
				.map(OrdersEntity::getAddressEntity)
				.map(AddressesEntity::getRepresentation)
				.map(AddressRepObj.class::cast)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$RET$0018));
	}




	private String getShippingService(ReturnRequestEntity request) {
		return request
				.getReturnedItems()
				.stream()
				.findFirst()
				.map(ReturnRequestItemEntity::getReturnShipment)
				.map(ReturnShipmentEntity::getShippingServiceId)
				.orElse("N/A");
	}



	private String getReturnConfirmationEmailMsg(List<ReturnShipmentTracker> trackers) {
		return trackers
				.stream()
				.map(ReturnShipmentTracker::getEmailMessage)
				.findFirst()
				.orElse("Thanks for you patience!");
	}




	private Optional<UserEntity> getCustomerName(ReturnRequestEntity request) {
		return ofNullable(request)
				.map(ReturnRequestEntity::getMetaOrder)
				.map(MetaOrderEntity::getUser);
	}



	private List<ReturnShipment> createReturnShipmentData(Map<String, List<ReturnRequestItemEntity>> groupedbByTrackNum
					, Map<Long, Optional<String>> variantsCoverImages) {
		return groupedbByTrackNum
				.entrySet()
				.stream()
				.map(ent -> toReturnShipment(ent.getKey(), ent.getValue(), variantsCoverImages))
				.collect(toList());
	}



	private ReturnShipment toReturnShipment(String trackNum, List<ReturnRequestItemEntity> itemEntities
					, Map<Long, Optional<String>> variantsCoverImages) {
		List<ReturnShipmentItem> items =
				itemEntities
				.stream()
				.map(itm -> toReturnShipmentItem(itm, variantsCoverImages))
				.collect(toList());
		ReturnShipment shipment = new ReturnShipment();
		shipment.setTrackNumber(trackNum);
		shipment.setItems(items);
		return shipment;
	}




	private ReturnShipmentItem toReturnShipmentItem(ReturnRequestItemEntity returnRequestItemEntity
					, Map<Long, Optional<String>> variantsCoverImages) {
		BasketsEntity orderItem = returnRequestItemEntity.getBasket();
		ProductVariantsEntity variant = orderItem.getStocksEntity().getProductVariantsEntity();
		ProductEntity product = variant.getProductEntity();
		BigDecimal price = orderItem.getPrice();
		String thumb = variantsCoverImages.get(variant.getId()).orElse("NA");
		String currency = ofNullable(getTransactionCurrency(orderItem.getCurrency())).orElse(EGP).name();

		ReturnShipmentItem item = new ReturnShipmentItem();
		item.setName(product.getName());
		item.setQuantity(returnRequestItemEntity.getReturnedQuantity());
		item.setReceivedQuantity(returnRequestItemEntity.getReceivedQuantity());
		item.setCurrency(currency);
		item.setPrice(price);
		item.setVariantFeatures(parseVariantFeatures(variant.getFeatureSpec(), 0));
		item.setThumb(thumb);
		item.setSku(variant.getSku());
		item.setProductCode(variant.getProductCode());
		return item;
	}




	private List<MailAttachment> createReturnConfirmMailAttachments(List<ReturnShipmentTracker> trackers) {
		Optional<Long> returnRequestId =
				trackers
					.stream()
					.map(ReturnShipmentTracker::getShippingDetails)
					.map(ShippingDetails::getReturnRequestId)
					.filter(Objects::nonNull)
					.findFirst();
		return range(0, trackers.size())
				.mapToObj(i -> doCreateReturnConfirmAirwaybill(i, trackers.get(i), trackers.size()))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());
	}



	private Optional<MailAttachment> doCreateReturnConfirmAirwaybill(int i, ReturnShipmentTracker tracker, Integer shipmentsCount) {
		String fileName =
				ofNullable(tracker)
				.map(ReturnShipmentTracker::getTracker)
				.map(trackNum -> format("airwaybill-%s.pdf", trackNum))
				.orElse("airwaybill.pdf");
		return ofNullable(tracker)
				.map(ReturnShipmentTracker::getAirwayBillFile)
				.map(Base64.getDecoder()::decode)
				.map(fileDecoded -> new MailAttachment(fileName, new ByteArrayResource(fileDecoded)));
	}
}


@Data
@AllArgsConstructor
class CartItemsForShop{
	private ShopsEntity shop;
	private List<CartCheckoutData> checkOutData;
}



class CartItemsGroupedByShopId extends HashMap<Long, List<CartCheckoutData>>{

	private static final long serialVersionUID = 166855415L;
	
	public CartItemsGroupedByShopId(Map<Long, List<CartCheckoutData>> map) {
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
