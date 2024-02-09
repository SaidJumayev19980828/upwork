package com.nasnav.service.impl;

import com.nasnav.dao.UserRepository;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.LoyaltyTierEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.CartCheckoutService;
import com.nasnav.service.OrderService;
import com.nasnav.service.PromotionsService;
import com.nasnav.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import static com.nasnav.exceptions.ErrorCodes.NOTIUSER$0006;
import static com.nasnav.exceptions.ErrorCodes.U$0001;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CartCheckoutServiceImpl implements CartCheckoutService {

	private final SecurityService securityService;
	private final OrderService orderService;
	private final LoyaltyTierServiceImp tierServiceImp;
	private final UserRepository userRepository;
	private final PromotionsService promotionsService;

	@Override
	public Order checkoutCart(CartCheckoutDTO dto) {
		BaseUserEntity userAuthed = securityService.getCurrentUser();
		Long userId;
		if(userAuthed instanceof EmployeeUserEntity) {
			userId= getCustomerId(dto);
		}else {
			userId = userAuthed.getId();
		}
		UserEntity userEntity = userRepository.findById(userId).orElseThrow(()-> new RuntimeBusinessException(NOT_FOUND, U$0001,userId));

		Order order = orderService.createOrder(dto,userEntity);
		if(dto.getPromoCode() != null ){
			promotionsService.updatePromoUsageAndCheckLimit(dto.getPromoCode());
		}
		return order;
	}

	@Override
	public Order checkoutYeshteryCart(CartCheckoutDTO dto) {
		return orderService.createYeshteryOrder(dto);
	}

	public Long getCustomerId( CartCheckoutDTO dto) {
        if(dto.getCustomerId() != null) {
            return dto.getCustomerId();
        }
        throw new RuntimeBusinessException(NOT_FOUND, NOTIUSER$0006);
    }

}
