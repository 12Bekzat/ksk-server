package com.example.ksk.dto;

import com.example.ksk.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class JkhResponseDto {
    private Long id;
    private String legalAddress;
    private String inn;
    private String kpp;
    private String name;
    private String bankAccount;
    private String phoneNumber;

    public JkhResponseDto(Long id, String legalAddress, String inn, String kpp, String name, String bankAccount, String phoneNumber) {
        this.id = id;
        this.inn = inn;
        this.kpp = kpp;
        this.name = name;
        this.bankAccount = bankAccount;
        this.phoneNumber = phoneNumber;
        this.legalAddress = legalAddress;
    }

    private List<UserDto> employee;
    private List<HouseDto> clients;
}
