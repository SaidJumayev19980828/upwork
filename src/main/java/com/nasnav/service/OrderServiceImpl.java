package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.isNullOrEmpty;
import static com.nasnav.commons.utils.EntityUtils.isNullOrZero;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_CALC_ORDER_FAILED;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_INVALID_ITEM_QUANTITY;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_INVALID_ORDER_STATUS;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_INVALID_ORDER_STATUS_UPDATE;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_ITEMS_FROM_MULTIPLE_SHOPS;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_NEW_ORDER_WITH_EMPTY_BASKET;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_NON_EXISTING_STOCK_ID;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_NO_ENOUGH_STOCK;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_NULL_ITEM;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_ORDER_NOT_EXISTS;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_UPDATED_ORDER_WITH_NO_ID;
import static com.nasnav.constatnts.error.orders.OrderServiceErrorMessages.ERR_NO_CURRENT_ORDER;
import static com.nasnav.enumerations.TransactionCurrency.UNSPECIFIED;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.enumerations.OrderFailedStatus;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.OrderResponse;
import com.nasnav.service.helpers.EmployeeUserServiceHelper;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;

@Service
public class OrderServiceImpl implements OrderService {

	private final OrdersRepository ordersRepository;

	private final BasketRepository basketRepository;

	private final StockRepository stockRepository;

	private final StockService stockService;

	private final EmployeeUserRepository employeeUserRepository;

	private final EmployeeUserServiceHelper employeeUserServiceHelper;
	
	private final UserRepository userRepository;

	@Autowired
	private EntityManager em;

	@Autowired
	private ProductImageService imgService;
	
	
	@Autowired
	private SecurityService securityService;
	

	@Autowired
	public OrderServiceImpl(OrdersRepository ordersRepository, BasketRepository basketRepository,
							StockRepository stockRepository ,StockService stockService, UserRepository userRepository,
	                        EmployeeUserServiceHelper employeeUserServiceHelper, EmployeeUserRepository employeeUserRepository,
							ProductRepository productRepository) {
		this.ordersRepository = ordersRepository;
		this.stockRepository = stockRepository;
		this.basketRepository = basketRepository;
		this.stockService = stockService;
		this.userRepository = userRepository;
		this.employeeUserServiceHelper = employeeUserServiceHelper;
		this.employeeUserRepository = employeeUserRepository;
	}
	
	
	
	

