package com.nasnav.service.impl;

import java.util.List;

import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import org.springframework.stereotype.Service;
import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.service.CartCheckoutService;
import com.nasnav.service.OrderService;
import com.nasnav.service.SecurityService;
import lombok.RequiredArgsConstructor;

import static com.nasnav.exceptions.ErrorCodes.O$CRT$0001;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@RequiredArgsConstructor
public class CartCheckoutServiceImpl implements CartCheckoutService {

	private final SecurityService securityService;
	private final OrderService orderService;
	private final LoyaltyTierServiceImp tierServiceImp;
	private final UserRepository userRepository;

	@Override
	public Order checkoutCart(CartCheckoutDTO dto) {
		BaseUserEntity userAuthed = securityService.getCurrentUser();
		if(userAuthed instanceof EmployeeUserEntity) {
			throw new RuntimeBusinessException(FORBIDDEN, O$CRT$0001);
		}
		Long userId = userAuthed.getId();
		LoyaltyTierEntity loyaltyTierEntity = tierServiceImp.getTierByAmount(orderService.countOrdersByUserId(userId));
		UserEntity userEntity = userRepository.findById(userId).orElseThrow();
		userEntity.setTier(loyaltyTierEntity);
		userRepository.save(userEntity);
		return orderService.createOrder(dto);
	}

	@Override
	public Order checkoutYeshteryCart(CartCheckoutDTO dto) {
		return orderService.createYeshteryOrder(dto);
	}
}
