package com.example.ksk.dto;

import com.example.ksk.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class PaymentDto {
    private Long id;
    private List<CounterDto> counters;
    private float price;
    private String deadline;
    private int status;
    private UserDto user;
    private JkhDto jkh;

    public PaymentDto(Long id, float price, String deadline, int status) {
        this.id = id;
        this.price = price;
        this.deadline = deadline;
        this.status = status;
    }
}
