package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferList;
import com.techelevator.tenmo.service.TransferService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@PreAuthorize("isAuthenticated()")
@RestController
public class TransferController {

    private TransferService transferService;

    public TransferController(TransferService transferService){
        this.transferService = transferService;
    }

    @PostMapping(path = "/transfer")
    public Long makeTransfer(@RequestBody Transfer transfer) throws Exception {

        return transferService.transfer(transfer);
    }

    @PostMapping(path = "/request")
    public Long makeRequest(@RequestBody Transfer transfer) throws Exception {

        return transferService.request(transfer);
    }

    @RequestMapping(path = "/users/{id}/transferHistory", method = RequestMethod.GET)
    public TransferList[] listTransactionHistory (@PathVariable Long id){

        return transferService.listTransferHistory(id);
    }

    @RequestMapping(path = "/users/transferHistory/{transferId}", method = RequestMethod.GET)

    public Transfer getTransferDetails( @PathVariable Long transferId) throws Exception {
        return transferService.getTransactionDetails(transferId);

    }

    @RequestMapping(path = "/users/{id}/pendingTransactions", method = RequestMethod.GET)
    public TransferList[] pendingTransactionList (@PathVariable Long id){

        return transferService.listPendingTransfers(id);
    }

    @RequestMapping(path = "/users/{id}/pendingTransactions/approveOrReject", method = RequestMethod.PUT)
    public void approveOrReject (@PathVariable Long id, @RequestBody Transfer transfer) throws Exception {

        transferService.updatePendingTransaction(transfer);
    }

}
