package com.digitalservicing.usermanager.controller;

import com.digitalservicing.usermanager.dto.UserDto;
import com.digitalservicing.usermanager.entity.User;
import com.digitalservicing.usermanager.exception.UserNotFoundException;
import com.digitalservicing.usermanager.service.S3ServiceImpl;
import com.digitalservicing.usermanager.service.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller-layer tests using MockMvc with the service dependencies mocked out.
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceImpl userService;

    @MockBean
    private S3ServiceImpl s3Service;

    @MockBean
    private ModelMapper modelMapper;

    @Test
    void version_returnsV1() throws Exception {
        mockMvc.perform(get("/api/v1/version"))
                .andExpect(status().isOk())
                .andExpect(content().string("v1"));
    }

    @Test
    void addUser_delegatesToServiceAndReturnsCreated() throws Exception {
        User requestUser = new User();
        requestUser.setUserName("JANE DOE");
        requestUser.setUserPassword("secret");

        User savedUser = new User();
        savedUser.setUserId(1L);
        savedUser.setUserName("JANE DOE");
        savedUser.setUserPassword("secret");

        when(userService.addUser(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/v1/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.userName").value("JANE DOE"));
        verify(userService, times(1)).addUser(any(User.class));
    }

    @Test
    void login_delegatesToServiceWithGivenCredentials() throws Exception {
        mockMvc.perform(get("/api/v1/users/login")
                        .param("userName", "JOHN DOE")
                        .param("password", "abcd"))
                .andExpect(status().isOk());

        verify(userService, times(1)).login("JOHN DOE", "abcd");
    }

    @Test
    void verifyLoginOtp_delegatesToServiceWithGivenCode() throws Exception {
        mockMvc.perform(get("/api/v1/users/login/verify")
                        .param("userName", "JOHN DOE")
                        .param("code", "123456"))
                .andExpect(status().isOk());

        verify(userService, times(1)).verifyLoginOtp("JOHN DOE", "123456");
    }

    @Test
    void addProfileToUser_delegatesToServiceWithParsedParams() throws Exception {
        mockMvc.perform(put("/api/v1/users/profile")
                        .param("userid", "299999")
                        .param("profileUrl", "http://google.com"))
                .andExpect(status().isCreated());

        verify(userService, times(1)).addProfileToUser(eq(299999L), eq(URI.create("http://google.com").toURL()));
    }

    @Test
    void getUser_returnsMappedDto_whenUserExists() throws Exception {
        User user = new User();
        user.setUserId(199999L);
        user.setUserName("JOHN DOE");

        UserDto dto = new UserDto();
        dto.setUserId(199999);
        dto.setUserName("JOHN DOE");

        when(userService.getUser(199999L)).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/199999/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(199999))
                .andExpect(jsonPath("$.userName").value("JOHN DOE"));
    }

    @Test
    void getUser_propagatesNotFound_whenServiceThrows() throws Exception {
        when(userService.getUser(404L)).thenThrow(new UserNotFoundException());

        mockMvc.perform(get("/api/v1/users/404/profile"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteImage_delegatesToS3Service() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/users/image")
                        .param("imageHash", "abc123"))
                .andExpect(status().isOk());

        verify(s3Service, times(1)).deleteImage("abc123");
    }
}
