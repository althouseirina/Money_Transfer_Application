package com.techelevator.tenmo.services;

import com.techelevator.tenmo.Exceptiom.Error;
import com.techelevator.tenmo.Exceptiom.TransferServiceException;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferList;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class TransferService {

    private String baseUrl;
    private RestTemplate restTemplate = new RestTemplate();

    public TransferService(String url) {
        this.baseUrl = url;
    }


    public TransferList[] listTransactionHistory(AuthenticatedUser currentUser) throws TransferServiceException {

        TransferList[] transferHistories = null;
        Long id = Long.valueOf(currentUser.getUser().getId());
        String authToken = currentUser.getToken();

        try {
            transferHistories = restTemplate.exchange(baseUrl + "users/" + id + "/transferHistory", HttpMethod.GET,
                    makeAuthEntity(authToken), TransferList[].class).getBody();
        } catch (RestClientResponseException ex) {
            throw new TransferServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
        } catch (ResourceAccessException ex) {
            System.out.println(ex.getMessage());
        }
        return transferHistories;
    }


    public Transfer getTransferDetails(String authToken, Long transferId) throws TransferServiceException {

        Transfer transferDetail = null;

        try {
            transferDetail = restTemplate.exchange(baseUrl + "users/transferHistory/" + transferId , HttpMethod.GET,
                    makeAuthEntity(authToken), Transfer.class).getBody();
        } catch (RestClientResponseException ex) {
            if( ex.getRawStatusCode() == 404){
                Error error = ServiceUtil.parseError(ex.getResponseBodyAsString());
                throw new TransferServiceException(error.getMessage());
            }
        } catch (ResourceAccessException ex) {
            System.out.println(ex.getMessage());
        }
        return transferDetail;
    }



    public Transfer makeTransfer(AuthenticatedUser currentUser, Long toId, BigDecimal amount) throws TransferServiceException {

        Transfer transfer = createTransfer(currentUser, toId, amount);
        try{
            HttpEntity<Transfer> entity =  makePostAuthEntity(currentUser.getToken(),transfer);
            Long transferId =  restTemplate.postForObject(baseUrl + "transfer", entity, Long.class);
            transfer.setTransferId(transferId);

        } catch (RestClientResponseException ex){
            if(ex.getRawStatusCode() == 400 || ex.getRawStatusCode() == 404){
                Error error = ServiceUtil.parseError(ex.getResponseBodyAsString());
                throw new TransferServiceException(error.getMessage());
            }
        } catch (ResourceAccessException ex){
            System.out.println(ex.getMessage());
        }
        return transfer;
    }

    public Transfer makeRequest(AuthenticatedUser currentUser, Long toId, BigDecimal amount) throws TransferServiceException {

        Transfer transfer = createRequest(currentUser, toId, amount);

        try{
            HttpEntity<Transfer> entity =  makePostAuthEntity(currentUser.getToken(),transfer);
            Long transferId =  restTemplate.postForObject(baseUrl + "request", entity, Long.class);
            transfer.setTransferId(transferId);

        } catch (RestClientResponseException ex){
            if(ex.getRawStatusCode() == 400 || ex.getRawStatusCode() == 404) {
                Error error = ServiceUtil.parseError(ex.getResponseBodyAsString());
                throw new TransferServiceException(error.getMessage());
            }
        } catch (ResourceAccessException ex){
            System.out.println(ex.getMessage());
        }
        return transfer;

    }

    public Transfer approveOrReject(Long transferId, String choice, AuthenticatedUser user) throws TransferServiceException {
        Transfer transfer = createUpdate(transferId, choice);
        String token = user.getToken();
        Long id = Long.valueOf(user.getUser().getId());

        try{
            HttpEntity<Transfer> entity = makePostAuthEntity(token, transfer);
            restTemplate.put(baseUrl + "users/" + id + "/pendingTransactions/approveOrReject" , entity );
        } catch (RestClientResponseException ex){
            if(ex.getRawStatusCode() == 400 || ex.getRawStatusCode() == 404){
                Error error = ServiceUtil.parseError(ex.getResponseBodyAsString());
                throw new TransferServiceException(error.getMessage());
            }
        } catch (ResourceAccessException ex){
            System.out.println(ex.getMessage());
        }

    return transfer;
    }

    private Transfer createTransfer(AuthenticatedUser currentUser, Long toId, BigDecimal amount){

        Transfer transfer = new Transfer();
        transfer.setUserIdFrom(Long.valueOf(currentUser.getUser().getId()));
        transfer.setUserIdTo(toId);
        transfer.setAmount(amount);

        return transfer;
    }

    private Transfer createRequest(AuthenticatedUser currentUser, Long fromId, BigDecimal amount){
        Transfer transfer = new Transfer();
        transfer.setUserIdFrom(fromId);
        transfer.setUserIdTo(Long.valueOf(currentUser.getUser().getId()));
        transfer.setAmount(amount);

        return transfer;
    }

    private Transfer createUpdate(  Long transferId, String choice){

        Transfer transfer = new Transfer();
        transfer.setTransferId(transferId);
        transfer.setTransferStatus(choice);

        return transfer;
    }

   public  TransferList[] pendingTransactionList(AuthenticatedUser currentUser) throws TransferServiceException {

       TransferList[] transferList = null;
       Long id = Long.valueOf(currentUser.getUser().getId());
       String authToken = currentUser.getToken();

       try{
           transferList = restTemplate.exchange(baseUrl + "users/" + id + "/pendingTransactions", HttpMethod.GET,
                   makeAuthEntity(authToken), TransferList[].class).getBody();
       } catch (RestClientResponseException ex){
           throw new TransferServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
       } catch (ResourceAccessException ex){
           System.out.println(ex.getMessage());
       }

       return transferList;
   }

    private HttpEntity<Transfer> makePostAuthEntity(String authToken, Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        HttpEntity<Transfer> entity = new HttpEntity<>(transfer,headers);
        return entity;
    }

    private HttpEntity makeAuthEntity(String authToken ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity entity = new HttpEntity<>(headers);
        return entity;
    }

}
