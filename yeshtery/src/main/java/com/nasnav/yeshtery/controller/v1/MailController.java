package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.request.mail.AbandonedCartsMail;
import com.nasnav.service.CartService;
import com.nasnav.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(MailController.API_PATH)
public class MailController {
    static final String API_PATH = YeshteryConstants.API_PATH +"/mail";

    @Autowired
    private CartService cartService;

    @Autowired
    private WishlistService wishlistService;

    @PostMapping(value = "/cart/abandoned")
    public void sendAbandonedCartEmails(@RequestHeader(name="User-Token", required=false) String userToken,
                                        @RequestBody AbandonedCartsMail dto) {
        cartService.sendAbandonedCartEmails(dto);
    }

    @PostMapping(value = "/wishlist/stock")
    public void sendRestockedWishlistEmails(@RequestHeader(name="User-Token", required=false) String userToken) {
        wishlistService.sendRestockedWishlistEmails();
    }
}
