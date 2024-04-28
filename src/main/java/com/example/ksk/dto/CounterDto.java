package com.example.ksk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CounterDto {
    private Long id;
    private int meterReadings;
    private String removalDate;
    private RateDto rate;
}
