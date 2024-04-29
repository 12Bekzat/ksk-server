package com.example.ksk.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "houses")
public class House {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String address;
    private float square;
    private int countOfPeople;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private User owner;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private Jkh jkh;

    @Override
    public String toString() {
        return "House{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", square=" + square +
                ", countOfPeople=" + countOfPeople +
                '}';
    }
}
