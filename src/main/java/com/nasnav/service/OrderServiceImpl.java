package com.nasnav.service;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.commons.utils.EntityUtils.collectionContainsAnyOf;
import static com.nasnav.commons.utils.EntityUtils.isNullOrEmpty;
import static com.nasnav.commons.utils.EntityUtils.isNullOrZero;
import static com.nasnav.commons.utils.MapBuilder.buildMap;
import static com.nasnav.constatnts.EmailConstants.ORDER_BILL_TEMPLATE;
import static com.nasnav.constatnts.EmailConstants.ORDER_NOTIFICATION_TEMPLATE;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_CALC_ORDER_FAILED;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_INVALID_ITEM_QUANTITY;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_INVALID_ORDER_STATUS;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_INVALID_ORDER_STATUS_UPDATE;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_MANAGER_CANNOT_CREATE_ORDERS;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_NEW_ORDER_WITH_EMPTY_BASKET;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_NON_EXISTING_STOCK_ID;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_NOT_EMP_ACCOUNT;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_NO_ENOUGH_STOCK;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_NULL_ITEM;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_ORDER_CONFIRMED_WITH_EMPTY_BASKET;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_ORDER_NOT_EXISTS;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_ORDER_NOT_OWNED_BY_ADMIN;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_ORDER_NOT_OWNED_BY_SHOP_MANAGER;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_ORDER_NOT_OWNED_BY_USER;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_ORDER_STATUS_NOT_ALLOWED_FOR_ROLE;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_UPDATED_ORDER_WITH_NO_ID;
import static com.nasnav.enumerations.OrderFailedStatus.INVALID_ORDER;
import static com.nasnav.enumerations.OrderStatus.CLIENT_CANCELLED;
import static com.nasnav.enumerations.OrderStatus.CLIENT_CONFIRMED;
import static com.nasnav.enumerations.OrderStatus.DELIVERED;
import static com.nasnav.enumerations.OrderStatus.DISPATCHED;
import static com.nasnav.enumerations.OrderStatus.NEW;
import static com.nasnav.enumerations.OrderStatus.STORE_CANCELLED;
import static com.nasnav.enumerations.OrderStatus.STORE_CONFIRMED;
import static com.nasnav.enumerations.OrderStatus.STORE_PREPARED;
import static com.nasnav.enumerations.OrderStatus.findEnum;
import static com.nasnav.enumerations.PaymentStatus.ERROR;
import static com.nasnav.enumerations.PaymentStatus.FAILED;
import static com.nasnav.enumerations.Roles.CUSTOMER;
import static com.nasnav.enumerations.Roles.NASNAV_ADMIN;
import static com.nasnav.enumerations.Roles.ORGANIZATION_MANAGER;
import static com.nasnav.enumerations.Roles.STORE_MANAGER;
import static com.nasnav.enumerations.ShippingStatus.DRAFT;
import static com.nasnav.enumerations.ShippingStatus.REQUSTED;
import static com.nasnav.enumerations.TransactionCurrency.EGP;
import static com.nasnav.enumerations.TransactionCurrency.UNSPECIFIED;
import static com.nasnav.exceptions.ErrorCodes.ADDR$ADDR$0002;
import static com.nasnav.exceptions.ErrorCodes.ADDR$ADDR$0004;
import static com.nasnav.exceptions.ErrorCodes.G$USR$0001;
import static com.nasnav.exceptions.ErrorCodes.O$0001;
import static com.nasnav.exceptions.ErrorCodes.O$CFRM$0001;
import static com.nasnav.exceptions.ErrorCodes.O$CFRM$0002;
import static com.nasnav.exceptions.ErrorCodes.O$CFRM$0004;
import static com.nasnav.exceptions.ErrorCodes.O$CHK$0001;
import static com.nasnav.exceptions.ErrorCodes.O$CHK$0002;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0001;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0002;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0003;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0004;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0005;
import static com.nasnav.exceptions.ErrorCodes.O$ORG$0001;
import static com.nasnav.exceptions.ErrorCodes.O$SHP$0001;
import static com.nasnav.exceptions.ErrorCodes.O$SHP$0002;
import static com.nasnav.exceptions.ErrorCodes.P$STO$0001;
import static com.nasnav.exceptions.ErrorCodes.S$0005;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.AppConfig;
import com.nasnav.dao.AddressRepository;
import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.CartItemRepository;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.OrganizationDomainsRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.RoleEmployeeUserRepository;
import com.nasnav.dao.ShipmentRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.BasketItem;
import com.nasnav.dto.BasketItemDTO;
import com.nasnav.dto.BasketItemDetails;
import com.nasnav.dto.DetailedOrderRepObject;
import com.nasnav.dto.OrderJsonDto;
import com.nasnav.dto.OrderRepresentationObject;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.shipping.ShipmentDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.dto.response.OrderConfrimResponseDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.dto.response.navbox.Shipment;
import com.nasnav.dto.response.navbox.SubOrder;
import com.nasnav.enumerations.OrderFailedStatus;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.enumerations.Roles;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.exceptions.StockValidationException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.exceptions.InvalidIntegrationEventException;
import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.CartItemEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationDomainsEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.persistence.ShipmentEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.dto.query.result.CartCheckoutData;
import com.nasnav.persistence.dto.query.result.CartItemData;
import com.nasnav.persistence.dto.query.result.StockBasicData;
import com.nasnav.request.OrderSearchParam;
import com.nasnav.response.OrderResponse;
import com.nasnav.service.helpers.EmployeeUserServiceHelper;
import com.nasnav.shipping.model.ShipmentTracker;

