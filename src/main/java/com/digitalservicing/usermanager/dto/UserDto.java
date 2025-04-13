package com.digitalservicing.usermanager.dto;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    @Id
    private int userId;
    @Nonnull
    private String userName;
    private URL profileUri;

}
