package com.techelevator.tenmo.service;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.exception.DataNotFoundException;
import com.techelevator.tenmo.exception.InsufficientBalanceException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.math.BigDecimal;

@Service
public class TransferService {

    public static final Long PENDING_TRANSFER_STATUS = 1L;
    public static final Long APPROVE_TRANSFER_STATUS = 2L;
    public static final Long REJECTED_TRANSFER_STATUS = 3L;

    public static final Long REQUEST_TRANSFER_TYPE = 1L;
    public static final Long SEND_TRANSFER_TYPE = 2L;

    private AccountDao accountDao;

    private TransferDao transferDao;

    public TransferService(AccountDao accountDao, TransferDao transferDao){
        this.accountDao = accountDao;
        this.transferDao = transferDao;
    }

    @Transactional
    public Long transfer(Transfer transfer) throws Exception {
        transfer.setTransferStatusId(APPROVE_TRANSFER_STATUS);
        transfer.setTransferTypeId(SEND_TRANSFER_TYPE);

        Account accountFrom = accountDao.getAccountByUserId(transfer.getUserIdFrom());
        Account accountTo = accountDao.getAccountByUserId(transfer.getUserIdTo());

        if(accountFrom == null){
            throw new DataNotFoundException("Invalid User Id: "+transfer.getUserIdFrom());
        } else if (accountTo  == null) {
            throw new DataNotFoundException("Invalid User Id: "+transfer.getUserIdTo());
        }

        withdraw(transfer, accountFrom);
        deposit(transfer, accountTo);

        //Add transaction in Transfer mapping table
        Long id = transferDao.addTransfer(transfer);

        return id;
    }

    public BigDecimal withdraw(Transfer transfer, Account accountFrom) throws Exception {
        transfer.setAccountIdFrom(accountFrom.getAccountId());
        BigDecimal currentBalance = accountFrom.getBalance();
        BigDecimal amount = transfer.getAmount();
        if(currentBalance.compareTo(amount) == -1 ) {
            throw new InsufficientBalanceException("Insufficient Balance in account.");
        }

        BigDecimal finalBalanceFrom = currentBalance.subtract(amount);
        accountFrom.setBalance(finalBalanceFrom);
        accountDao.updateBalance(accountFrom);
        return finalBalanceFrom;
    }


    public BigDecimal deposit(Transfer transfer, Account accountTo){
        transfer.setAccountIdTo(accountTo.getAccountId());
        BigDecimal accountToBalance = accountTo.getBalance().add(transfer.getAmount());
        accountTo.setBalance(accountToBalance);
        accountDao.updateBalance(accountTo);
        return accountToBalance;
    }


    public TransferList[] listTransferHistory(Long id){

        return transferDao.getTransferHistoryList(id);
    }


    public Transfer getTransactionDetails(Long transferId) throws Exception{
        Transfer transfer = null;
        transfer =  transferDao.getTransferDetails(transferId);

        if(transfer == null){
            throw new DataNotFoundException("Invalid Transfer Id: "+  transferId);
        }

        return transfer;
    }


    public Long request(Transfer transfer) throws Exception {

        transfer.setTransferStatusId(PENDING_TRANSFER_STATUS);
        transfer.setTransferTypeId(REQUEST_TRANSFER_TYPE);

        Account accountFrom =  accountDao.getAccountByUserId(transfer.getUserIdFrom());
        Account accountTo =  accountDao.getAccountByUserId(transfer.getUserIdTo());

        if(accountFrom == null){
            throw new DataNotFoundException("Invalid User Id: " +transfer.getUserIdFrom());
        } else if (accountTo  == null) {
            throw new DataNotFoundException("Invalid User Id: " +transfer.getUserIdTo());        }

        transfer.setAccountIdFrom(accountFrom.getAccountId());
        transfer.setAccountIdTo(accountTo.getAccountId());

        return transferDao.addRequest(transfer);
    }

    public TransferList[] listPendingTransfers(Long id){

        return transferDao.pendingTransferList(id);
    }

    @Transactional
    public void updatePendingTransaction(Transfer transfer) throws Exception {
        Long id = transfer.getTransferId();
        Transfer dbTransferObj = transferDao.getTransferByTransferId(id);
        if(dbTransferObj == null){
            throw new DataNotFoundException("Invalid Transfer Id: " + id);
        }
        Long transferStatusId = 0L;

        if(transfer.getTransferStatus().equals("Approve")){
            transferStatusId = JdbcTransferDao.APPROVED_STATUS_ID;
            Account accountFrom = accountDao.getAccountById(dbTransferObj.getAccountIdFrom());
            withdraw(dbTransferObj, accountFrom);

            Account accountTo = accountDao.getAccountById(dbTransferObj.getAccountIdTo());
            deposit(dbTransferObj, accountTo);
            transferDao.approveOrRejectTransfer(id, transferStatusId);
        } else if (transfer.getTransferStatus().equals("Reject")){
            transferStatusId = JdbcTransferDao.REJECTED_STATUS_ID;
            transferDao.approveOrRejectTransfer(id, transferStatusId);
        }

    }

}