import lombok.AllArgsConstructor;
import lombok.Data;

@Service
public class OrderServiceImpl implements OrderService {

	private static final int ORDER_DEFAULT_COUNT = 1000;

	private static final int ORDER_FULL_DETAILS_LEVEL = 3;

	private static final Long NON_EXISTING_ORDER_ID = -1L;

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
	private ShippingManagementService shippingManagementService;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private OrganizationDomainsRepository domainRepo;
	
	@Autowired
	private AppConfig appConfig;
	
	@Autowired
	private RoleEmployeeUserRepository empRoleRepo;
	private Map<OrderStatus, Set<OrderStatus>> nextOrderStatusSet;
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
		nextOrderStatusSet = new HashMap<>();
		buildMap(nextOrderStatusSet)
			.put(NEW				, setOf(NEW, CLIENT_CONFIRMED, CLIENT_CANCELLED))
			.put(CLIENT_CONFIRMED	, setOf(CLIENT_CANCELLED, STORE_CONFIRMED, STORE_CANCELLED))
			.put(STORE_CONFIRMED	, setOf(STORE_PREPARED, STORE_CANCELLED))
			.put(STORE_PREPARED		, setOf(DISPATCHED, STORE_CANCELLED))
			.put(DISPATCHED			, setOf(DELIVERED, STORE_CANCELLED));
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
		return Objects.equals(order.getStatus(), OrderStatus.NEW.toString() );
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
		orderEntity.setStatus( OrderStatus.NEW.getValue() );
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
		
