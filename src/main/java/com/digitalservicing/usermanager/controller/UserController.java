package com.digitalservicing.usermanager.controller;

import com.digitalservicing.usermanager.dto.UserDto;
import com.digitalservicing.usermanager.entity.User;
import com.digitalservicing.usermanager.exception.UserNotFoundException;
import com.digitalservicing.usermanager.service.ImgurService;
import com.digitalservicing.usermanager.service.KafkaProducerService;
import com.digitalservicing.usermanager.service.UserService;
import com.mashape.unirest.http.exceptions.UnirestException;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Rest controller for the application.
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
@Validated
@Builder
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private ImgurService imgurService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/version")
    public String version(){
        return "v1";
    }

    @PostMapping(value = "/user")
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@Valid User aUser){
        return this.userService.addUser(aUser);
    }

    @GetMapping("/user/login")
    @ResponseStatus(HttpStatus.OK)
    public void login(@RequestParam String userName, @RequestParam String password)  {
        log.info("Logging in with name {} ", userName);
        this.userService.login(userName, password);
    }

    @PutMapping("/user/profile")
    @ResponseStatus(HttpStatus.CREATED)
    public void addProfileToUser(@RequestParam Long userid, @RequestParam URL profileUrl) throws UserNotFoundException{
        log.info("Adding profile to user with userid {} and profile {}", userid, profileUrl);
        this.userService.addProfileToUser(userid, profileUrl);
        kafkaProducerService.sendMessage("Userid " + userid + " profileurl " + profileUrl);
    }
    @GetMapping("/user/{userid}/profile")
    public UserDto getUser(@PathVariable Long userid) throws UserNotFoundException{
        log.info("Invoking get user with userid {}", userid);
        User user = this.userService.getUser(userid);
        return convertToDto(user);
    }

    @PostMapping("/user/image")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public URL uploadToImgur(@RequestParam("fileName") String fileName) throws UnirestException, IOException {
        log.info("Invoking upload of Image with fileName {}", fileName);
        return imgurService.uploadImage(new File(fileName));
    }

    @GetMapping("/user/image")
    public void getFromImgur(@RequestParam String imageHash) throws UnirestException {
        log.info("Invoking Get of Image with hash {}", imageHash);
        imgurService.getImage(imageHash);
    }

    @DeleteMapping("/user/image")
    public void deleteFromImgur(@RequestParam String imageHash) throws UnirestException {
        //TODO: Validate login
        log.info("Invoking Delete of Image with hash {}", imageHash);
        imgurService.deleteImage(imageHash);
    }

    private UserDto convertToDto(User user) {
        UserDto userDto = modelMapper.map(user, UserDto.class);
        if (user.getUserProfile() != null) {
            userDto.setProfileUri(user.getUserProfile().getProfileUri());
        }
        return userDto;
    }

    private User convertToEntity(UserDto userDto)  {
        User user = modelMapper.map(userDto, User.class);
        return user;
    }
}
