package com.example.ksk.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Collection;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String fullName;
    private String password;
    private String email;
    private boolean isBanned;

    @OneToOne(cascade = CascadeType.ALL)
    private Image avatar;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private House house;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Service> services;

    @ManyToMany
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Collection<Role> roles;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private Jkh jkh;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    public void addService(Service service) {
        services.add(service);
    }
    public void addPayment(Payment payment) {
        payments.add(payment);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
