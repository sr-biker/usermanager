package com.digitalservicing.usermanager.service;

import com.digitalservicing.usermanager.entity.User;
import com.digitalservicing.usermanager.entity.UserProfile;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

public interface UserService {
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    User addUser(User user);

    @Transactional(readOnly = true)
    User login(User aUser);

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    User addProfileToUser(Long aUserId, Set<UserProfile> profiles);
}
