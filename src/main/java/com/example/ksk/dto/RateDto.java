package com.example.ksk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RateDto {
    private Long id;
    private int number;
    private String name;
    private float price;
}
