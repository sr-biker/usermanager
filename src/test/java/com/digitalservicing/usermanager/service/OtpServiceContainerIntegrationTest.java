package com.digitalservicing.usermanager.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Exercises OtpService against a real Redis instance instead of a mocked
 * StringRedisTemplate. OtpServiceTest already covers the branching logic with mocks,
 * but a mock can't confirm the TTL actually reaches Redis, that the value survives a
 * real RESP round-trip, or that a delete really removes the key server-side -- this
 * fills exactly the gap called out in OtpServiceTest's "a mock can't age" comment.
 */
@Testcontainers
class OtpServiceContainerIntegrationTest {

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    private LettuceConnectionFactory connectionFactory;
    private StringRedisTemplate redisTemplate;
    private MessageManagerClient messageManagerClient;
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        connectionFactory = new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();

        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();

        messageManagerClient = mock(MessageManagerClient.class);
        otpService = new OtpService(redisTemplate, messageManagerClient);
    }

    @AfterEach
    void tearDown() {
        connectionFactory.destroy();
    }

    @Test
    void sendOtpThenVerifyOtp_roundTripsThroughRealRedis() {
        otpService.sendOtp("JOHN DOE", "+17037550417");
        String code = redisTemplate.opsForValue().get("otp:JOHN DOE");

        assertThat(code).matches("\\d{6}");
        assertThatCode(() -> otpService.verifyOtp("JOHN DOE", code)).doesNotThrowAnyException();
        verify(messageManagerClient).sendSms("+17037550417", "sms_appointment_reminders");
    }

    @Test
    void sendOtp_setsARealTtlOnTheKeyInRedis() {
        otpService.sendOtp("JOHN DOE", "+17037550417");

        Long ttl = redisTemplate.getExpire("otp:JOHN DOE");

        assertThat(ttl).isNotNull();
        assertThat(ttl).isBetween(1L, Duration.ofSeconds(300).getSeconds());
    }

    @Test
    void verifyOtp_reallyDeletesTheKey_soItCannotBeReused() {
        otpService.sendOtp("JOHN DOE", "+17037550417");
        String code = redisTemplate.opsForValue().get("otp:JOHN DOE");
        otpService.verifyOtp("JOHN DOE", code);

        assertThat(redisTemplate.hasKey("otp:JOHN DOE")).isFalse();
        assertThatThrownBy(() -> otpService.verifyOtp("JOHN DOE", code))
                .isInstanceOf(com.digitalservicing.usermanager.exception.UserLoginException.class);
    }
}
