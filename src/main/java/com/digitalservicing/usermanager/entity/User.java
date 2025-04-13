package com.digitalservicing.usermanager.entity;

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
    private String userPassword;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profileId")
    private UserProfile userProfile;

}
