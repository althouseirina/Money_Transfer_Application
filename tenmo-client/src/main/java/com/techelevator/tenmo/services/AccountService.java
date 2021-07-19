package com.techelevator.tenmo.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {

    private String baseUrl;
    private RestTemplate restTemplate = new RestTemplate();

    public AccountService(String url) {

        this.baseUrl = url;

    }

    public BigDecimal getBalance(Long id , String authToken) throws AuthenticationServiceException {
        BigDecimal balance = null;
        try {
            balance = restTemplate.exchange(baseUrl + "users/" + id + "/balance", HttpMethod.GET, makeAuthEntity(authToken), BigDecimal.class).getBody();
        } catch (RestClientResponseException ex) {
            throw new AuthenticationServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
        } catch (ResourceAccessException ex){
            System.out.println(ex.getMessage());
        }
        return balance;
    }

    private HttpEntity makeAuthEntity(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity entity = new HttpEntity<>(headers);
        return entity;
    }


}
