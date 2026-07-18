package com.digitalservicing.usermanager.service;

import com.digitalservicing.usermanager.exception.UserLoginException;
import com.digitalservicing.usermanager.exception.UserNotFoundException;
import com.digitalservicing.usermanager.entity.UserProfile;
import com.digitalservicing.usermanager.entity.User;
import com.digitalservicing.usermanager.repository.UserProfileRepository;
import com.digitalservicing.usermanager.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl {

    private UserRepository userRepository;
    private UserProfileRepository userProfileRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public User addUser(User user){
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User login(String aUserName, String aPassword) {
        Optional<User>  optionalUser = userRepository.findUser(aUserName,
                aPassword);
        return optionalUser.orElseThrow(UserLoginException::new);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public void addProfileToUser(Long aUserId, URL url) {
        UserProfile userProfile = new UserProfile();
        userProfile.setProfileUri(url);
        userProfileRepository.save(userProfile);
        Optional<User> userOptional = userRepository.findById(aUserId);
        User user = userOptional.get();
        user.setUserId(aUserId);
        user.setUserProfile(userProfile);
        userRepository.save(user);
    }

    public User getUser(Long aUserId) {
        Optional<User> aUser = userRepository.findById(aUserId);
        return aUser.orElseThrow(UserNotFoundException::new);
    }
}