		order.getSubOrders().forEach(this::sendNotificationEmailToStoreManager);
		sendBillEmail(order);
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
		params.put("id", order.getId().toString());
		params.put("creationTime", orderTime);
		params.put("orderPageUrl", buildDashboardPageUrl(order));
		params.put("sub", order);
		return params;
	}


	
	
	private String buildDashboardPageUrl(OrdersEntity order) {
		Long orgId = order.getOrganizationEntity().getId();
		String domain = 
				domainRepo
				.findByOrganizationEntity_Id(orgId)
				.map(OrganizationDomainsEntity::getDomain)
				.map(this::addProtocolIfNeeded)
				.orElse("");
		String path = appConfig.dashBoardOrderPageUrl.replace("{order_id}", order.getId().toString());
		return format("%s/%s", domain, path);
	}
	
	
	
	private String addProtocolIfNeeded(String domain) {
		return domain.startsWith("http:") ? domain : "https://"+domain;
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
		String total = getMetaOrderTotal(order).toPlainString();
		
		Map<String, Object> params = new HashMap<>();
		params.put("metaOrder", order);
		params.put("creationDate", orderTimeStr);
		params.put("total", total);
		return params;
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
	}
	
	
	

	private void clearOrderItemsFromCart(OrdersEntity order) {
		Long userId = order.getUserId();
		List<Long> stockIds = 
				order
				.getBasketsEntity()
				.stream()
				.map(BasketsEntity::getStocksEntity)
				.map(StocksEntity::getId)
				.collect(toList());
		cartItemRepo.deleteByStockIdInAndUser_Id(stockIds, userId);
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
			orderEntity.setCreationDate( LocalDateTime.now() );
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





	private boolean canOrderStatusChangeTo(OrderStatus currentStatus, OrderStatus newStatus) {
		return ofNullable(nextOrderStatusSet.get(currentStatus))
										.orElse(emptySet())
										.contains(newStatus);
	}
	
	
	

	private void validateManagerCanSetStatus(OrderStatus newStatus) throws BusinessException {
		if( !canManagerSetOrderStatusTo(newStatus)) {
			throw new BusinessException(
					format(ERR_ORDER_STATUS_NOT_ALLOWED_FOR_ROLE, newStatus.name(), "*MANAGER")
					,"INVALID_PARAM: status"
					, HttpStatus.NOT_ACCEPTABLE);
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
	public DetailedOrderRepObject getOrderInfo(Long orderId, Integer detailsLevel) throws BusinessException {
		
		BaseUserEntity user = securityService.getCurrentUser();
		Long orgId = securityService.getCurrentUserOrganizationId();
		boolean isNasnavAdmin = securityService.currentUserHasRole(NASNAV_ADMIN);
		Optional<OrdersEntity> order = Optional.empty();;

		Integer finalDetailsLevel = getFinalDetailsLevel(detailsLevel);

		if (user instanceof UserEntity ) {
			order = ordersRepository.findByIdAndUserIdAndOrganizationEntity_Id(orderId, user.getId(), orgId);
		} else if ( isNasnavAdmin ) {
			order = ordersRepository.findById(orderId);
		} else {
			order = ordersRepository.findByIdAndOrganizationEntity_Id(orderId, orgId);
		}

		if (order.isPresent()) {
			return getDetailedOrderInfo(order.get(), finalDetailsLevel);
		}

		throwInvalidOrderException( OrderFailedStatus.INVALID_ORDER.toString() );
		
		return null;
	}
	
	private Integer getFinalDetailsLevel(Integer detailsLevel) {
		return (detailsLevel == null || detailsLevel < 0 || detailsLevel > 3) ? ORDER_FULL_DETAILS_LEVEL : detailsLevel;
	}
	
	
	
	private DetailedOrderRepObject getDetailedOrderInfo(OrdersEntity order, Integer detailsLevel) {
	    Map<Long, List<BasketItemDetails>> basketItemsDetailsMap = getBasketItemsDetailsMap( setOf(order.getId()) );
		Map<Long, BigDecimal> orderItemsQuantity = getOrderItemsQuantity( setOf(order.getId()) );
	    return getDetailedOrderInfo(order, detailsLevel, orderItemsQuantity, basketItemsDetailsMap);
	}
	
	
	
	
	
	//TODO: doesn't support getting shipment fees
	private DetailedOrderRepObject getDetailedOrderInfo(OrdersEntity order, Integer detailsLevel,
														Map<Long, BigDecimal> orderItemsQuantity,
														Map<Long, List<BasketItemDetails>> basketItemsDetailsMap) {
		DetailedOrderRepObject representation = new DetailedOrderRepObject();

		List<BasketItemDetails> basketItemsDetails = ofNullable(basketItemsDetailsMap)
															.map(map -> map.get(order.getId()) )
															.orElse(new ArrayList<>());

		
		BeanUtils.copyProperties(getOrderSummary(order), representation);
		if (detailsLevel == null)
			detailsLevel = 0;


        if (detailsLevel >= 1)
        	BeanUtils.copyProperties( getOrderDetails(order), representation, new String[]{"orderId", "userId", "shopId", "createdAt", "status", "paymentStatus", "total"});

        if (detailsLevel == 2 && orderItemsQuantity.get(order.getId()) != null)
        	representation.setTotalQuantity(orderItemsQuantity.get(order.getId()).intValue());

		if (detailsLevel == 3) {
			List<BasketItem> itemsList = getBasketItems(basketItemsDetails);
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
		DetailedOrderRepObject obj = new DetailedOrderRepObject();

		obj.setOrderId(entity.getId() );
		obj.setUserId(entity.getUserId());
		obj.setShopId(entity.getShopsEntity().getId());
		obj.setCreatedAt(entity.getCreationDate());
		obj.setStatus(findEnum(entity.getStatus()).name());
		obj.setPaymentStatus(entity.getPaymentStatus().toString());
		obj.setTotal(entity.getAmount());

		return obj;
	}
	
	private DetailedOrderRepObject getOrderDetails(OrdersEntity entity) {
		DetailedOrderRepObject obj = new DetailedOrderRepObject();

		obj.setUserName(entity.getName());
		obj.setShopName(entity.getShopsEntity().getName());
		obj.setDeliveryDate(entity.getDeliveryDate());
		obj.setSubtotal(entity.getAmount());
		obj.setShipping(new BigDecimal(0));
		obj.setTotal(obj.getShipping().add(obj.getSubtotal()));

		//TODO set shipping price

		if (entity.getAddressEntity() != null) {
			AddressRepObj address = (AddressRepObj) entity.getAddressEntity().getRepresentation();
			obj.setShippingAddress(address);
		}

		return obj;
	}
	
	

	private List<BasketItem> getBasketItems(List<BasketItemDetails> itemsDetailsList) {
		return itemsDetailsList.stream()
							.map(this::toBasketItem)
							.collect(toList());
	}
	
	
	
	

	private BasketItem toBasketItem(BasketItemDetails itemDetails) {

		BasketItem item = new BasketItem();
		item.setProductId(itemDetails.getProductId());
		item.setName(itemDetails.getProductName());
		item.setPname(itemDetails.getProductPname());
		item.setStockId(itemDetails.getStockId());
		item.setQuantity(itemDetails.getQuantity().intValue());
		//TODO set item unit //
		item.setTotalPrice(itemDetails.getPrice());
		item.setThumb( itemDetails.getProductCoverImage() );
		item.setCurrency(ofNullable(TransactionCurrency.getTransactionCurrency(itemDetails.getCurrency())).orElse(EGP).name());
		
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

		
		return ordersEntityList.stream()
								.map(order -> getDetailedOrderInfo(order, detailsLevel, orderItemsQuantity, basketItemsDetailsMap))
								.collect(toList());
	}
	
	
	
	
	

	private Map<Long, List<BasketItemDetails>> getBasketItemsDetailsMap(Set<Long> ordersIds) {
		Set<Long> nonEmptyOrdersIds = ofNullable(ordersIds)
											.filter(set -> !set.isEmpty())
											.orElse( setOf(NON_EXISTING_ORDER_ID) );
		
		List<BasketItemDetails> basketData = em.createNamedQuery("Basket", BasketItemDetails.class)
												.setParameter("orderId", nonEmptyOrdersIds)
												.getResultList();
		return  basketData.stream()
						.filter(Objects::nonNull)
						.collect( groupingBy(BasketItemDetails::getOrderId));
	}


	private Map<Long, BigDecimal> getOrderItemsQuantity(Set<Long> orderIds) {
		List<BasketsEntity> basketsEntities = basketRepository.findByOrdersEntity_IdIn(orderIds)
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

		Predicate[] predicatesArr = getOrderQueryPredicates(params, builder, root);

		query.where(predicatesArr).orderBy(builder.desc(root.get("updateDate")));
		
		return query;
	}
	
	
	

	private Predicate[] getOrderQueryPredicates(OrderSearchParam params, CriteriaBuilder builder, Root<OrdersEntity> root) {
		List<Predicate> predicates = new ArrayList<>();

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


	private Cart getUserCart(Long userId) {
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
		StocksEntity stock = ofNullable(item.getStockId()).map(id -> stockRepository.findByIdAndOrganizationEntity_Id(id, orgId))
							.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE ,P$STO$0001,item.getStockId()));
		validateCartItem(stock, item);

		CartItemEntity cartItem = ofNullable(cartItemRepo.findByStock_IdAndUser_Id(stock.getId(), user.getId()))
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
		cartItem.setCoverImage(item.getCoverImg());
		cartItemRepo.save(cartItem);

		return getUserCart(user.getId());
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
		
		Map<String,String> variantFeatures = parseVariantFeatures(itemData.getFeatureSpec());
		
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
		itemDto.setStockId(itemData.getStockId());
		
		return itemDto;
	}
	
	
	


	private Map<String, String> parseVariantFeatures(String featureSpec) {
		return productService.parseVariantFeatures(featureSpec);
	}





	@Override
	@Transactional
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
		if(CLIENT_CONFIRMED != status) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CFRM$0002, orderId, status.name());
		}
		return order;
	}





	private void confirmSubOrderAndMetaOrder(OrdersEntity order) {
		order.setStatus(STORE_CONFIRMED.getValue());
		ordersRepository.save(order);
		
		MetaOrderEntity metaOrder = order.getMetaOrder();		
		if(isAllOtherOrdersConfirmed(order.getId(), metaOrder)) {
			metaOrder.setStatus(STORE_CONFIRMED.getValue());
			metaOrderRepo.save(metaOrder);
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
		return metaOrder
				.getSubOrders()
				.stream()
				.filter(ord -> !Objects.equals(ord.getId(), orderId))
				.allMatch(ord -> Objects.equals(STORE_CONFIRMED.getValue() , ord.getStatus()));
	}

	public ArrayList<OrdersEntity> getOrdersForMetaOrder(Long metaOrderId) {
		if (metaOrderId == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(ordersRepository.findByMetaOrderId(metaOrderId));
	}
	
	
	
	
	
	@Override
	@Transactional
	public Order checkoutCart(CartCheckoutDTO dto) throws IOException {
		BaseUserEntity user = securityService.getCurrentUser();
		if(user instanceof EmployeeUserEntity) {
			throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
		}
		
		cancelAbandonedOrders();

		validateCartCheckoutDTO(dto);

		MetaOrderEntity order = createMetaOrder(dto, user);

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
		abandonedOrders.forEach(this::cancelAbandonedOrder);
	}


	
	
	
	private void cancelAbandonedOrder(MetaOrderEntity metaOrder) {
		metaOrder.setStatus(CLIENT_CANCELLED.getValue());
		metaOrder
		.getSubOrders()
		.forEach(subOrder -> subOrder.setStatus(CLIENT_CANCELLED.getValue()));
	}



	private MetaOrderEntity createMetaOrder(CartCheckoutDTO dto, BaseUserEntity user) {
		AddressesEntity userAddress = 
				addressRepo
				.findByIdAndUserId(dto.getAddressId(), user.getId())
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0002, dto.getAddressId()));

		Map<Long, List<CartCheckoutData>> checkOutData = getAndValidateCheckoutData(user); 
		MetaOrderEntity order = createOrder( checkOutData, userAddress, dto );
		return order;
	}





	private Map<Long, List<CartCheckoutData>> getAndValidateCheckoutData(BaseUserEntity user) {
		List<CartCheckoutData> userCartItems = cartItemRepo.getCheckoutCartByUser_Id(user.getId());

		validateCartCheckoutItems(userCartItems);

		Map<Long,List<CartCheckoutData>> checkOutData = 
				userCartItems
				.stream()
				.collect(groupingBy(CartCheckoutData::getShopId));
		return checkOutData;
	}

	


	private Order getOrderResponse(MetaOrderEntity order) {
		Map<Long, List<BasketItemDetails>> itemsMap =
				getBasketItemsDetailsMap(order.getSubOrders().stream().map(OrdersEntity::getId).collect(toSet()));

		Order orderDto = setMetaOrderBasicData(order);

		List<SubOrder> subOrders = 	createSubOrderDtoList(order, itemsMap);
				
		orderDto.setSubOrders(subOrders);
		orderDto.setSubtotal(order.getSubTotal());
		orderDto.setShipping(order.getShippingTotal());
		orderDto.setTotal(order.getGrandTotal());
		return orderDto;
	}





	private List<SubOrder> createSubOrderDtoList(MetaOrderEntity order, Map<Long, List<BasketItemDetails>> itemsMap) {
		return order
		.getSubOrders()
		.stream()
		.map(subOrder -> getSubOrder(subOrder, itemsMap))
		.collect(toList());
	}



	private Order setMetaOrderBasicData(MetaOrderEntity metaOrder) {
		Order order = new Order();
		order.setUserId(metaOrder.getUser().getId());
		order.setUserName(metaOrder.getUser().getName());
		order.setOrderId(metaOrder.getId());
		order.setCurrency(getOrderCurrency(metaOrder));
		order.setCreationDate(metaOrder.getCreatedAt());
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
			order = metaOrderRepo.findById(orderId);
		} else {
			order = metaOrderRepo.findByIdAndOrganization_Id(orderId, orgId);
		}

		if (order.isPresent()) {
			return getOrderResponse(order.get());
		}
		throw new RuntimeBusinessException(NOT_FOUND, O$0001, orderId);
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

	




	private SubOrder getSubOrder(OrdersEntity order, Map<Long, List<BasketItemDetails>> itemsMap) {

		String status = 
				ofNullable(findEnum(order.getStatus()))
				.orElse(NEW)
				.name();
		
		List<BasketItem> items = 
				itemsMap
				.get(order.getId())
				.stream()
				.map(this::toBasketItem)
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
		subOrder.setSubtotal(order.getAmount());
		subOrder.setStatus(status);
		subOrder.setDeliveryAddress((AddressRepObj)order.getAddressEntity().getRepresentation());
		subOrder.setItems(items);
		subOrder.setShipment(shipmentDto);
		subOrder.setTotalQuantity(totalQuantity);
		subOrder.setTotal(order.getTotal());
		subOrder.setSubtotal(order.getAmount());
		return subOrder;
	}


	private void validateCartCheckoutDTO(CartCheckoutDTO dto){
		if (dto.getAddressId() == null) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0004);
		}
		if (dto.getServiceId() == null) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CHK$0002);
		}
		if (!(dto.getAdditionalData() == null || dto.getAdditionalData().isEmpty())) {
			shippingManagementService.validateShippingAdditionalData(dto);
		}
	}


	private MetaOrderEntity createOrder(Map<Long, List<CartCheckoutData>> shopCartsMap, AddressesEntity address, CartCheckoutDTO dto) {
		OrganizationEntity org = securityService.getCurrentUserOrganization();
		UserEntity user = (UserEntity)securityService.getCurrentUser();
		
		List<CartItemsForShop> cartDividedByShop = groupCartItemsByShop(shopCartsMap);
		Set<OrdersEntity> subOrders = createSubOrders(cartDividedByShop, address, dto);
		BigDecimal subTotal = calculateSubTotal(subOrders);
		BigDecimal shippingFeeTotal = calculateShippingTotal(subOrders);
		BigDecimal total = calculateTotal(subOrders);
		
		MetaOrderEntity order = new MetaOrderEntity();
		order.setOrganization(org);
		order.setUser(user);
		order.setStatus(CLIENT_CONFIRMED.getValue());
		order.setGrandTotal(total);
		order.setSubTotal(subTotal);
		order.setShippingTotal(shippingFeeTotal);
		subOrders.forEach(order::addSubOrder);
		return metaOrderRepo.save(order);
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
		.findByOrganizationEntity_Id(org.getId())
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
				.map(subOrder -> shippingManagementService.createShippingDetailsFromOrder(subOrder, dto.getAdditionalData()))
				.collect(collectingAndThen(toList(), shippingManagementService::getOffersFromOrganizationShippingServices));
		
		for(OrdersEntity subOrder : subOrders) {
			subOrder.setShipment(createShipment(subOrder, dto, shippingOffers));
			subOrder.setTotal(calculateTotal(subOrder));
		}
		return subOrders;
	}
	
	
	
	


	private BigDecimal calculateTotal(OrdersEntity subOrder) {
		BigDecimal shippingFee = 
				ofNullable(subOrder.getShipment())
				.map(ShipmentEntity::getShippingFee)
				.orElse(ZERO);
		BigDecimal subTotal = ofNullable(subOrder.getAmount()).orElse(ZERO);
		return subTotal.add(shippingFee);
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
		
		BasketsEntity basket = new BasketsEntity();
		basket.setStocksEntity(stock);
		basket.setPrice(data.getPrice());
		basket.setQuantity(new BigDecimal(data.getQuantity()));
		basket.setCurrency(data.getCurrency());
		basket.setOrdersEntity(subOrder);

		return basket;
	}


	private OrdersEntity createSubOrder(AddressesEntity shippingAddress,
										CartItemsForShop cartItems) {
		Long userId = securityService.getCurrentUser().getId();
		OrganizationEntity org = securityService.getCurrentUserOrganization();
		
		OrdersEntity subOrder = new OrdersEntity();
		subOrder.setUserId(userId);
		subOrder.setShopsEntity(cartItems.getShop());
		subOrder.setOrganizationEntity(org);
		subOrder.setAddressEntity(shippingAddress);
		subOrder.setStatus(CLIENT_CONFIRMED.getValue());
		
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
				.findFirst()
				.map(ShippingOfferDTO::getShipments)
				.orElse(emptyList())
				.stream()
				.filter(shp -> Objects.equals(shp.getSubOrderId(), subOrder.getId()))
				.findFirst()
				.get();

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

}


@Data
@AllArgsConstructor
class CartItemsForShop{
	private ShopsEntity shop;
	private List<CartCheckoutData> checkOutData;
}
