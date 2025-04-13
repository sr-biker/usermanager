package com.digitalservicing.usermanager.repository;

import com.digitalservicing.usermanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    @Query("SELECT u FROM User u WHERE u.userName = :userName and u.userPassword = :password")
    Optional<User> findUser(String userName, String password);

}
