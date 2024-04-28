package com.example.ksk.repository;

import com.example.ksk.entity.Rate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RateRepository extends JpaRepository<Rate, Long> {
    Optional<Rate> findByName(String name);
    Optional<Rate> findByNumber(int number);
}
