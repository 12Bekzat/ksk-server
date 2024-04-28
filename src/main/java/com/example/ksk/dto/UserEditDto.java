package com.example.ksk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserEditDto {
    private String fullName;
    private String email;
    private boolean me;
    private String username;
    private String password;
    private String confirmPassword;
}
