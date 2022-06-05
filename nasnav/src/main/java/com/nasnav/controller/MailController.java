package com.nasnav.controller;

import com.nasnav.dto.request.mail.AbandonedCartsMail;
import com.nasnav.service.CartService;
import com.nasnav.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
public class MailController {

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
