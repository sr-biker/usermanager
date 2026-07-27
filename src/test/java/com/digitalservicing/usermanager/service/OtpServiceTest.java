package com.digitalservicing.usermanager.service;

import com.digitalservicing.usermanager.exception.UserLoginException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private MessageManagerClient messageManagerClient;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private OtpService otpService;

    @Test
    void sendOtp_sendsTrialTemplateBody_toGivenPhoneNumber() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        otpService.sendOtp("JOHN DOE", "+17037550417");

        verify(messageManagerClient).sendSms("+17037550417", "sms_appointment_reminders");
    }

    @Test
    void sendOtp_storesTheCode_withA300SecondTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        otpService.sendOtp("JOHN DOE", "+17037550417");

        verify(valueOperations).set(eq("otp:JOHN DOE"), any(), eq(Duration.ofSeconds(300)));
    }

    @Test
    void verifyOtp_succeeds_whenCodeMatchesTheOneJustSent() {
        String code = stubStoredCode("JOHN DOE", "123456");

        assertThatCode(() -> otpService.verifyOtp("JOHN DOE", code)).doesNotThrowAnyException();
    }

    @Test
    void verifyOtp_consumesTheCode_soItCannotBeReused() {
        stubStoredCode("JOHN DOE", "123456");

        otpService.verifyOtp("JOHN DOE", "123456");

        verify(redisTemplate).delete("otp:JOHN DOE");
    }

    @Test
    void verifyOtp_throws_whenCodeIsWrong() {
        stubStoredCode("JOHN DOE", "123456");

        assertThatThrownBy(() -> otpService.verifyOtp("JOHN DOE", "000000"))
                .isInstanceOf(UserLoginException.class);
    }

    @Test
    void verifyOtp_throws_whenNoOtpWasEverSent() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:UNKNOWN USER")).thenReturn(null);

        assertThatThrownBy(() -> otpService.verifyOtp("UNKNOWN USER", "123456"))
                .isInstanceOf(UserLoginException.class);
    }

    /**
     * TTL expiry is enforced server-side by Redis -- a mock can't age, so an expired
     * code is indistinguishable here from a key that was never set/already evicted.
     */
    @Test
    void verifyOtp_throws_whenTheKeyHasExpiredOrWasNeverSet() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:JOHN DOE")).thenReturn(null);

        assertThatThrownBy(() -> otpService.verifyOtp("JOHN DOE", "123456"))
                .isInstanceOf(UserLoginException.class);
    }

    private String stubStoredCode(String userName, String code) {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:" + userName)).thenReturn(code);
        return code;
    }
}
