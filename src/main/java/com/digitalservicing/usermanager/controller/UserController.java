package com.digitalservicing.usermanager.controller;

import com.digitalservicing.usermanager.entity.UserProfile;
import com.digitalservicing.usermanager.entity.User;
import com.digitalservicing.usermanager.exception.UserNotFoundException;
import com.digitalservicing.usermanager.service.ImgurServiceImpl;
import com.digitalservicing.usermanager.service.UserServiceImpl;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Set;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ImgurServiceImpl imgurService;

    @GetMapping("/version")
    public String version(){
        return "v1";
    }

    @PostMapping(value = "/user")
    public User addUser(User aUser){
        return this.userService.addUser(aUser);
    }

    @GetMapping("/user/login")
    public void login(User user) throws UserNotFoundException{
        this.userService.login(user);
    }

    @PutMapping("/user/{userid}/profile")
    public void addProfileToUser(@PathVariable Long userid, Set<UserProfile> profiles) throws UserNotFoundException{
        this.userService.addProfileToUser(userid, profiles);
    }

    @PostMapping("/user/image")
    public void uploadToImgur(@RequestParam("fileName") String fileName) throws UnirestException {
        imgurService.uploadImage(new File(fileName));
    }
}
