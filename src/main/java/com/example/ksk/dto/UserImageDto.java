package com.example.ksk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserImageDto {
    private Long id;
    private String username;
    private String fullName;
    private Long avatarId;
}
