package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class TransferList {

    Long transferId;
    String toFromName;
    BigDecimal amount;

    public TransferList(){

    }

    public TransferList(Long transferId, String toFromName, BigDecimal amount) {
        this.transferId = transferId;
        this.toFromName = toFromName;
        this.amount = amount;
    }

    public Long getTransferId() {
        return transferId;
    }

    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }

    public String getToFromName() {
        return toFromName;
    }

    public void setToFromName(String toFromName) {
        this.toFromName = toFromName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
