package com.example.ksk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JkhDto {
    private Long id;
    private String legalAddress;
    private String inn;
    private String kpp;
    private String name;
    private String bankAccount;
    private String phoneNumber;

    public JkhDto(String legalAddress, String inn, String kpp, String name, String bankAccount, String phoneNumber) {
        this.legalAddress = legalAddress;
        this.inn = inn;
        this.kpp = kpp;
        this.name = name;
        this.bankAccount = bankAccount;
        this.phoneNumber = phoneNumber;
    }
}
