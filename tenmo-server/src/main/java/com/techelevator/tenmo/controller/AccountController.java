package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.service.AccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;


@PreAuthorize("isAuthenticated()")
@RestController
public class AccountController {

    private AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @RequestMapping(path = "/users/{id}/balance", method = RequestMethod.GET)
    public BigDecimal getBalance (@PathVariable Long id){

        return accountService.getBalance(id);
    }
}
