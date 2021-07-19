package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferList;

public interface TransferDao {

    Long addTransfer(Transfer transfer);

    Long addRequest(Transfer transfer);

     TransferList[] getTransferHistoryList(Long userId);

     Transfer getTransferDetails(Long transferId);

     TransferList[] pendingTransferList(Long userId);

    void approveOrRejectTransfer(Long transferId, Long statusId);

    Transfer getTransferByTransferId(Long transferId);

}


