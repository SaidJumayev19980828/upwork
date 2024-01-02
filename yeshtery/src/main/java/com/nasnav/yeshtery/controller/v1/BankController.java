package com.nasnav.yeshtery.controller.v1;


import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.response.BankAccountDTO;
import com.nasnav.dto.response.BankAccountDetailsDTO;
import com.nasnav.dto.response.BankActivityDetailsDTO;
import com.nasnav.dto.response.BankBalanceSummaryDTO;
import com.nasnav.dto.response.BankReservationDTO;
import com.nasnav.dto.response.DepositBlockChainRequest;
import com.nasnav.persistence.BankAccountEntity;
import com.nasnav.service.BankAccountActivityService;
import com.nasnav.service.BankAccountService;
import com.nasnav.service.BankInsideTransactionService;
import com.nasnav.service.BankOutsideTransactionService;
import com.nasnav.service.BankReservationService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = BankController.API_PATH, produces = APPLICATION_JSON_VALUE)
@AllArgsConstructor
@CrossOrigin("*")
public class BankController {
    static final String API_PATH = YeshteryConstants.API_PATH + "/bank";
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

    @PostMapping("/pay")
    public void pay(@RequestHeader(name = "User-Token", required = false) String token,
                    @RequestParam long amount) {
        bankInsideTransactionService.pay(amount);
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


    @PostMapping("/assign-wallet-address")
    public BankAccountEntity assignWalletAddress(@RequestHeader(name = "User-Token", required = false) String token, @RequestParam String walletAddress) {
        return bankAccountService.assignWalletAddress(walletAddress);
    }

    @PostMapping("/transaction/deposit")
    public void depositCoins(@RequestHeader(name = "User-Token", required = false) String token,
                             @RequestParam String txHash) {
        bankOutsideTransactionService.depositCoins(txHash);
    }

    @PostMapping(value = "/deposit/bc", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void depositFromBlockChain(@RequestBody DepositBlockChainRequest depositRequest) {
        bankOutsideTransactionService.depositCoinsFromBC(depositRequest);
    }

}
