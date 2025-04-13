package com.digitalservicing.usermanager.service;

import com.digitalservicing.usermanager.exception.UserNotFoundException;
import com.digitalservicing.usermanager.entity.UserProfile;
import com.digitalservicing.usermanager.entity.User;
import com.digitalservicing.usermanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    @Override
    public User addUser(User user){
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    @Override
    public User login(User aUser){
        Optional<User>  optionalUser = userRepository.findUser(aUser.getUserName(),
                aUser.getUserPassword());
        return optionalUser.orElseThrow(UserNotFoundException::new);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    @Override
    public User addProfileToUser(Long aUserId, Set<UserProfile> profiles) {
        Optional<User> aUser = userRepository.findById(aUserId);
        userRepository.save(aUser.get());
        return aUser.orElseThrow(UserNotFoundException::new);
    }
}
