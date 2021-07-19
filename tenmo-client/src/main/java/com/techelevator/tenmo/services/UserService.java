package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.UserListItem;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

public class UserService {

    private String baseUrl;
    private RestTemplate restTemplate = new RestTemplate();

    public UserService(String url) {
        this.baseUrl = url;
    }

    public UserListItem[] listUsers(AuthenticatedUser currentUser) throws AuthenticationServiceException {
        UserListItem[] users = null;
        try {
            Long id = Long.valueOf(currentUser.getUser().getId());
            users = restTemplate
                    .exchange(baseUrl + "users/" + id, HttpMethod.GET, makeAuthEntity(currentUser.getToken()), UserListItem[].class).getBody();
        } catch (RestClientResponseException ex) {
            throw new AuthenticationServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
        }
        return users;
    }

    private HttpEntity makeAuthEntity(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity entity = new HttpEntity<>(headers);
        return entity;
    }

}


