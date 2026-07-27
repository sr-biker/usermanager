package com.digitalservicing.usermanager.repository;

import com.digitalservicing.usermanager.entity.User;
import com.digitalservicing.usermanager.entity.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the repositories against a real Postgres container instead of an embedded
 * substitute -- schema.sql uses Postgres-specific DDL (a FK constraint, an identity
 * column) that an in-memory database wouldn't enforce the same way.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryContainerIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.defer-datasource-initialization", () -> "true");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    void userProfileForeignKeyIsPopulatedByRealSchema() throws Exception {
        UserProfile profile = new UserProfile();
        profile.setProfileUri(URI.create("http://google.com").toURL());
        UserProfile savedProfile = userProfileRepository.saveAndFlush(profile);

        User user = new User();
        user.setUserId(900001L);
        user.setUserName("PROFILE OWNER");
        user.setUserPassword("hashed");
        user.setUserProfile(savedProfile);

        User saved = userRepository.saveAndFlush(user);

        assertThat(userRepository.findById(saved.getUserId()))
                .get()
                .extracting(u -> u.getUserProfile().getProfileId())
                .isEqualTo(savedProfile.getProfileId());
    }

    /**
     * User.userId is mapped GenerationType.IDENTITY, which requires USER_ID to be a
     * real Postgres identity column -- schema.sql now declares it as one, so a save
     * with no ID set (the path addUser() actually takes) lets Postgres generate the
     * PK instead of rejecting the row with a NOT NULL violation.
     */
    @Test
    void savingAUserWithoutAnExplicitIdLetsPostgresGenerateThePrimaryKey() {
        User user = new User();
        user.setUserName("NO ID SET");
        user.setUserPassword("hashed");

        User saved = userRepository.saveAndFlush(user);

        assertThat(saved.getUserId()).isNotNull();
        assertThat(userRepository.findByUserName("NO ID SET")).isPresent();
    }
}
