package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;

public interface AccountDao {

    BigDecimal getBalance(Long userId);

    Account getAccountByUserId(Long id);

    Account getAccountById(Long accountId);

    void updateBalance(Account account);
}
