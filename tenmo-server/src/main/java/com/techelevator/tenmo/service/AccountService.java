package com.techelevator.tenmo.service;

import com.techelevator.tenmo.dao.AccountDao;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {

    private AccountDao accountDao;

    public AccountService(AccountDao accountDao){
        this.accountDao = accountDao;
    }

    public BigDecimal getBalance(Long userId) {
        return accountDao.getBalance(userId);
    }
}
