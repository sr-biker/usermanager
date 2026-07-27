package com.digitalservicing.usermanager.service;

import com.digitalservicing.usermanager.exception.UserLoginException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * OTPs live in Redis (not this instance's heap) so they survive a pod restart and are
 * visible to whichever replica handles the verify request, not just the one that sent it.
 */
@Service
@Slf4j
@AllArgsConstructor
public class OtpService {

    private static final Duration OTP_TTL = Duration.ofSeconds(300);
    private static final String KEY_PREFIX = "otp:";

    private final SecureRandom random = new SecureRandom();
    private final StringRedisTemplate redisTemplate;

    private final MessageManagerClient messageManagerClient;

    public void sendOtp(String userName, String phoneNumber) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        redisTemplate.opsForValue().set(key(userName), code, OTP_TTL);
        // The prod Twilio account is still in trial mode, which rejects arbitrary
        // message bodies ("Invalid template name. Trial accounts can only use
        // predefined SMS templates.") -- sms_appointment_reminders is one of Twilio's
        // built-in trial template names. Swap this for the real "Your verification
        // code is {code}" body once the account is upgraded out of trial.
        messageManagerClient.sendSms(phoneNumber, "sms_appointment_reminders");
    }

    public void verifyOtp(String userName, String code) {
        String storedCode = redisTemplate.opsForValue().get(key(userName));
        if (storedCode == null || !storedCode.equals(code)) {
            throw new UserLoginException();
        }
        redisTemplate.delete(key(userName));
    }

    private String key(String userName) {
        return KEY_PREFIX + userName;
    }
}
