package com.techelevator.tenmo.service;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserListItem;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class UserService {

    private UserDao userDao;

    public UserService(UserDao userDao){
        this.userDao = userDao;
    }

    public UserListItem[] getUserList(Long id) {

        return userDao.UserList(id).toArray(new UserListItem[0]);
    }
}