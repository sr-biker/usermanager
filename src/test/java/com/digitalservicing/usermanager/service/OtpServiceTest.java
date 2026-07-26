package com.digitalservicing.usermanager.service;

import com.digitalservicing.usermanager.exception.UserLoginException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private MessageManagerClient messageManagerClient;

    @InjectMocks
    private OtpService otpService;

    @Test
    void sendOtp_sendsTrialTemplateBody_toGivenPhoneNumber() {
        otpService.sendOtp("JOHN DOE", "+17037550417");

        verify(messageManagerClient).sendSms("+17037550417", "sms_appointment_reminders");
    }

    @Test
    void verifyOtp_succeeds_whenCodeMatchesTheOneJustSent() {
        otpService.sendOtp("JOHN DOE", "+17037550417");
        String code = codeSentTo("JOHN DOE");

        assertThatCode(() -> otpService.verifyOtp("JOHN DOE", code)).doesNotThrowAnyException();
    }

    @Test
    void verifyOtp_consumesTheCode_soItCannotBeReused() {
        otpService.sendOtp("JOHN DOE", "+17037550417");
        String code = codeSentTo("JOHN DOE");
        otpService.verifyOtp("JOHN DOE", code);

        assertThatThrownBy(() -> otpService.verifyOtp("JOHN DOE", code))
                .isInstanceOf(UserLoginException.class);
    }

    @Test
    void verifyOtp_throws_whenCodeIsWrong() {
        otpService.sendOtp("JOHN DOE", "+17037550417");

        assertThatThrownBy(() -> otpService.verifyOtp("JOHN DOE", "000000"))
                .isInstanceOf(UserLoginException.class);
    }

    @Test
    void verifyOtp_throws_whenNoOtpWasEverSent() {
        assertThatThrownBy(() -> otpService.verifyOtp("UNKNOWN USER", "123456"))
                .isInstanceOf(UserLoginException.class);
    }

    @Test
    void verifyOtp_throws_whenCodeHasExpired() throws Exception {
        otpService.sendOtp("JOHN DOE", "+17037550417");
        String code = codeSentTo("JOHN DOE");
        expire("JOHN DOE");

        assertThatThrownBy(() -> otpService.verifyOtp("JOHN DOE", code))
                .isInstanceOf(UserLoginException.class);
    }

    @SuppressWarnings("unchecked")
    private String codeSentTo(String userName) {
        Map<String, Object> otpsByUserName = (Map<String, Object>) ReflectionTestUtils.getField(otpService, "otpsByUserName");
        Object otp = otpsByUserName.get(userName);
        return (String) ReflectionTestUtils.getField(otp, "code");
    }

    @SuppressWarnings("unchecked")
    private void expire(String userName) throws Exception {
        // Otp is a record -- its components are truly final (even reflection can't set
        // them) -- so replace the whole map entry with one whose expiresAt is in the past.
        Map<String, Object> otpsByUserName = (Map<String, Object>) ReflectionTestUtils.getField(otpService, "otpsByUserName");
        Object otp = otpsByUserName.get(userName);
        Class<?> otpClass = otp.getClass();
        var constructor = otpClass.getDeclaredConstructor(String.class, Instant.class);
        constructor.setAccessible(true);
        Object expiredOtp = constructor.newInstance(ReflectionTestUtils.getField(otp, "code"), Instant.now().minusSeconds(1));
        otpsByUserName.put(userName, expiredOtp);
    }
}
