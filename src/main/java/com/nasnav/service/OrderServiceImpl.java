package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.collectionContainsAnyOf;
import static com.nasnav.commons.utils.EntityUtils.isNullOrEmpty;
import static com.nasnav.commons.utils.EntityUtils.isNullOrZero;
import static com.nasnav.commons.utils.EntityUtils.setOf;
import static com.nasnav.commons.utils.MapBuilder.buildMap;
import static com.nasnav.commons.utils.StringUtils.nullSafe;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_CALC_ORDER_FAILED;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_INVALID_ITEM_QUANTITY;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_INVALID_ORDER_STATUS;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_INVALID_ORDER_STATUS_UPDATE;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_ITEMS_FROM_MULTIPLE_SHOPS;
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
import static com.nasnav.enumerations.OrderStatus.CLIENT_CANCELLED;
import static com.nasnav.enumerations.OrderStatus.CLIENT_CONFIRMED;
import static com.nasnav.enumerations.OrderStatus.DELIVERED;
import static com.nasnav.enumerations.OrderStatus.DISPATCHED;
import static com.nasnav.enumerations.OrderStatus.NEW;
import static com.nasnav.enumerations.OrderStatus.STORE_CANCELLED;
import static com.nasnav.enumerations.OrderStatus.STORE_CONFIRMED;
import static com.nasnav.enumerations.OrderStatus.STORE_PREPARED;
import static com.nasnav.enumerations.Roles.CUSTOMER;
import static com.nasnav.enumerations.Roles.ORGANIZATION_MANAGER;
import static com.nasnav.enumerations.Roles.STORE_MANAGER;
import static com.nasnav.enumerations.TransactionCurrency.UNSPECIFIED;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.BasketItem;
import com.nasnav.dto.BasketItemDTO;
import com.nasnav.dto.BasketItemDetails;
import com.nasnav.dto.DetailedOrderRepObject;
import com.nasnav.dto.OrderJsonDto;
import com.nasnav.dto.ShippingAddress;
import com.nasnav.enumerations.OrderFailedStatus;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.enumerations.Roles;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.request.OrderSearchParam;
import com.nasnav.response.OrderResponse;
import com.nasnav.service.helpers.EmployeeUserServiceHelper;

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
	
	@Autowired
	private EntityManager em;

	@Autowired
	private SecurityService securityService;

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
			amount = amount.add(basketEntity.getPrice().multiply(basketEntity.getQuantity()));
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
	
	
	
	
	
	@Override
	public OrderResponse handleOrder(OrderJsonDto orderJson) throws BusinessException {

		validateOrder(orderJson);

		if (isNewOrderToBeCreated(orderJson)) {
			return createNewOrderAndBasketItems(orderJson);
		} else {
			return updateOrder(orderJson);
		}
	}
	
	
	
	

	private void validateOrder(OrderJsonDto order) throws BusinessException {
		Long orderId = order.getId();
		
		List<String> possibleStatusList = getPossibleOrderStatus();
		if(!possibleStatusList.contains( order.getStatus() )) {
			throwInvalidOrderException(ERR_INVALID_ORDER_STATUS);
		}		
		
		if( !isNewOrder(order) && isNullOrZero(order.getId()) ) {
			throwInvalidOrderException(ERR_UPDATED_ORDER_WITH_NO_ID);
		}		

		if (isNewOrderToBeCreated(order)) {
			validateOrderCreation(order);			
		}		
		
		if( !isNullOrZero(orderId) ) {
			validateOrderUpdate(order);			 			
		}
		
		List<BasketItemDTO> basket = order.getBasket();
		
		if(basket!= null &&  isNewOrder(order)){
			validateBasketItems(order);			
		}		
	}





	private void validateOrderUpdate(OrderJsonDto order) throws BusinessException {		
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
	}





	private boolean isNewOrder(OrderJsonDto order) {
		return Objects.equals(order.getStatus(), OrderStatus.NEW.toString() );
	}
	
	
	
	
	

	private List<String> getPossibleOrderStatus() {
		List<String> possibleStatusList = Arrays.asList( OrderStatus.values() )
												.stream()
												.map(OrderStatus::toString)
												.collect(Collectors.toList());
		return possibleStatusList;
	}
	
	
	
	

	private void validateBasketItems(OrderJsonDto order) throws BusinessException {	
		List<BasketItemDTO> basket = order.getBasket();
		
		for(BasketItemDTO item: basket) {
			validateBasketItem(item);
		}
		
						
		if( countShops(basket) > 1) {
			throwInvalidOrderException(ERR_ITEMS_FROM_MULTIPLE_SHOPS);
		}
	}
	
	
	
	

	private Long countShops(List<BasketItemDTO> basket) {
		List<StocksEntity> stocks = getBasketStocks(basket);
		Long shopsCount = stocks.stream()
								.map(StocksEntity::getShopsEntity)
								.filter(Objects::nonNull)
								.map(ShopsEntity::getId)
								.distinct()
								.count();
		return shopsCount;
	}
	
	
	
	
	
	
	private List<StocksEntity> getBasketStocks(List<BasketItemDTO> basket) {
		if(basket == null) {
			return new ArrayList<>();
		}
		
		List<Long> itemStockIds = basket.stream()
										.map(BasketItemDTO::getStockId)
										.collect(Collectors.toList());
		
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
	
	
	
	
	
	private boolean isNewOrderToBeCreated(OrderJsonDto orderJson) {
		return isNewOrder(orderJson)
					&& isNullOrZero(orderJson.getId());
	}
	
	
	
	
	
	
	private void throwInvalidOrderException(String msg, Object... msgParams) throws BusinessException {
		throw getInvalidOrderException(msg, msgParams);
	}
	
	
	
	
	
	private BusinessException getInvalidOrderException(String msg, Object... msgParams) {
		String error = OrderFailedStatus.INVALID_ORDER.toString();
		return new BusinessException( format(msg, msgParams), error, HttpStatus.NOT_ACCEPTABLE);
	}
	
	
	
	
	
	private void throwForbiddenOrderException(String msg, Object... msgParams) throws BusinessException {
		throw getForbiddenOrderException(msg, msgParams);
	}
	
	
	
	private BusinessException getForbiddenOrderException(String msg, Object... msgParams) {
		String error = OrderFailedStatus.INVALID_ORDER.toString();
		return new BusinessException( format(msg, msgParams), error, HttpStatus.FORBIDDEN);
	}
	
	
	
	

	private OrderResponse createNewOrderAndBasketItems(OrderJsonDto order) throws BusinessException {
		
		OrdersEntity orderEntity = createNewOrderEntity(order);
		orderEntity = ordersRepository.save(orderEntity);

		addItemsToBasket(order, orderEntity);

		OrderResponse orderResponse = new OrderResponse(orderEntity.getId(), orderEntity.getAmount());
		orderResponse.setCode(HttpStatus.CREATED);
		return orderResponse;
	}
	
	
	
	

	private OrdersEntity createNewOrderEntity(OrderJsonDto order) {
		BaseUserEntity user = securityService.getCurrentUser();
		OrganizationEntity org = securityService.getCurrentUserOrganization();
		ShopsEntity shop = getOrderShop(order);

		OrdersEntity orderEntity = new OrdersEntity();
		
		String address = ofNullable(order.getAddress())
								.orElse( getUserAddressAsStr());
		orderEntity.setAddress(address);
		orderEntity.setAmount( calculateOrderTotalValue(order) );
		// TODO ordersEntity.setPayment_type(payment_type);
		orderEntity.setShopsEntity(shop);
		orderEntity.setStatus( OrderStatus.NEW.getValue() );
		orderEntity.setUserId( user.getId() );
		orderEntity.setName(user.getName());
		orderEntity.setOrganizationEntity( org );
		return orderEntity;
	}
	
	
	
	

	private String getUserAddressAsStr() {
		BaseUserEntity baseUser = securityService.getCurrentUser();
		String address = null;
		
		if(baseUser instanceof UserEntity) {			
			UserEntity user = (UserEntity)baseUser;
			
			address = format("%s, %s, %s "
								, nullSafe(user.getAddress())
								, nullSafe(user.getAddressCity())
								, nullSafe(user.getAddressCountry()));
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

		OrderStatus newStatus = Optional.ofNullable(orderJsonDto.getStatus())
										.map(OrderStatus::findEnum)
										.orElse(OrderStatus.NEW);

		OrdersEntity orderEntity =  ordersRepository.findById( orderJsonDto.getId() )
													.orElseThrow(() -> getInvalidOrderException(ERR_ORDER_NOT_EXISTS));

		OrderResponse orderResponse = updateCurrentOrderStatus(orderJsonDto, orderEntity, newStatus);
		
		if ( newStatus.equals( OrderStatus.NEW )) {
			orderResponse = updateOrderBasket(orderJsonDto, orderEntity);
		}
		return orderResponse;
	}
	
	
	
	

	private OrderResponse updateOrderBasket(OrderJsonDto order, OrdersEntity orderEntity) throws BusinessException {

		basketRepository.deleteByOrderIdIn(asList(order.getId()));
		
		orderEntity.setAmount( calculateOrderTotalValue(order) );
		orderEntity.setShopsEntity( getOrderShop( order) );
		orderEntity = ordersRepository.save(orderEntity);

		addItemsToBasket(order, orderEntity);

		return new OrderResponse(orderEntity.getId(), orderEntity.getAmount());
	}
	
	

	
	private OrderResponse updateCurrentOrderStatus(OrderJsonDto orderJsonDto, OrdersEntity orderEntity,
												   OrderStatus newStatus) throws BusinessException {

		OrderStatus currentOrderStatus = OrderStatus.findEnum(orderEntity.getStatus());		
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
		basketsEntity.setCurrency(TransactionCurrency.EGP.getValue());

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
		OrdersEntity order;

		Integer finalDetailsLevel = getFinalDetailsLevel(detailsLevel);

		if (user instanceof UserEntity && ordersRepository.existsByIdAndUserId(orderId, user.getId())) {
			order = ordersRepository.findByIdAndUserId(orderId, user.getId());
			return getDetailedOrderInfo(order, finalDetailsLevel);
		} else if (ordersRepository.existsById(orderId)) {
			order = ordersRepository.findById(orderId).get();
			return getDetailedOrderInfo(order, finalDetailsLevel);
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
		obj.setStatus(OrderStatus.findEnum(entity.getStatus()).name());
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
		//TODO set shipping address, shipping price
		ShippingAddress address = new ShippingAddress();
		address.setDetails(entity.getAddress());
		obj.setShippingAddress(address);

		return obj;
	}
	
	

	private List<BasketItem> getBasketItems(List<BasketItemDetails> itemsDetailsList) {
		return itemsDetailsList.stream()
							.map(this::toBasketItem)
							.collect(Collectors.toList());
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
		item.setCurrency(TransactionCurrency.getTransactionCurrency(itemDetails.getCurrency()).name());
		
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
			ordersIds = ordersEntityList.stream().map(OrdersEntity::getId).collect(Collectors.toSet());
		}

		Map<Long, List<BasketItemDetails>> basketItemsDetailsMap = getBasketItemsDetailsMap(detailsLevel == 3 ? ordersIds : new HashSet<>() );

		Map<Long, BigDecimal> orderItemsQuantity = getOrderItemsQuantity(detailsLevel == 2 ? ordersIds : new HashSet<>());

		
		return ordersEntityList.stream()
								.map(order -> getDetailedOrderInfo(order, detailsLevel, orderItemsQuantity, basketItemsDetailsMap))
								.collect(Collectors.toList());
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
				.collect( Collectors.toList());

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
			OrderStatus statusEnum = OrderStatus.findEnum(status);
			if (statusEnum == null) {
				throw new BusinessException("Provided status (" + status + ") doesn't match any existing status!","INVALID PARAM: status",HttpStatus.BAD_REQUEST);
			}				
			return OrderStatus.findEnum(status).getValue();
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
		
		OrdersEntity entity = ordersRepository.findFirstByUserIdAndStatusOrderByUpdateDateDesc( user.getId(), OrderStatus.NEW.getValue() )
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
								.collect(Collectors.toList());
		
		basketRepository.deleteByOrderIdIn(userNewOrders);		
		ordersRepository.deleteByStatusAndUserId( NEW.getValue(), user.getId());
	}
}