	public OrderValue getOrderValue(OrdersEntity order) {
		TransactionCurrency currency = null;
		BigDecimal amount = new BigDecimal(0);

		List<BasketsEntity> basketsEntity = basketRepository.findByOrdersEntity_Id(order.getId());
		for (BasketsEntity basketEntity : basketsEntity) {
			amount = amount.add(basketEntity.getStocksEntity().getPrice().multiply(basketEntity.getQuantity()));
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
		
		List<String> possibleStatusList = getPossibleOrderStatus();
		if(!possibleStatusList.contains( order.getStatus() )) {
			throwInvalidOrderException(ERR_INVALID_ORDER_STATUS);
		}
		
		if(!isNewOrder(order)
				&& isNullOrZero(order.getId()) ) {
			throwInvalidOrderException(ERR_UPDATED_ORDER_WITH_NO_ID);
		}
		
		
		//TODO: can be removed, there is no reason for not-accepting empty baskets at the first place
		//it can be handled at confirmation
		if (isNewOrderToBeCreated(order) && isNullOrEmpty(order.getBasket()) ) {
			throwInvalidOrderException(ERR_NEW_ORDER_WITH_EMPTY_BASKET);
		}
		
		if( !isNullOrZero(order.getId()) ) {
			Optional<OrdersEntity> orderEntity = ordersRepository.findById(order.getId());
			if(!orderEntity.isPresent()) {
				throwInvalidOrderException(ERR_ORDER_NOT_EXISTS);
			}
		}
		
		List<BasketItemDTO> basket = order.getBasket();
		
		if(basket!= null &&  isNewOrder(order)){
			validateBasketItems(order);			
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
	
	
	
	
	
	
	private void throwInvalidOrderException(String msg) throws BusinessException {
		throw getInvalidOrderException(msg);
	}
	
	
	
	
	
	private BusinessException getInvalidOrderException(String msg) {
		String error = OrderFailedStatus.INVALID_ORDER.toString();
		return new BusinessException( msg, error, HttpStatus.NOT_ACCEPTABLE);
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
		orderEntity.setAddress(order.getAddress());
		orderEntity.setAmount( calculateOrderTotalValue(order) );
		// TODO ordersEntity.setPayment_type(payment_type);
		orderEntity.setShopsEntity(shop);
		orderEntity.setStatus( OrderStatus.NEW.getValue() );
		orderEntity.setUserId( user.getId() );
		orderEntity.setOrganizationEntity( org );
		return orderEntity;
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

		Optional<OrdersEntity> optional = ordersRepository.findById( orderJsonDto.getId() );
		if(!optional.isPresent()) {
			throwInvalidOrderException(ERR_ORDER_NOT_EXISTS);
		}
		OrdersEntity orderEntity = optional.get();	

		OrderResponse orderResponse = updateCurrentOrderStatus(orderJsonDto, orderEntity, newStatus);
		if ( newStatus.equals( OrderStatus.NEW )) {
			orderResponse = updateOrderBasket(orderJsonDto, orderEntity);
		}
		return orderResponse;
	}
	
	
	
	

	private OrderResponse updateOrderBasket(OrderJsonDto order, OrdersEntity orderEntity) throws BusinessException {

		basketRepository.deleteByOrdersEntity_Id(order.getId());
		
		orderEntity.setAmount( calculateOrderTotalValue(order) );
		orderEntity.setShopsEntity( getOrderShop( order) );
		orderEntity = ordersRepository.save(orderEntity);

		addItemsToBasket(order, orderEntity);

		return new OrderResponse(orderEntity.getId(), orderEntity.getAmount());
	}
	
	

	
	private OrderResponse updateCurrentOrderStatus(OrderJsonDto orderJsonDto, OrdersEntity orderEntity,
												   OrderStatus newStatus) throws BusinessException {

		OrderStatus currentOrderStatus = OrderStatus.findEnum(orderEntity.getStatus());		
		if ((newStatus.getValue() < currentOrderStatus.getValue()) ) {
			throwInvalidOrderException(ERR_INVALID_ORDER_STATUS_UPDATE);
		}
		
		if (newStatus == OrderStatus.CLIENT_CONFIRMED) {
			//TODO: WHY ???
			orderEntity.setCreationDate( LocalDateTime.now() );
		}
		orderEntity.setStatus(newStatus.getValue());
		orderEntity = ordersRepository.save(orderEntity);
		
		return new OrderResponse(orderEntity.getId(), orderEntity.getAmount());
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
		BigDecimal quantity = Optional.ofNullable( item.getQuantity() )
										.map(BigDecimal::new)
										.orElse(BigDecimal.ZERO);
		
		return Optional.ofNullable(item)
						.map(BasketItemDTO::getStockId)
						.flatMap(stockRepository::findById)
						.map(StocksEntity::getPrice)
						.filter(Objects::nonNull)
						.map(price -> price.multiply(quantity))
						.orElseThrow( () ->  new RuntimeException(ERR_CALC_ORDER_FAILED));
	}
	
	



	@Override
	public DetailedOrderRepObject getOrderInfo(Long orderId) throws BusinessException {
		
		BaseUserEntity user = securityService.getCurrentUser();
		
		if (user instanceof UserEntity 
				&& ordersRepository.existsByIdAndUserId(orderId, user.getId())) {
			return getDetailedOrderInfo(orderId, true);
		} else if (ordersRepository.existsById(orderId)) {
			return getDetailedOrderInfo(orderId, true);
		}			

		throwInvalidOrderException( OrderFailedStatus.INVALID_ORDER.toString() );
		
		return null;
	}
	
	
	
	
	
	
	//TODO: doesn't support getting shipment fees
	private DetailedOrderRepObject getDetailedOrderInfo(Long orderId, boolean getItems) {
		OrdersEntity entity = ordersRepository.findById(orderId).get();
		
		DetailedOrderRepObject obj = new DetailedOrderRepObject();
		obj.setOrderId(entity.getId() );
		obj.setUserId(entity.getUserId());
		obj.setShopId(entity.getShopsEntity().getId());
		obj.setCreatedAt(entity.getCreationDate());
		obj.setDeliveryDate(entity.getDeliveryDate());		
		obj.setSubtotal( entity.getAmount() );
		obj.setShipping(new BigDecimal(0));
		obj.setTotal(obj.getShipping().add(obj.getSubtotal()));
		obj.setStatus(OrderStatus.findEnum(entity.getStatus()).name());

		//TODO set shipping address, shipping price
		ShippingAddress address = new ShippingAddress();
		address.setDetails(entity.getAddress());
        obj.setShippingAddress(address);

		if (getItems) {
			List<BasketItem> itemsList = getBasketItems(orderId);
			obj.setItems(itemsList);
			if (itemsList.size() > 0) {
				obj.setCurrency(itemsList.get(0).getCurrency());
				Integer totalQuantity = itemsList.stream()
						.map(BasketItem::getQuantity)
						.reduce(0, Integer::sum);
				obj.setTotalQuantity( totalQuantity);
			}
		}
		return obj;
	}
	
	
	
	

	private List<BasketItem> getBasketItems(Long orderId) {

		List<BasketData> itemsEntityList = em.createNamedQuery("Basket").setParameter("orderId", orderId).getResultList();
		return itemsEntityList.stream()
							.map(this::toBasketItem)
							.collect(Collectors.toList());
	}
	
	
	
	

	private BasketItem toBasketItem(BasketData entityItem) {

		BasketItem item = new BasketItem();
		item.setProductId(entityItem.getProductId());
		item.setName(entityItem.getProductName());
		item.setPname(entityItem.getProductPname());
		item.setStockId(entityItem.getStockId());
		item.setQuantity(entityItem.getQuantity().intValue());
		//TODO set item unit //
		item.setTotalPrice(entityItem.getPrice());
		item.setThumb( entityItem.getProductCoverImage() );
		item.setCurrency(TransactionCurrency.getTransactionCurrency(entityItem.getCurrency()).name());
		
		return item;
	}
	
	
	

	private BigDecimal calculateOrderAmount(List<BasketItem> items) {
		BigDecimal total = new BigDecimal(0);
		for(BasketItem item: items) {
			total = total.add(item.getTotalPrice());
		}
			
		return total;
	}
	
		
	
	@Override
	public List<DetailedOrderRepObject> getOrdersList(String userToken, Long userId, Long storeId, Long orgId,
													  String status, boolean details) throws BusinessException {
		Integer statusId = null;

		if (status != null) {
			if ((OrderStatus.findEnum(status)) == null)
				throw new BusinessException("Provided status" + status + " doesn't match any existing status!","INVALID PARAM: status",HttpStatus.BAD_REQUEST);
			statusId = (OrderStatus.findEnum(status)).getValue();
		}

		BaseUserEntity user = securityService.getCurrentUser();
		if (user instanceof UserEntity) { // User
			userId = user.getId();
			orgId = null;
			storeId = null;
		} else { // EmployeeUser
			EmployeeUserEntity empUser = (EmployeeUserEntity)user;
			List<String> employeeUserRoles = employeeUserServiceHelper.getEmployeeUserRoles(empUser.getId());
			if (employeeUserRoles.contains("STORE_ADMIN") || employeeUserRoles.contains("STORE_MANAGER") || employeeUserRoles.contains("STORE_EMPLOYEE")){
				storeId = empUser.getShopId();
			} else if (employeeUserRoles.contains("ORGANIZATION_ADMIN") || employeeUserRoles.contains("ORGANIZATION_MANAGER") || employeeUserRoles.contains("ORGANIZATION_EMPLOYEE")) {
				orgId = empUser.getOrganizationId();
			}
		}

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<OrdersEntity> query = builder.createQuery(OrdersEntity.class);
		Root<OrdersEntity> root = query.from(OrdersEntity.class);

		Predicate[] predicatesArr = getOrderQueryPredicates(userId, storeId, orgId, statusId, builder, root);

		query.where(predicatesArr);

		List<OrdersEntity> ordersEntityList = em.createQuery(query).getResultList();

		List<DetailedOrderRepObject> ordersRep = new ArrayList<>();
		for(OrdersEntity orders: ordersEntityList)
			ordersRep.add(getDetailedOrderInfo(orders.getId(), details));

		return ordersRep;
	}

	private Predicate[] getOrderQueryPredicates(Long userId, Long storeId, Long orgId, Integer statusId,
												CriteriaBuilder builder, Root<OrdersEntity> root) {
		List<Predicate> predicates = new ArrayList<>();

		if(userId != null)
			predicates.add( builder.equal(root.get("userId"), userId) );

		if(orgId != null)
			predicates.add( builder.equal(root.get("organizationEntity").get("id"), orgId) );

		if(storeId != null)
			predicates.add( builder.equal(root.get("shopsEntity").get("id"), storeId) );

		if(statusId != null)
			predicates.add( builder.equal(root.get("status"), statusId) );

		return predicates.stream().toArray( Predicate[]::new) ;
	}




	@Override
	public DetailedOrderRepObject getCurrentOrder() throws BusinessException {
		BaseUserEntity user = securityService.getCurrentUser();
		
		OrdersEntity entity = ordersRepository.findFirstByUserIdAndStatusOrderByUpdateDateDesc( user.getId(), OrderStatus.NEW.getValue() )
											 .orElseThrow(() -> getNoCurrentOrderFoundException() );
		
		return getDetailedOrderInfo(entity.getId(), true);
	}


	
	
	
	private BusinessException getNoCurrentOrderFoundException() {
		return new BusinessException("User have no new orders!", "NOT FOUND", HttpStatus.NOT_FOUND);
	}



	@Override
	@Transactional
	public void deleteCurrentOrders() {
		BaseUserEntity user = securityService.getCurrentUser();
		
		ordersRepository.deleteByStatusAndUserId( OrderStatus.NEW.getValue(), user.getId());
	}
}
