package com.digitalservicing.usermanager.service;

import com.digitalservicing.usermanager.entity.User;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.LoginException;
import java.net.URL;

public interface UserService {
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    User addUser(User user);

    @Transactional(readOnly = true)
    User login(String aUserName, String aPassword);

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    void addProfileToUser(Long aUserId, URL url);

    User getUser(Long userid);
}
