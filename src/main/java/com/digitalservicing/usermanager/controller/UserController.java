package com.digitalservicing.usermanager.controller;

import com.digitalservicing.usermanager.dto.UserDto;
import com.digitalservicing.usermanager.entity.User;
import com.digitalservicing.usermanager.exception.UserNotFoundException;
import com.digitalservicing.usermanager.service.S3ServiceImpl;
import com.digitalservicing.usermanager.service.KafkaProducerServiceImpl;
import com.digitalservicing.usermanager.service.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URL;

/**
 * Rest controller for the application.
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
@Validated
@AllArgsConstructor
public class UserController {
    private UserServiceImpl userService;
    private S3ServiceImpl s3Service;
    private KafkaProducerServiceImpl kafkaProducerService;
    private ModelMapper modelMapper;


    @GetMapping("/version")
    public String version(){
        return "v1";
    }

    @PostMapping(value = "/users")
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@Valid @RequestBody User aUser){
        return this.userService.addUser(aUser);
    }

    @GetMapping("/users/login")
    @ResponseStatus(HttpStatus.OK)
    public void login(@RequestParam String userName, @RequestParam String password)  {
        log.info("Logging in with name {} ", userName);
        this.userService.login(userName, password);
    }

    @PutMapping("/users/profile")
    @ResponseStatus(HttpStatus.CREATED)
    public void addProfileToUser(@RequestParam("userid") Long userid, @RequestParam("profileUrl") URL profileUrl) throws UserNotFoundException{
        log.info("Adding profile to user with userid {} and profile {}", userid, profileUrl);
        this.userService.addProfileToUser(userid, profileUrl);
        kafkaProducerService.sendMessage("Userid " + userid + " profileurl " + profileUrl);
    }
    @GetMapping("/users/{userid}/profile")
    public UserDto getUser(@PathVariable("userid") Long userid) throws UserNotFoundException{
        log.info("Invoking get user with userid {}", userid);
        User user = this.userService.getUser(userid);
        return convertToDto(user);
    }

    @PostMapping(value = "/users/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public URL uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("Invoking upload of Image with fileName {}", file.getOriginalFilename());
        return s3Service.uploadImage(file);
    }

    @GetMapping("/users/image")
    public void getImage(@RequestParam String imageHash) {
        log.info("Invoking Get of Image with hash {}", imageHash);
        s3Service.getImage(imageHash);
    }

    @DeleteMapping("/users/image")
    public void deleteImage(@RequestParam String imageHash) {
        //TODO: Validate login
        log.info("Invoking Delete of Image with hash {}", imageHash);
        s3Service.deleteImage(imageHash);
    }

    @GetMapping("/users")
    public void exportCsv(){

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
