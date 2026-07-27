package com.digitalservicing.usermanager.service;

import com.digitalservicing.types.ProfileCreatedEvent;
import com.digitalservicing.usermanager.entity.User;
import com.digitalservicing.usermanager.entity.UserProfile;
import com.digitalservicing.usermanager.exception.UserLoginException;
import com.digitalservicing.usermanager.exception.UserNotFoundException;
import com.digitalservicing.usermanager.repository.UserProfileRepository;
import com.digitalservicing.usermanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure mock-based unit tests for {@link UserServiceImpl}, isolating it from Spring context and the database.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private KafkaProducerServiceImpl kafkaProducerService;

    @Mock
    private OtpService otpService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, userProfileRepository, kafkaProducerService, otpService, passwordEncoder);
    }

    @Test
    void addUser_savesAndReturnsUser_withThePasswordHashed() {
        User user = new User();
        user.setUserName("JOHN DOE");
        user.setUserPassword("abcd");
        when(passwordEncoder.encode("abcd")).thenReturn("hashed-abcd");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.addUser(user);

        assertThat(result).isSameAs(user);
        ArgumentCaptor<User> savedCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().getUserPassword()).isEqualTo("hashed-abcd");
    }

    @Test
    void login_returnsUser_whenCredentialsMatch() {
        User user = new User();
        user.setUserName("JOHN DOE");
        user.setPhoneNumber("+17037550417");
        user.setUserPassword("hashed-abcd");
        when(userRepository.findByUserName("JOHN DOE")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("abcd", "hashed-abcd")).thenReturn(true);

        User result = userService.login("JOHN DOE", "abcd");

        assertThat(result).isSameAs(user);
    }

    @Test
    void login_sendsOtp_toTheUsersPhoneNumber_afterCredentialsMatch() {
        User user = new User();
        user.setUserName("JOHN DOE");
        user.setPhoneNumber("+17037550417");
        user.setUserPassword("hashed-abcd");
        when(userRepository.findByUserName("JOHN DOE")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("abcd", "hashed-abcd")).thenReturn(true);

        userService.login("JOHN DOE", "abcd");

        verify(otpService).sendOtp("JOHN DOE", "+17037550417");
    }

    @Test
    void login_throwsUserLoginException_whenUserNotFound() {
        when(userRepository.findByUserName("JOHN DOE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login("JOHN DOE", "wrong"))
                .isInstanceOf(UserLoginException.class);

        verify(otpService, never()).sendOtp(any(), any());
    }

    @Test
    void login_throwsUserLoginException_whenPasswordDoesNotMatch() {
        User user = new User();
        user.setUserName("JOHN DOE");
        user.setUserPassword("hashed-abcd");
        when(userRepository.findByUserName("JOHN DOE")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed-abcd")).thenReturn(false);

        assertThatThrownBy(() -> userService.login("JOHN DOE", "wrong"))
                .isInstanceOf(UserLoginException.class);

        verify(otpService, never()).sendOtp(any(), any());
    }

    @Test
    void verifyLoginOtp_delegatesToOtpService() {
        userService.verifyLoginOtp("JOHN DOE", "123456");

        verify(otpService).verifyOtp("JOHN DOE", "123456");
    }

    @Test
    void getUser_returnsUser_whenFound() {
        User user = new User();
        user.setUserId(199999L);
        when(userRepository.findById(199999L)).thenReturn(Optional.of(user));

        User result = userService.getUser(199999L);

        assertThat(result).isSameAs(user);
    }

    @Test
    void getUser_throwsUserNotFoundException_whenMissing() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(404L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void addProfileToUser_savesProfileAndUserAndPublishesEvent() throws MalformedURLException {
        Long userId = 299999L;
        URL profileUrl = URI.create("http://google.com").toURL();
        User existingUser = new User();
        existingUser.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.addProfileToUser(userId, profileUrl);

        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getProfileUri()).isEqualTo(profileUrl);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUserId()).isEqualTo(userId);
        assertThat(userCaptor.getValue().getUserProfile().getProfileUri()).isEqualTo(profileUrl);

        ArgumentCaptor<ProfileCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ProfileCreatedEvent.class);
        verify(kafkaProducerService).sendEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().userId()).isEqualTo(userId);
        assertThat(eventCaptor.getValue().profileUrl()).isEqualTo(profileUrl.toString());
    }

    @Test
    void addProfileToUser_throwsNoSuchElementException_whenUserMissing() throws MalformedURLException {
        Long userId = 404L;
        URL profileUrl = URI.create("http://google.com").toURL();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addProfileToUser(userId, profileUrl))
                .isInstanceOf(NoSuchElementException.class);

        verify(kafkaProducerService, never()).sendEvent(any());
    }
}
