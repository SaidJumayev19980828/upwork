package com.nasnav.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.OrderRepresentationObject;
import com.nasnav.persistence.*;
import com.nasnav.service.helpers.EmployeeUserServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.BasketItem;
import com.nasnav.dto.OrderJsonDto;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.enumerations.OrderFailedStatus;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.response.OrderResponse;
import com.nasnav.response.exception.OrderValidationException;

import static com.nasnav.enumerations.TransactionCurrency.UNSPECIFIED;

@Service
public class OrderServiceImpl implements OrderService {

	private final OrdersRepository ordersRepository;

	private final BasketRepository basketRepository;

	private final StockRepository stockRepository;

	private final StockService stockService;

	private final EmployeeUserRepository employeeUserRepository;

	private final EmployeeUserServiceHelper employeeUserServiceHelper;
	private final UserRepository userRepository;

	private final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class.getName());
	private final ProductRepository productRepository;
	
	
	@Autowired
	private ProductImageService imgService;

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
		this.productRepository = productRepository;
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

	public OrderResponse updateOrder(OrderJsonDto orderJson, Long userId) {

		if ((orderJson.getId() == null || orderJson.getId() == 0)
				&& (orderJson.getBasket() == null || orderJson.getBasket().size() == 0)) {

			return new OrderResponse(OrderFailedStatus.INVALID_ORDER, HttpStatus.NOT_ACCEPTABLE);
		}

		if ((orderJson.getStatus() == null || orderJson.getStatus().equals(OrderStatus.NEW.toString()))
				&& (orderJson.getBasket() == null || orderJson.getBasket().size() == 0)) {
			return new OrderResponse(OrderFailedStatus.INVALID_ORDER, HttpStatus.NOT_ACCEPTABLE);
		}

		if (orderJson.getId() == null || orderJson.getId() == 0) {
			return createNewOrderAndBasketItems(orderJson, userId);
		} else if (orderJson.getStatus() != null) {
			return updateCurrentOrder(orderJson);
		} else {
			return new OrderResponse(OrderFailedStatus.INVALID_ORDER, HttpStatus.NOT_ACCEPTABLE);
		}
		// map order object from API to OrdersEntity
//		OrderResponse orderMappingResponse = mapOrderJsonToOrderEntity(orderJson);
//		if (orderMappingResponse.getCode().equals(HttpStatus.NOT_ACCEPTABLE)) {
//			return orderMappingResponse;
//		}
//		return updateOrCreateOrderEntity(orderMappingResponse.getEntity());
	}

	private OrderResponse createNewOrderAndBasketItems(OrderJsonDto orderJsonDto, Long userId) {

		// Getting the stocks related to current order
		List<StocksEntity> stocksEntites = getCurrentStock(orderJsonDto);
		// Getting the actual added stock items
		Long availableStock = stocksEntites.stream().filter(stock -> stock != null).count();

		// Return error cause stock is not available
		if (availableStock != orderJsonDto.getBasket().size()) {
			return new OrderResponse(OrderFailedStatus.INVALID_ORDER, HttpStatus.NOT_ACCEPTABLE);
		}

		Long numberOfShops = stocksEntites.stream().map(stock -> stock.getShopsEntity().getId()).distinct().count();
		if (numberOfShops > 1) {
			return new OrderResponse(OrderFailedStatus.MULTIPLE_STORES, HttpStatus.NOT_ACCEPTABLE);
		}

		OrdersEntity orderEntity = new OrdersEntity();
		orderEntity.setAddress(orderJsonDto.getAddress());
		orderEntity.setAmount(calculateOrderAmount(orderJsonDto, stocksEntites));
		orderEntity.setCreationDate( LocalDateTime.now() );
		// TODO ordersEntity.setPayment_type(payment_type);
		orderEntity.setShopsEntity(stocksEntites.get(0).getShopsEntity());
		orderEntity.setStatus(OrderStatus.NEW.getValue());
		orderEntity.setUpdateDate( LocalDateTime.now()  );
		orderEntity.setUserId(userId);

		orderEntity = ordersRepository.save(orderEntity);

		addItemsToBasket(orderJsonDto, orderEntity, stocksEntites);

		OrderResponse orderResponse = new OrderResponse(orderEntity.getId(), orderEntity.getAmount());
		orderResponse.setCode(HttpStatus.CREATED);
		return orderResponse;

	}

	private List<StocksEntity> getCurrentStock(OrderJsonDto orderJsonDto) {
		// Getting the stocks related to current order
		List<StocksEntity> stocksEntites = new ArrayList<>(orderJsonDto.getBasket().size());

		orderJsonDto.getBasket().stream()
				.filter(basketItem -> basketItem.getQuantity() > 0 && basketItem.getStockId() != null)
				.forEach(basketItem -> {
					Optional<StocksEntity> optionalStocksEntity = stockRepository.findById(basketItem.getStockId());

					if (isInvalidStock(basketItem, optionalStocksEntity)) {
						return;
					}
					stocksEntites.add(optionalStocksEntity.get());
				});

		// TODO check for pending orders logic and what to do
		return stocksEntites;
	}


	/**
	 * @return if stock Id is invalid or available quantity is less than required
	 * */
	private boolean isInvalidStock(BasketItemDTO basketItem, Optional<StocksEntity> optionalStocksEntity) {
		Integer q = stockService.getStockQuantity(optionalStocksEntity.get());
		return optionalStocksEntity == null || !optionalStocksEntity.isPresent()
				|| basketItem.getQuantity() > q
				|| optionalStocksEntity.get().getPrice().doubleValue() <= 0;
	}



	private OrderResponse updateCurrentOrder(OrderJsonDto orderJsonDto) {

		OrderStatus newStatus = OrderStatus.findEnum(orderJsonDto.getStatus());
		if (newStatus == null && (orderJsonDto.getBasket() == null || orderJsonDto.getBasket().isEmpty())) {
			log.error("Order update should be either status or changing basket items");
			return new OrderResponse(OrderFailedStatus.INVALID_ORDER, HttpStatus.NOT_ACCEPTABLE);
		}
		Optional<OrdersEntity> orderOptional = ordersRepository.findById(orderJsonDto.getId());

		if (orderOptional == null || !orderOptional.isPresent()) {
			log.error("Supplied Order Id was not found");
			return new OrderResponse(OrderFailedStatus.INVALID_ORDER, HttpStatus.BAD_REQUEST);
		}

		OrdersEntity orderEntity = orderOptional.get();

		OrderStatus currentOrderStatus = OrderStatus.findEnum(orderEntity.getStatus());

		OrderResponse orderResponse = null;
		if (newStatus != null) {
			orderResponse = updateCurrentOrderStatus(orderJsonDto, orderEntity, newStatus, currentOrderStatus);
		}

		if (orderJsonDto.getBasket() != null && !orderJsonDto.getBasket().isEmpty()) {
			orderResponse = updateCurrentOrderBasket(orderJsonDto, orderEntity);
		}

		return orderResponse;
	}

	private OrderResponse updateCurrentOrderBasket(OrderJsonDto orderJsonDto, OrdersEntity orderEntity) {

		basketRepository.deleteByOrdersEntity_Id(orderJsonDto.getId());

		// Getting the stocks related to current order
		List<StocksEntity> stocksEntites = new ArrayList<>(orderJsonDto.getBasket().size());

		orderJsonDto.getBasket().stream().filter(basketItem -> basketItem.getStockId() != null).forEach(basketItem -> {
			Optional<StocksEntity> optionalStocksEntity = stockRepository.findById(basketItem.getStockId());

			if (isInvalidStock(basketItem , optionalStocksEntity)) {
				// stock Id is invalid or available quantity is less than required
				log.error("Stock Id {} is invalid", basketItem.getStockId());
				return;
			}
			stocksEntites.add(optionalStocksEntity.get());
		});

		orderEntity.setAmount(calculateOrderAmount(orderJsonDto, stocksEntites));
		orderEntity.setShopsEntity(stocksEntites.get(0).getShopsEntity());
		orderEntity.setUpdateDate( LocalDateTime.now());
		orderEntity = ordersRepository.save(orderEntity);

		addItemsToBasket(orderJsonDto, orderEntity, stocksEntites);

		return new OrderResponse(orderEntity.getId(), orderEntity.getAmount());

	}

	private OrderResponse updateCurrentOrderStatus(OrderJsonDto orderJsonDto, OrdersEntity orderEntity,
												   OrderStatus newStatus, OrderStatus currentOrderStatus) {


		// commented as baskets are required only for new orders
		/*
		if ((newStatus.getValue() >= currentOrderStatus.getValue())
				&& (orderJsonDto.getBasket() == null || orderJsonDto.getBasket().isEmpty())) {
			// TODO check with Marek if this valid from business perspective
			log.error("Cannot update order staus to previous status");
			return new OrderResponse(OrderFailedStatus.INVALID_STATUS, HttpStatus.BAD_REQUEST);
		}

		 */
		if (newStatus == OrderStatus.CLIENT_CONFIRMED) {
			orderEntity.setCreationDate( LocalDateTime.now() );
		}
		orderEntity.setStatus(newStatus.getValue());
		orderEntity.setUpdateDate( LocalDateTime.now() );
		orderEntity = ordersRepository.save(orderEntity);
		return new OrderResponse(orderEntity.getId(), orderEntity.getAmount());
	}

	private void addItemsToBasket(OrderJsonDto orderJsonDto, OrdersEntity orderEntity,
								  List<StocksEntity> stocksEntites) {

		for (BasketItemDTO basketItem : orderJsonDto.getBasket()) {
			StocksEntity stocksEntity = stocksEntites.stream().filter(stock -> stock.getId().equals(basketItem.getStockId()))
					.findFirst().get();
			BasketsEntity basketsEntity = new BasketsEntity();
			basketsEntity.setStocksEntity(stocksEntity);
			basketsEntity.setOrdersEntity(orderEntity);
			// TODO make sure price here means item price multiplied by quantity
			basketsEntity.setPrice(new BigDecimal(basketItem.getQuantity()).multiply(stocksEntity.getPrice()));
			basketsEntity.setQuantity(new BigDecimal(basketItem.getQuantity()));

			// TODO how currency determined for specific order
			basketsEntity.setCurrency(TransactionCurrency.EGP.getValue());

			basketRepository.save(basketsEntity);
		}

	}

	private BigDecimal calculateOrderAmount(OrderJsonDto orderJsonDto, List<StocksEntity> stocksEntites) {

		return orderJsonDto.getBasket().stream().map(basketItem -> {

			BigDecimal price = stocksEntites.stream().filter(stock -> stock.getId().equals(basketItem.getStockId()))
					.findFirst().get().getPrice();
			// TODO Discount rules to be applied if required

			return price.multiply(new BigDecimal(basketItem.getQuantity()));
		}).reduce(BigDecimal.ZERO, BigDecimal::add);

	}

	private OrderResponse mapOrderJsonToOrderEntity(OrderJsonDto orderJson) {
		OrdersEntity entity = new OrdersEntity();
		entity.setId(orderJson.getId());
		if (orderJson.getStatus() != null && !orderJson.getStatus().isEmpty()) {
			OrderStatus statusEnum = OrderStatus.findEnum(orderJson.getStatus());
			if (statusEnum == null) {
				return new OrderResponse(OrderFailedStatus.INVALID_STATUS, HttpStatus.NOT_ACCEPTABLE);
			}
			entity.setStatus(statusEnum.ordinal());
		} else {
			entity.setStatus(0);
		}
		entity.setAddress(orderJson.getAddress());
//		entity.setBasket(orderJson.getBasket().toString());
		return new OrderResponse(entity);
	}

	private OrderJsonDto mapOrderStringToOrderJsonDto(String orderString) {
		OrderJsonDto orderJson;
		try {
			orderJson = new ObjectMapper().readValue(orderString, OrderJsonDto.class);
		} catch (IOException e) {
			throw new OrderValidationException("Error Occured while parsing order object",
					OrderFailedStatus.INVALID_ORDER);
		}
		return orderJson;
	}

	private OrderResponse updateOrCreateOrderEntity(OrdersEntity orderEntity) {
		OrdersEntity createdOrderEntity = orderEntity;
		if (orderEntity.getId() != null && orderEntity.getId() != 0) {
			Optional<OrdersEntity> foundOrdersEntity = ordersRepository.findById(orderEntity.getId());
			if (foundOrdersEntity == null || !foundOrdersEntity.isPresent()) {
				return new OrderResponse(OrderFailedStatus.INVALID_ORDER, HttpStatus.NOT_ACCEPTABLE);
			}
			createdOrderEntity = foundOrdersEntity.get();
		} else {
			if (orderEntity.getStatus() != null && orderEntity.getStatus() != 0) {
				return new OrderResponse(OrderFailedStatus.INVALID_STATUS, HttpStatus.NOT_ACCEPTABLE);
			}
			createdOrderEntity = new OrdersEntity();

			createdOrderEntity.setCreationDate( LocalDateTime.now() );
			createdOrderEntity.setUpdateDate(LocalDateTime.now() );
			// FIXME: HOW COME THE AMOUNT IS SET AS RANDOM VALUE??
			createdOrderEntity.setAmount(BigDecimal.valueOf(new Random().nextDouble()));
		}
		createdOrderEntity.setStatus(orderEntity.getStatus());

//		if (orderEntity.getBasketsEntities() != null && !orderEntity.getBasketsEntities().isEmpty()) {
////    		createdOrderEntity.setBasket(orderEntity.getBasketsEntity().toString());
//		}

		createdOrderEntity.setAddress(orderEntity.getAddress());
		ordersRepository.save(createdOrderEntity);

		return new OrderResponse(createdOrderEntity.getId(), createdOrderEntity.getAmount());
	}

	@Override
	public OrderResponse getOrderInfo(Long orderId) {
		if (ordersRepository.existsById(orderId)) {
			return new OrderResponse(getDetailedOrderInfo(orderId, true));
		}
		return new OrderResponse(OrderFailedStatus.INVALID_ORDER, HttpStatus.NOT_ACCEPTABLE);
	}

	private DetailedOrderRepObject getDetailedOrderInfo(Long orderId, boolean getItems) {
		OrdersEntity entity = ordersRepository.findById(orderId).get();
		List<BasketItem> itemsList = getBasketItems(orderId);
		DetailedOrderRepObject obj = new DetailedOrderRepObject();
		obj.setUserId(entity.getUserId());
		obj.setShopId(entity.getShopsEntity().getId());
		obj.setOrderId(orderId);
		obj.setCreatedAt(entity.getCreationDate());
		obj.setDeliveryDate(entity.getDeliveryDate());
		if (itemsList.size() > 0) {
			obj.setCurrency(itemsList.get(0).getCurrency());
			List<Integer> l = itemsList.stream().map(item -> item.getQuantity()).collect(Collectors.toList());
			obj.setTotalQuantity(l.stream().mapToInt(Integer::intValue).sum());
		}
		obj.setSubtotal(calculateOrderAmount(itemsList));
		obj.setShipping(new BigDecimal(0));
		obj.setTotal(obj.getShipping().add(obj.getSubtotal()));
		obj.setStatus(OrderStatus.findEnum(entity.getStatus()).name());
		//TODO set shipping address, shipping price
		ShippingAddress address = new ShippingAddress();
		address.setDetails(entity.getAddress());
        obj.setShippingAddress(address);
		if (getItems)
			obj.setItems(itemsList);
		return obj;
	}
	
	
	
	

	private List<BasketItem> getBasketItems(Long orderId) {
		List<BasketsEntity> itemsEntityList = basketRepository.findByOrdersEntity_Id(orderId);
		return itemsEntityList.stream()
							.map(this::toBasketItem)
							.collect(Collectors.toList());
	}
	
	
	
	

	private BasketItem toBasketItem(BasketsEntity entityItem) {
		StocksEntity stock = entityItem.getStocksEntity();
		ProductEntity product = stock.getProductVariantsEntity().getProductEntity();
		
		BasketItem item = new BasketItem();
		item.setProductId(product.getId());
		item.setName(product.getName());
		item.setPname(product.getPname());
		item.setStockId(stock.getId());
		item.setQuantity(entityItem.getQuantity().intValue());
		//TODO set item unit //
		item.setTotalPrice(entityItem.getPrice());
		item.setThumb( imgService.getProductCoverImage( product.getId() ));
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
	public List<DetailedOrderRepObject> getOrdersList(Long loggedUserId, String userToken, Long userId, Long storeId,
														 Long orgId, String status){
		List<OrdersEntity> ordersEntityList;
		List<DetailedOrderRepObject> ordersRep = new ArrayList<>();
		Integer statusId = -1;
		if (status != null){
			if ((OrderStatus.findEnum(status)) != null) {
				statusId = (OrderStatus.findEnum(status)).getValue();
			}
		}
		List<String> employeeUserRoles = employeeUserServiceHelper.getEmployeeUserRoles(loggedUserId);
		UserEntity user = userRepository.getByIdAndAuthenticationToken(loggedUserId, userToken);
		if (user == null){ // EmployeeUser
			EmployeeUserEntity employeeUser = employeeUserRepository.getByIdAndAuthenticationToken(loggedUserId, userToken);
			if (employeeUserRoles.contains("STORE_ADMIN") || employeeUserRoles.contains("STORE_MANAGER") || employeeUserRoles.contains("STORE_EMPLOYEE")){
				storeId = employeeUser.getShopId();
			} else if (employeeUserRoles.contains("ORGANIZATION_ADMIN") || employeeUserRoles.contains("ORGANIZATION_MANAGER") || employeeUserRoles.contains("ORGANIZATION_EMPLOYEE")) {
				orgId = employeeUser.getOrganizationId();
			}
			if (statusId != -1){
				if (orgId != null){
					if (userId != null && storeId != null) {
						ordersEntityList = ordersRepository.getOrdersEntityByShopsEntityIdAndStatusAndUserIdAndOrganizationEntityId(storeId, statusId, userId, orgId);
					} else if (userId != null) {
						ordersEntityList = ordersRepository.findByOrganizationEntityIdAndStatusAndUserId(orgId, statusId, userId);
					} else if (storeId != null){
						ordersEntityList = ordersRepository.getOrdersEntityByShopsEntityIdAndStatusAndOrganizationEntityId(storeId, statusId, orgId);
					} else {
						ordersEntityList = ordersRepository.findByOrganizationEntityIdAndStatus(orgId, statusId);
					}
				} else {
					if (userId != null && storeId != null) {
						ordersEntityList = ordersRepository.getOrdersEntityByShopsEntityIdAndStatusAndUserId(storeId, statusId, userId);
					} else if (userId != null) {
						ordersEntityList = ordersRepository.findByUserIdAndStatus(userId, statusId);
					} else if (storeId != null){
						ordersEntityList = ordersRepository.getOrdersEntityByShopsEntityIdAndStatus(storeId, statusId);
					} else {
						ordersEntityList = ordersRepository.findByStatus(statusId);
					}
				}
			} else {
				if (orgId != null){
					if (userId != null && storeId != null) {
						ordersEntityList = ordersRepository.getOrdersEntityByShopsEntityIdAndUserIdAndOrganizationEntityId(storeId, userId, orgId);
					} else if (userId != null) {
						ordersEntityList = ordersRepository.findByOrganizationEntityIdAndUserId(orgId, userId);
					} else if (storeId != null){
						ordersEntityList = ordersRepository.getOrdersEntityByShopsEntityIdAndOrganizationEntityId(storeId, orgId);
					} else {
						ordersEntityList = ordersRepository.findByOrganizationEntityId(orgId);
					}
				} else {
					if (userId != null && storeId != null) {
						ordersEntityList = ordersRepository.getOrdersEntityByShopsEntityIdAndUserId(storeId, userId);
					} else if (userId != null) {
						ordersEntityList = ordersRepository.findByUserId(userId);
					} else if (storeId != null){
						ordersEntityList = ordersRepository.findByshopsEntityId(storeId);
					} else {
						ordersEntityList = ordersRepository.findAll();
					}
				}
			}
		} else { // User
			if (statusId != -1){
				ordersEntityList = ordersRepository.findByUserIdAndStatus(loggedUserId, statusId);
			} else {
				ordersEntityList = ordersRepository.findByUserId(loggedUserId);
			}
		}
		for(OrdersEntity orders: ordersEntityList) {
			ordersRep.add(getDetailedOrderInfo(orders.getId(), false));
			//ordersEntityList.stream().map(order -> (OrderRepresentationObject) order.getRepresentation())
			//.collect(Collectors.toList());
		}
		return ordersRep;
	}
}
