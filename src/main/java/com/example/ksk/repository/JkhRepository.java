package com.example.ksk.repository;

import com.example.ksk.entity.Jkh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JkhRepository extends JpaRepository<Jkh, Long> {
    Optional<Jkh> findByName(String name);
}
