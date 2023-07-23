package com.nasnav.controller;

import com.nasnav.dto.response.BankAccountDTO;
import com.nasnav.dto.response.BankAccountDetailsDTO;
import com.nasnav.dto.response.BankActivityDTO;
import com.nasnav.dto.response.BankReservationDTO;
import com.nasnav.service.*;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bank")
@AllArgsConstructor
public class BankController {
    private final BankAccountService bankAccountService;
    private final BankAccountActivityService bankAccountActivityService;
    private final BankOutsideTransactionService bankOutsideTransactionService;
    private final BankInsideTransactionService bankInsideTransactionService;
    private final BankReservationService bankReservationService;

    @PostMapping("/account") // org admin
    public BankAccountDetailsDTO createAccount(@RequestHeader(name = "User-Token", required = false) String token,
                                                @RequestBody BankAccountDetailsDTO dto) {
        return bankAccountService.createAccount(dto);
    }

    @GetMapping("/account") // org admin / customer
    public BankAccountDTO getAccountById(@RequestHeader(name = "User-Token", required = false) String token) {
        return bankAccountService.getAccount();
    }

    @PutMapping("/account") // nasnav admin
    public void lockOrUnlockAccount(@RequestHeader(name = "User-Token", required = false) String token,
                                     @RequestParam long accountId,
                                     @RequestParam boolean isLocked) {
        bankAccountService.lockOrUnlockAccount(accountId, isLocked);
    }

    @PostMapping("/account/setOpeningBalance") //nasnav admin
    public void setOpeningBalanceForSpecificAccount(@RequestHeader(name = "User-Token", required = false) String token,
                                                    @RequestParam long accountId) {
        bankAccountService.setOpeningBalance(accountId);
    }

    @PostMapping("/transaction/out") //any user
    public void outsideTransaction(@RequestHeader(name = "User-Token", required = false) String token,
                                   @RequestParam long amount,
                                   @RequestParam boolean isDeposit,
                                   @RequestParam long blockChainKey) {
        bankOutsideTransactionService.depositOrWithdrawal(amount, isDeposit, blockChainKey);
    }

    @PostMapping("/transaction/in")
    public void insideTransaction(@RequestHeader(name = "User-Token", required = false) String token,
                                  @RequestParam long receiverAccountId,
                                  @RequestParam long amount) {
        bankInsideTransactionService.transfer(receiverAccountId, amount);
    }

    @GetMapping("/account/reservations")
    public List<BankReservationDTO> getReservations(@RequestHeader(name = "User-Token", required = false) String token,
                                                    @RequestParam(required = false) Boolean isFulfilled) {
        return bankReservationService.getReservations(isFulfilled);
    }

    @GetMapping("/account/reservation")
    public BankReservationDTO getReservationById(@RequestHeader(name = "User-Token", required = false) String token,
                                                       @RequestParam long reservationId) {
        return bankReservationService.getReservationById(reservationId);
    }

    @PutMapping("/account/reservation/fulfill")
    public void fulfilReservation(@RequestHeader(name = "User-Token", required = false) String token,
                                  @RequestParam long reservationId) {
        bankReservationService.fulfilReservation(reservationId);
    }

    @GetMapping("/account/history")
    public BankActivityDTO getAccountHistory(@RequestHeader(name = "User-Token", required = false) String token,
                                             @RequestParam long accountId) {
        return bankAccountActivityService.getHistory(accountId);
    }

}
