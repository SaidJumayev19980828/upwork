package com.nasnav.controller;

import com.nasnav.dto.response.*;
import com.nasnav.service.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bank")
@AllArgsConstructor
@CrossOrigin("*")
public class BankController {
    private final BankAccountService bankAccountService;
    private final BankAccountActivityService bankAccountActivityService;
    private final BankOutsideTransactionService bankOutsideTransactionService;
    private final BankInsideTransactionService bankInsideTransactionService;
    private final BankReservationService bankReservationService;

    @PostMapping("/account")
    public BankAccountDetailsDTO createAccount(@RequestHeader(name = "User-Token", required = false) String token,
                                                @RequestBody BankAccountDetailsDTO dto) {
        return bankAccountService.createAccount(dto);
    }

    @GetMapping("/account")
    public BankAccountDTO getAccountById(@RequestHeader(name = "User-Token", required = false) String token) {
        return bankAccountService.getAccount();
    }

    @PutMapping("/account")
    public void lockOrUnlockAccount(@RequestHeader(name = "User-Token", required = false) String token,
                                     @RequestParam long accountId,
                                     @RequestParam boolean isLocked) {
        bankAccountService.lockOrUnlockAccount(accountId, isLocked);
    }

    @PostMapping("/account/setOpeningBalance")
    public void setOpeningBalanceForSpecificAccount(@RequestHeader(name = "User-Token", required = false) String token,
                                                    @RequestParam long accountId) {
        bankAccountService.setOpeningBalance(accountId);
    }

    @PostMapping("/transaction/out")
    public void outsideTransaction(@RequestHeader(name = "User-Token", required = false) String token,
                                   @RequestParam float amount,
                                   @RequestParam boolean isDeposit,
                                   @RequestParam String blockChainKey) {
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
    public PageImpl<BankActivityDetailsDTO> getAccountHistory(@RequestHeader(name = "User-Token", required = false) String token,
                                                              @RequestParam(required = false, defaultValue = "0") Integer start,
                                                              @RequestParam(required = false, defaultValue = "10") Integer count) {
        return bankAccountActivityService.getHistory(start, count);
    }

    @GetMapping("/account/summary")
    public BankBalanceSummaryDTO getAccountSummary(@RequestHeader(name = "User-Token", required = false) String token) {
        return bankAccountActivityService.getAccountSummary();
    }

}
