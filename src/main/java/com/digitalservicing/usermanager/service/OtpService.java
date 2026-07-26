package com.digitalservicing.usermanager.service;

import com.digitalservicing.usermanager.exception.UserLoginException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory OTP store -- fine for a single-replica demo, not for prod (lost on
 * restart, doesn't work across replicas). A real implementation would use Redis
 * or the DB.
 */
@Service
@Slf4j
@AllArgsConstructor
public class OtpService {

    private static final long OTP_TTL_SECONDS = 300;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Otp> otpsByUserName = new ConcurrentHashMap<>();

    private final MessageManagerClient messageManagerClient;

    public void sendOtp(String userName, String phoneNumber) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        otpsByUserName.put(userName, new Otp(code, Instant.now().plusSeconds(OTP_TTL_SECONDS)));
        // The prod Twilio account is still in trial mode, which rejects arbitrary
        // message bodies ("Invalid template name. Trial accounts can only use
        // predefined SMS templates.") -- sms_appointment_reminders is one of Twilio's
        // built-in trial template names. Swap this for the real "Your verification
        // code is {code}" body once the account is upgraded out of trial.
        messageManagerClient.sendSms(phoneNumber, "sms_appointment_reminders");
    }

    public void verifyOtp(String userName, String code) {
        Otp otp = otpsByUserName.get(userName);
        if (otp == null || otp.expiresAt.isBefore(Instant.now()) || !otp.code.equals(code)) {
            throw new UserLoginException();
        }
        otpsByUserName.remove(userName);
    }

    private record Otp(String code, Instant expiresAt) {
    }
}
