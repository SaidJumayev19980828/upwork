package com.nasnav.controller;

import com.nasnav.dto.BankAccountDTO;
import com.nasnav.service.BankAccountService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bank")
@AllArgsConstructor
public class BankController {
    private final BankAccountService bankAccountService;

    @PostMapping("/account")
    private BankAccountDTO createAccount(@RequestHeader(name = "User-Token", required = false) String token,
                                         @RequestBody BankAccountDTO dto){
        return bankAccountService.createAccount(dto);
    }
}
