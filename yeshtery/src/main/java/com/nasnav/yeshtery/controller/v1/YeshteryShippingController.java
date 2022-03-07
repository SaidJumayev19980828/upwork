package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.service.ShippingManagementService;
import com.nasnav.yeshtery.YeshteryConstants;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(YeshteryShippingController.API_PATH)
@Tag(name = "Yeshtery Shipping Controller")
@CrossOrigin("*")
@EnableJpaRepositories
public class YeshteryShippingController {

    static final String API_PATH = YeshteryConstants.API_PATH +"/shipping";

    @Autowired
    private ShippingManagementService shippingService;

    @GetMapping(path = "/offers", produces= APPLICATION_JSON_VALUE)
    public List<ShippingOfferDTO> getShippingOffers(@RequestHeader(name = "User-Token", required = false) String token,
                                                    @RequestParam("customer_address") Long customerAddress,
                                                    @RequestParam("payment_method_id") String paymentMethodId,
                                                    @RequestParam("shipping_service_id") String shippingServiceId) {
        return shippingService.getYeshteryShippingOffers(customerAddress, paymentMethodId, shippingServiceId);
    }
}
