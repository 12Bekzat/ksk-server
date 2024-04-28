package com.example.ksk.dto;

import lombok.Data;

@Data
public class RegUserDto {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String role;

    public String getUser() {
        return username + " " + password + " " + fullName + " " + email + " " + role;
    }
}
