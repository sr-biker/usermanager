package com.digitalservicing.usermanager.entity;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.net.URI;

@Entity
@Data
@Table(name = "DIGITAL_SERVICE_USER_PROFILE")
public class UserProfile {

    @Id
    private Long profileId;

    @Nonnull
    private URI profileUri;
}
