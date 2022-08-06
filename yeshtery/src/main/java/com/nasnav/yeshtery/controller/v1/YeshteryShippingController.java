package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.service.ShippingManagementService;
import com.nasnav.commons.YeshteryConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(YeshteryShippingController.API_PATH)
@CrossOrigin("*")
@EnableJpaRepositories
public class YeshteryShippingController {

    static final String API_PATH = YeshteryConstants.API_PATH +"/shipping";

    @Autowired
    private ShippingManagementService shippingService;

    @GetMapping(path = "/offers", produces= APPLICATION_JSON_VALUE)
    public List<ShippingOfferDTO> getShippingOffers(@RequestHeader(name = "User-Token", required = false) String token,
                                                    @RequestParam(value = "customer_address", required = false, defaultValue = "-1") Long customerAddress,
                                                    @RequestParam(value = "payment_method_id", required = false, defaultValue = "") String paymentMethodId,
                                                    @RequestParam(value = "shipping_service_id", required = false, defaultValue = "") String shippingServiceId) {
        return shippingService.getYeshteryShippingOffers(customerAddress, paymentMethodId, shippingServiceId);
    }
}
