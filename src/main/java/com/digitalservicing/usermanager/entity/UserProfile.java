package com.digitalservicing.usermanager.entity;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;

@Entity
@Data
@Table(name = "DIGITAL_SERVICE_USER_PROFILE")
@NoArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;
    @Nonnull
    private URL profileUri;
}
