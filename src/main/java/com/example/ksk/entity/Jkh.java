package com.example.ksk.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "jkhs")
public class Jkh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String legalAddress;
    private String inn;
    private String kpp;
    private String name;
    private String bankAccount;
    private String phoneNumber;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> employee;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<House> clients;

    public void addEmployee(User user) {
        employee.add(user);
    }

    public void removeEmployee(User user) {
        employee.remove(user);
    }

    public void addHouse(House house) {
        clients.add(house);
    }

    public void removeHouse(House house) {
        clients.remove(house);
    }
}
