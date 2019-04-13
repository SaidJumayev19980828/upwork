package com.nasnav.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.BasketItem;
import com.nasnav.dto.OrderJsonDto;
import com.nasnav.dto.TransactionCurrency;
import com.nasnav.enumerations.OrderFailedStatus;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.response.OrderResponse;

@Service
public class OrderServiceImpl implements OrderService {

	private final OrdersRepository ordersRepository;

	private final BasketRepository basketRepository;

	private final StockRepository stockRepository;

	private final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class.getName());

	@Autowired
	public OrderServiceImpl(OrdersRepository ordersRepository, BasketRepository basketRepository,
			StockRepository stockRepository) {
		this.ordersRepository = ordersRepository;
		this.stockRepository = stockRepository;
		this.basketRepository = basketRepository;
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
		orderEntity.setCreationDate(new Date());
		// TODO ordersEntity.setPayment_type(payment_type);
		orderEntity.setShopsEntity(stocksEntites.get(0).getShopsEntity());
		orderEntity.setStatus(OrderStatus.NEW.getValue());
		orderEntity.setUpdateDate(new Date());
		orderEntity.setUser_id(userId);

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

					if (optionalStocksEntity == null || !optionalStocksEntity.isPresent()
							|| basketItem.getQuantity() > optionalStocksEntity.get().getQuantity()
							|| optionalStocksEntity.get().getPrice().doubleValue() <= 0) {
						// stock Id is invalid or available quantity is less than required
						return;
					}
					stocksEntites.add(optionalStocksEntity.get());
				});

		// TODO check for pending orders logic and what to do
		return stocksEntites;
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

			if (optionalStocksEntity == null || !optionalStocksEntity.isPresent()
					|| basketItem.getQuantity() > optionalStocksEntity.get().getQuantity()) {
				// stock Id is invalid or available quantity is less than required
				log.error("Stock Id {} is invalid", basketItem.getStockId());
				return;
			}
			stocksEntites.add(optionalStocksEntity.get());
		});

		orderEntity.setAmount(calculateOrderAmount(orderJsonDto, stocksEntites));
		orderEntity.setShopsEntity(stocksEntites.get(0).getShopsEntity());
		orderEntity.setUpdateDate(new Date());
		orderEntity = ordersRepository.save(orderEntity);

		addItemsToBasket(orderJsonDto, orderEntity, stocksEntites);

		return new OrderResponse(orderEntity.getId(), orderEntity.getAmount());

	}

	private OrderResponse updateCurrentOrderStatus(OrderJsonDto orderJsonDto, OrdersEntity orderEntity,
			OrderStatus newStatus, OrderStatus currentOrderStatus) {

		if ((newStatus.getValue() >= currentOrderStatus.getValue())
				&& (orderJsonDto.getBasket() == null || orderJsonDto.getBasket().isEmpty())) {
			// TODO check with Marek if this valid from business perspective
			log.error("Cannot update order staus to previous status");
			return new OrderResponse(OrderFailedStatus.INVALID_STATUS, HttpStatus.BAD_REQUEST);
		}

		orderEntity.setStatus(newStatus.getValue());
		orderEntity.setUpdateDate(new Date());
		orderEntity = ordersRepository.save(orderEntity);

		return new OrderResponse(orderEntity.getId(), orderEntity.getAmount());
	}

	private void addItemsToBasket(OrderJsonDto orderJsonDto, OrdersEntity orderEntity,
			List<StocksEntity> stocksEntites) {

		for (BasketItem basketItem : orderJsonDto.getBasket()) {
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

	/*
	 * private OrderResponse mapOrderJsonToOrderEntity(OrderJsonDto orderJson) {
	 * OrdersEntity entity = new OrdersEntity(); entity.setId(orderJson.getId()); if
	 * (orderJson.getStatus() != null && !orderJson.getStatus().isEmpty()) {
	 * OrderStatus statusEnum = OrderStatus.findEnum(orderJson.getStatus()); if
	 * (statusEnum == null) { return new
	 * OrderResponse(OrderFailedStatus.INVALID_STATUS, HttpStatus.NOT_ACCEPTABLE); }
	 * entity.setStatus(statusEnum.ordinal()); } else { entity.setStatus(0); }
	 * entity.setAddress(orderJson.getAddress()); //
	 * entity.setBasket(orderJson.getBasket().toString()); return new
	 * OrderResponse(entity); }
	 */
	/*
	 * private OrderJsonDto mapOrderStringToOrderJsonDto(String orderString) {
	 * OrderJsonDto orderJson; try { orderJson = new
	 * ObjectMapper().readValue(orderString, OrderJsonDto.class); } catch
	 * (IOException e) { throw new
	 * OrderValidationException("Error Occured while parsing order object",
	 * OrderFailedStatus.INVALID_ORDER); } return orderJson; }
	 */

	/*
	 * private OrderResponse updateOrCreateOrderEntity(OrdersEntity orderEntity) {
	 * OrdersEntity createdOrderEntity = orderEntity; if (orderEntity.getId() !=
	 * null && orderEntity.getId() != 0) { Optional<OrdersEntity> foundOrdersEntity
	 * = ordersRepository.findById(orderEntity.getId()); if (foundOrdersEntity ==
	 * null || !foundOrdersEntity.isPresent()) { return new
	 * OrderResponse(OrderFailedStatus.INVALID_ORDER, HttpStatus.NOT_ACCEPTABLE); }
	 * createdOrderEntity = foundOrdersEntity.get(); } else { if
	 * (orderEntity.getStatus() != null && orderEntity.getStatus() != 0) { return
	 * new OrderResponse(OrderFailedStatus.INVALID_STATUS,
	 * HttpStatus.NOT_ACCEPTABLE); } createdOrderEntity = new OrdersEntity();
	 * 
	 * createdOrderEntity.setCreationDate(new Date(System.currentTimeMillis()));
	 * createdOrderEntity.setUpdateDate(new Date(System.currentTimeMillis())); //
	 * FIXME: HOW COME THE AMOUNT IS SET AS RANDOM VALUE??
	 * createdOrderEntity.setAmount(BigDecimal.valueOf(new Random().nextDouble()));
	 * } createdOrderEntity.setStatus(orderEntity.getStatus());
	 * 
	 * // if (orderEntity.getBasketsEntities() != null &&
	 * !orderEntity.getBasketsEntities().isEmpty()) { ////
	 * createdOrderEntity.setBasket(orderEntity.getBasketsEntity().toString()); // }
	 * 
	 * createdOrderEntity.setAddress(orderEntity.getAddress());
	 * ordersRepository.save(createdOrderEntity);
	 * 
	 * return new OrderResponse(createdOrderEntity.getId(),
	 * createdOrderEntity.getAmount()); }
	 */
	@Override
	public OrderResponse getOrderInfo(Long orderId) {
		Optional<OrdersEntity> entity = ordersRepository.findById(orderId);
		if (entity.isPresent()) {
			return new OrderResponse(entity.get());
		}
		return new OrderResponse(OrderFailedStatus.INVALID_ORDER, HttpStatus.NOT_ACCEPTABLE);
	}

}
