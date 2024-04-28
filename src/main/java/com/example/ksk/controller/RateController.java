package com.example.ksk.controller;

import com.example.ksk.dto.RateDto;
import com.example.ksk.dto.ResponseDto;
import com.example.ksk.entity.Rate;
import com.example.ksk.error.AppError;
import com.example.ksk.repository.RateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class RateController {
    private final RateRepository rateRepository;

    @GetMapping("/rates")
    public ResponseEntity<?> getRates() {
        List<Rate> all = rateRepository.findAll();

        return ResponseEntity.ok(all);
    }

    @GetMapping("/rates/{id}")
    public ResponseEntity<?> getRates(@PathVariable(name = "id") Long id) {
        Optional<Rate> rateOptional = rateRepository.findById(id);

        if (rateOptional.isPresent()) {
            return ResponseEntity.ok(rateOptional.get());
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Rate is not exist!"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/rates/create")
    public ResponseEntity<?> createRate(@RequestBody RateDto rateDto) {
        Rate rate = new Rate();
        rate.setName(rateDto.getName());
        rate.setNumber(rateDto.getNumber());
        rate.setPrice(rateDto.getPrice());

        rateRepository.save(rate);

        return ResponseEntity.ok(new ResponseDto(200, "Rate created successfully!"));
    }

    @PostMapping("/rates/edit/{id}")
    public ResponseEntity<?> editRate(@PathVariable(name = "id") Long id, @RequestBody RateDto rateDto) {
        Optional<Rate> rateOptional = rateRepository.findById(id);

        if(rateOptional.isPresent()) {
            Rate rate = rateOptional.get();
            rate.setName(rateDto.getName());
            rate.setNumber(rateDto.getNumber());
            rate.setPrice(rateDto.getPrice());

            rateRepository.save(rate);

            return ResponseEntity.ok(new ResponseDto(200, "Rate changed successfully!"));
        }
        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Rate is not exist!"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/rates/remove/{id}")
    public ResponseEntity<?> removeRate(@PathVariable(name = "id") Long id) {
        Optional<Rate> rateOptional = rateRepository.findById(id);

        if(rateOptional.isPresent()) {
            rateRepository.deleteById(id);

            return ResponseEntity.ok(new ResponseDto(200, "Rate deleted successfully!"));
        }
        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Rate is not exist!"), HttpStatus.BAD_REQUEST);
    }
}
