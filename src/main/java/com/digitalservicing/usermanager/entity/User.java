package com.digitalservicing.usermanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "DIGITAL_SERVICE_USER")
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @Nonnull
    private String userName;
    @Nonnull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String userPassword;

    private String phoneNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profileId")
    private UserProfile userProfile;

}
