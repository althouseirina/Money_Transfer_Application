package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;

@Component
public class JdbcAccountDao implements AccountDao{

    private JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate){

        this.jdbcTemplate = jdbcTemplate;
    }



    @Override
    public BigDecimal getBalance(Long userId) {


        BigDecimal balance = BigDecimal.ZERO;
        String sql = "SELECT * FROM accounts WHERE user_id = ?";
        Account account = (Account) jdbcTemplate.queryForObject(sql, new Object[]{userId}, new BeanPropertyRowMapper<>(Account.class));

        if(account != null){
            balance = account.getBalance();
        }

        return balance;
    }

    @Override
    public Account getAccountByUserId(Long userId) {
        String sql = "Select * from accounts where user_id = ?";
        SqlRowSet accountRowSwt = jdbcTemplate.queryForRowSet(sql, userId);
        if(accountRowSwt.next()){
            return mapRowToAccount(accountRowSwt);
        }
        return null;
    }

    @Override
    public Account getAccountById(Long accountId) {
        String sql = "Select * from accounts where account_id = ?";
        SqlRowSet accountRowSwt = jdbcTemplate.queryForRowSet(sql, accountId);
        if(accountRowSwt.next()){
            return mapRowToAccount(accountRowSwt);
        }
        return null;
    }

    @Override
    public void updateBalance(Account account) {

        String sql = "UPDATE accounts SET user_id = ?, balance = ? WHERE account_id = ?";
        jdbcTemplate.update(sql, account.getUserId(), account.getBalance(), account.getAccountId());

    }

        private Account  mapRowToAccount(SqlRowSet rowSet) {

        Account account = new Account();
        account.setAccountId(rowSet.getLong("account_id"));
        account.setUserId(rowSet.getLong("user_id"));
        account.setBalance(rowSet.getBigDecimal("balance"));

        return account;
    }



}
