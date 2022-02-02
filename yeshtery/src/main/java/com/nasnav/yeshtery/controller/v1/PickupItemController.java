package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.response.PickupItem;
import com.nasnav.service.PickupItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/pickup")
public class PickupItemController {

    @Autowired
    private PickupItemService pickupItemService;

    @GetMapping(produces=APPLICATION_JSON_VALUE)
    public Set<PickupItem> getPickupItems(@RequestHeader(name = "User-Token", required = false) String userToken) {
        return pickupItemService.getPickupItems();
    }

    @PostMapping(value = "/into_cart", consumes = APPLICATION_JSON_VALUE)
    public void movePickupItemToCartItem(@RequestHeader(name = "User-Token", required = false) String userToken,
                                         @RequestBody(required = false) Set<Long> items) {
        pickupItemService.movePickupItemToCartItem(items);
    }

    @PostMapping(value = "/into_pickup", consumes = APPLICATION_JSON_VALUE)
    public void moveCartItemToPickupItem(@RequestHeader(name = "User-Token", required = false) String userToken,
                                         @RequestBody Set<Long> items) {
        pickupItemService.moveCartItemToPickupItem(items);
    }
}
