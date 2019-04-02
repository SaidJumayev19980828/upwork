package com.nasnav.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.nasnav.response.exception.OrderValidationException;

@Service
public class OrderServiceImpl implements OrderService {

	private final OrdersRepository ordersRepository;

	private final BasketRepository basketRepository;

	private final StockRepository stockRepository;
	
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

		if (orderJson.getStatus() != null && OrderStatus.findEnum(orderJson.getStatus()) == null) {
			return new OrderResponse(OrderFailedStatus.INVALID_STATUS, HttpStatus.NOT_ACCEPTABLE);
		}

		if (orderJson.getId() == null || orderJson.getId() == 0) {
			return createNewOrderAndBasketItems(orderJson, userId);
		} else {
			return updateCurrentOrderStatus(orderJson);
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
		List<StocksEntity> stocksEntites = new ArrayList<>(orderJsonDto.getBasket().size());

		orderJsonDto.getBasket().forEach(basketItem -> {
			Optional<StocksEntity> optionalStocksEntity = stockRepository.findById(basketItem.getStockId());

			if (optionalStocksEntity == null || !optionalStocksEntity.isPresent()
					|| basketItem.getQuantity() > optionalStocksEntity.get().getQuantity()) {
				// stock Id is invalid or available quantity is less than required
				return;
			}
			stocksEntites.add(optionalStocksEntity.get());
		});

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
		
		for(BasketItem basketItem : orderJsonDto.getBasket()) {
			StocksEntity stocksEntity = stocksEntites.stream().filter(stock->stock.getId()==basketItem.getStockId()).findFirst().get();
			BasketsEntity basketsEntity = new BasketsEntity();
			basketsEntity.setStocksEntity(stocksEntity);
			basketsEntity.setOrdersEntity(orderEntity);
			//TODO make sure price here means item price multiplied by quantity
			basketsEntity.setPrice(new BigDecimal(basketItem.getQuantity()).multiply(stocksEntity.getPrice()));
			basketsEntity.setQuantity(new BigDecimal(basketItem.getQuantity()));
			
			//TODO how currency determined for specific order
			basketsEntity.setCurrency(TransactionCurrency.EGP.getValue());
			
			basketRepository.save(basketsEntity);
		}

		return new OrderResponse(orderEntity.getId(), orderEntity.getAmount());

	}

	private BigDecimal calculateOrderAmount(OrderJsonDto orderJsonDto, List<StocksEntity> stocksEntites) {

		BigDecimal totalAmount = new BigDecimal(0);
		orderJsonDto.getBasket().stream().forEach(basketItem -> {

			BigDecimal price = stocksEntites.stream().filter(stock -> stock.getId() == basketItem.getStockId())
					.findFirst().get().getPrice();
			// TODO Discount rules to be applied if required

			totalAmount.add(price.multiply(new BigDecimal(basketItem.getQuantity())));
		});

		return totalAmount;
	}

	private OrderResponse updateCurrentOrderStatus(OrderJsonDto orderJsonDto) {

		return null;
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

			createdOrderEntity.setCreationDate(new Date(System.currentTimeMillis()));
			createdOrderEntity.setUpdateDate(new Date(System.currentTimeMillis()));

			createdOrderEntity.setAmount(new BigDecimal(new Random().nextDouble()));
		}
		createdOrderEntity.setStatus(orderEntity.getStatus());

		if (orderEntity.getBasketsEntities() != null && !orderEntity.getBasketsEntities().isEmpty()) {
//    		createdOrderEntity.setBasket(orderEntity.getBasketsEntity().toString());
		}

		createdOrderEntity.setAddress(orderEntity.getAddress());
		ordersRepository.save(createdOrderEntity);

		return new OrderResponse(createdOrderEntity.getId(), createdOrderEntity.getAmount());
	}

	@Override
	public OrderResponse getOrderInfo(Long orderId) {
		Optional<OrdersEntity> entity = ordersRepository.findById(orderId);
		if (entity.isPresent()) {
			return new OrderResponse(entity.get());
		}
		return new OrderResponse(OrderFailedStatus.UNAUTHENTICATED, HttpStatus.NOT_ACCEPTABLE);
	}

}
