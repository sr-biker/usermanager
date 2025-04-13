package com.digitalservicing.usermanager.service;

import com.digitalservicing.usermanager.dto.UserDto;
import com.digitalservicing.usermanager.entity.User;
import com.digitalservicing.usermanager.entity.UserProfile;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.Set;

public interface UserService {
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    User addUser(User user);

    @Transactional(readOnly = true)
    User login(String aUserName, String aPassword);

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    void addProfileToUser(Long aUserId, URL url);

    User getUser(Long userid);
}
