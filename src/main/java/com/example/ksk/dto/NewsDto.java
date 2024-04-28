package com.example.ksk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewsDto {
    private String title;
    private String date;
    private String text;
}
