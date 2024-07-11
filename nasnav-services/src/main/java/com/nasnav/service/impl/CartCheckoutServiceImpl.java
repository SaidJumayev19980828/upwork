package com.nasnav.service.impl;

import com.nasnav.dao.ReferralCodeRepo;
import com.nasnav.dao.StoreCheckoutsRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.cart.StoreCheckoutDto;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.enumerations.ReferralType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.payments.cash.PaymentService;
import com.nasnav.payments.misc.Commons;
import com.nasnav.persistence.*;
import com.nasnav.service.*;
import com.nasnav.service.otp.OtpService;
import com.nasnav.service.otp.OtpType;
import com.nasnav.service.yeshtery.YeshteryOtpService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class CartCheckoutServiceImpl implements CartCheckoutService {

	private static final Logger checkoutLogger = LogManager.getLogger(CartCheckoutService.class);

	private final SecurityService securityService;
	private final OrderService orderService;
	private final LoyaltyTierServiceImp tierServiceImp;
	private final UserRepository userRepository;
	private final PromotionsService promotionsService;
	private final OtpService otpService;
	private final MailService mailService;
	private final OrganizationService organizationService;
	private final Commons paymentCommons;


	@Autowired
	@Qualifier("storeCashPaymentService")
	private PaymentService storeCashPaymentService;

	public static final String OTP_TEMPLATE = "mail_templates/otp_template.html";
	public static final String OTP_PARAMETER = "#OTP#";
	private final StoreCheckoutsRepository storeCheckoutsRepository;
	@Autowired
	private ReferralCodeRepo referralCodeRepo;


	@Override
	public void initiateCheckout(Long userId) {
		UserEntity userEntity = userRepository.findById(userId)
				.orElseThrow(()-> new RuntimeBusinessException(NOT_FOUND, U$0001,userId));
		UserOtpEntity userOtp = otpService.createUserOtp(userEntity, OtpType.CHECKOUT);
		OrganizationEntity organization = organizationService.getOrganizationById(securityService.getCurrentUserOrganizationId());
		try {
			mailService.send(organization.getName(), userEntity.getEmail(), "Checkout OTP", OTP_TEMPLATE, Map.of(OTP_PARAMETER, userOtp.getOtp()));
		} catch (Exception e) {
			checkoutLogger.error(e, e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR,  GEN$0003, e.getMessage());
		}
		storeCheckoutsRepository.deleteByEmployeeId(userEntity.getId());
	}


	@Override
	public Order checkoutCart(CartCheckoutDTO dto) {
		BaseUserEntity userAuthed = securityService.getCurrentUser();
		Long userId;
		if(userAuthed instanceof EmployeeUserEntity) {
			userId= getCustomerId(dto);
			dto.setCreatedByEmployeeId(userAuthed.getId());
		}else {
			userId = userAuthed.getId();
		}
		UserEntity userEntity = userRepository.findById(userId).orElseThrow(()-> new RuntimeBusinessException(NOT_FOUND, U$0001,userId));
		if(Objects.nonNull(dto.getCreatedByEmployeeId()) && dto.getCreatedByEmployeeId() > 0) {
			otpService.validateOtp(dto.getOtp(), userEntity, OtpType.CHECKOUT);
		}

		Order order = orderService.createOrder(dto,userEntity);
		if(dto.getPromoCode() != null && !referralCodeRepo.existsByReferralCodeAndReferralType(dto.getPromoCode(), ReferralType.INFLUENCER)){
			promotionsService.updatePromoUsageAndCheckLimit(dto.getPromoCode());
		}
		return order;
	}

	@Override
	public Order completeCheckout(CartCheckoutDTO dto) throws BusinessException {
		Order checkoutedOutOrder = checkoutCart(dto);
		return orderService.getMetaOrder(checkoutedOutOrder.getOrderId(), false);
	}

	public StoreCheckoutDto storeCheckout(Long userId) {
		StoreCheckoutsEntity storeCheckoutsEntity = new StoreCheckoutsEntity();
		storeCheckoutsEntity.setEmployeeId(securityService.getCurrentUser().getId());
		storeCheckoutsEntity.setUserId(userId);
		storeCheckoutsEntity.setShopId(securityService.getCurrentUserShopId());
		storeCheckoutsEntity.setOrganizationId(securityService.getCurrentUserOrganizationId());
		storeCheckoutsRepository.save(storeCheckoutsEntity);

		return StoreCheckoutDto.builder()
				.userId(userId)
				.build();
	}

	@Override
	public Order completeYeshteryCheckout(CartCheckoutDTO dto) throws BusinessException {
		validateCartCheckoutDTO(dto);
		Order checkoutedOutOrder = checkoutYeshteryCart(dto);
		storeCashPaymentService.finalize(
				storeCashPaymentService.createPaymentForOrder(checkoutedOutOrder.getOrderId(),
				orderService.getMetaOrderTotalValue(checkoutedOutOrder.getOrderId()),
				checkoutedOutOrder.getUserId()), true);  /// only workin for cash payment we need for MVR
		return orderService.getMetaOrder(checkoutedOutOrder.getOrderId(), false);
	}

	private void validateCartCheckoutDTO(CartCheckoutDTO dto) {
		if (dto.getSelectedStockIds() == null || dto.getSelectedStockIds().isEmpty())
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, NOTSELECTEDSTOCKIDS);
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
