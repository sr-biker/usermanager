package com.digitalservicing.usermanager.entity;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "DIGITAL_SERVICE_USER")
public class User {

    @Id
    private int userId;

    @Nonnull
    private String userName;
    @Nonnull
    private String userPassword;

}
