package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferList;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao{

    public static final String PENDING_STATUS = "Pending";
    public static final String APPROVED_STATUS = "Approved";
    public static final String REJECTED_STATUS = "Rejected";

    public static final Long APPROVED_STATUS_ID = 2L;
    public static final Long REJECTED_STATUS_ID = 3L;

    public static final String REQUEST_TYPE = "Request";
    public static final String SEND_TYPE = "Send";





    private JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate){

        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public Long addTransfer(Transfer transfer) {
        String sql = "INSERT INTO transfers( transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES( ?, ?, ?, ?, ?) RETURNING transfer_id;";
        Long newId = jdbcTemplate.queryForObject(sql, Long.class,
                transfer.getTransferTypeId(), transfer.getTransferStatusId(),
                transfer.getAccountIdFrom(), transfer.getAccountIdTo(),transfer.getAmount());

        return newId;
    }

    @Override
    public Long addRequest(Transfer transfer) {
        String sql = "INSERT INTO transfers( transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES( ?, ?, ?, ?, ?) RETURNING transfer_id;";
        Long newId = jdbcTemplate.queryForObject(sql, Long.class,
                transfer.getTransferTypeId(), transfer.getTransferStatusId(),
                transfer.getAccountIdFrom(), transfer.getAccountIdTo(),transfer.getAmount());

        return newId;
    }


    @Override
    public TransferList[] getTransferHistoryList(Long userId){
        List<TransferList> transferHistories = new ArrayList<>();

        String sql = "SELECT t.transfer_id, " +
                "    CASE " +
                "     WHEN u1.user_id = ?  " +
                "        THEN 'To: ' || u2.username " +
                "     WHEN u2.user_id = ?   " +
                "        THEN 'From: ' || u1.username " +
                "     END AS transfer_name, " +
                "     t.amount  FROM  " +
                " transfers t " +
                " JOIN accounts a ON t.account_from = a.account_id " +
                " JOIN accounts b ON t.account_to = b.account_id " +
                " JOIN users u1   ON a.user_id = u1.user_id " +
                " JOIN users u2   ON b.user_id = u2.user_id " +
                " JOIN transfer_statuses ts ON t.transfer_status_id = ts.transfer_status_id " +
                "WHERE (a.user_id = ? OR b.user_id = ?) " ;
                //" AND    ts.transfer_status_desc = ? ; ";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, userId, userId,userId, userId);
        while(rowSet.next()){
            TransferList transferHistory = mapRowToTransferHistory(rowSet);
            transferHistories.add(transferHistory);
        }

        return transferHistories.toArray(new TransferList[0]);
    }


    @Override
    public Transfer getTransferDetails(Long transferId) {
        String sql = "SELECT t.transfer_id, u1.username AS user_from, u2.username AS user_to, tt.transfer_type_desc AS transfer_type, ts.transfer_status_desc AS transfer_status, amount " +
                " FROM transfers t " +
                " JOIN accounts a ON t.account_from = a.account_id " +
                " JOIN accounts b ON t.account_to = b.account_id " +
                " JOIN users u1   ON a.user_id = u1.user_id " +
                " JOIN users u2   ON b.user_id = u2.user_id " +
                " JOIN transfer_types tt ON tt.transfer_type_id = t.transfer_type_id " +
                " JOIN transfer_statuses ts ON ts.transfer_status_id = t.transfer_status_id " +
                "WHERE t.transfer_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, transferId);
        if(rowSet.next()){
            return mapRowToTransfer(rowSet);
        }
        return null;

    }


    @Override
    public TransferList[] pendingTransferList(Long userId) {
        List<TransferList> transferHistories = new ArrayList<>();

        String sql = "SELECT t.transfer_id, u2.username as transfer_name, t.amount   " +
                " FROM transfers t " +
                " JOIN accounts a ON t.account_from = a.account_id " +
                " JOIN accounts b ON t.account_to = b.account_id " +
                " JOIN users u1   ON a.user_id = u1.user_id " +
                " JOIN users u2   ON b.user_id = u2.user_id " +
                " JOIN transfer_types tt ON t.transfer_type_id = tt.transfer_type_id " +
                " JOIN transfer_statuses ts ON t.transfer_status_id = ts.transfer_status_id  " +
                "WHERE (u1.user_id = ?) " +
                " AND  tt.transfer_type_desc = ? " +
                " AND  ts.transfer_status_desc = ?  ;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, userId, REQUEST_TYPE , PENDING_STATUS);
        while(rowSet.next()){
            TransferList transferHistory = mapRowToTransferHistory(rowSet);
            transferHistories.add(transferHistory);
        }

        return transferHistories.toArray(new TransferList[0]);



    }

    @Override
    public Transfer getTransferByTransferId(Long transferId){
        Transfer transfer = null;
        String sql = "SELECT * FROM transfers WHERE transfer_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, transferId);

        if(rowSet.next()){
            transfer = mapRowToUpdateTransfers(rowSet);
        }
        return transfer;
    }

    @Override
    public void approveOrRejectTransfer(Long transferId, Long transferStatusId) {
        String sql = "UPDATE transfers SET transfer_status_id = ? WHERE transfer_id = ?";
        jdbcTemplate.update(sql,transferStatusId, transferId);
    }

    private TransferList mapRowToTransferHistory(SqlRowSet rowSet) {

        TransferList transferHistory = new TransferList();
        transferHistory.setTransferId(rowSet.getLong("transfer_id"));
        transferHistory.setToFromName(rowSet.getString("transfer_name"));
       transferHistory.setAmount(rowSet.getBigDecimal("amount"));

        return transferHistory;
    }

    private Transfer mapRowToTransfer(SqlRowSet rowSet){
        Transfer transfer = new Transfer();
        transfer.setTransferId(rowSet.getLong("transfer_id"));
        transfer.setUserFrom(rowSet.getString("user_from"));
        transfer.setUserTo(rowSet.getString("user_to"));
        transfer.setTransferType(rowSet.getString("transfer_type"));
        transfer.setTransferStatus(rowSet.getString("transfer_status"));
        transfer.setAmount(rowSet.getBigDecimal("amount"));

        return transfer;
    }


    private Transfer mapRowToUpdateTransfers(SqlRowSet rowSet){

        Transfer transfer = new Transfer();
        transfer.setTransferId(rowSet.getLong("transfer_id"));
        transfer.setTransferTypeId(rowSet.getLong("transfer_type_id"));
        transfer.setTransferStatusId(rowSet.getLong("transfer_status_id"));
        transfer.setAccountIdFrom(rowSet.getLong("account_from"));
        transfer.setAccountIdTo(rowSet.getLong("account_to"));
        transfer.setAmount(rowSet.getBigDecimal("amount"));

        return transfer;
    }

}
