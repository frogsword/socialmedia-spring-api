package com.app.socialmedia.dtos.user;

import lombok.Data;

@Data
public class RegisterDto {
    private String email;

    private String password;

    private String confirmPassword;

    private String name;
}
