package com.example.ksk.controller;

import com.example.ksk.dto.*;
import com.example.ksk.entity.House;
import com.example.ksk.entity.Jkh;
import com.example.ksk.entity.User;
import com.example.ksk.error.AppError;
import com.example.ksk.repository.HouseRepository;
import com.example.ksk.repository.JkhRepository;
import com.example.ksk.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
public class JkhController {
    private final JkhRepository jkhRepository;
    private final UserService userService;
    private final HouseRepository houseRepository;

    @GetMapping("/jkh/all")
    public ResponseEntity<?> getAll() {
        List<Jkh> all = jkhRepository.findAll();
        List<JkhResponseDto> allResponse = new ArrayList<>();

        for (Jkh item : all) {
            JkhResponseDto jkhResponseDto = new JkhResponseDto(item.getId(), item.getLegalAddress(), item.getInn(), item.getKpp(), item.getName(), item.getBankAccount(), item.getPhoneNumber());
            List<User> employee = item.getEmployee();
            List<House> clients = item.getClients();

            List<UserDto> userDtoStream = employee.stream().map(emp -> {
                return new UserDto(emp.getId(), emp.getUsername(), emp.getEmail(), emp.getFullName(), emp.isBanned(), emp.getHouse() != null);
            }).toList();

            List<HouseDto> houseDtoStream = clients.stream().map(client -> {
                User owner = client.getOwner();
                return new HouseDto(client.getId(), client.getAddress(), client.getSquare(), client.getCountOfPeople(), client.getJkh().getId(),
                        new UserDto(owner.getId(), owner.getUsername(), owner.getEmail(), owner.getFullName(), owner.isBanned(), owner.getHouse() != null));
            }).toList();

            jkhResponseDto.setEmployee(userDtoStream);
            jkhResponseDto.setClients(houseDtoStream);

            allResponse.add(jkhResponseDto);
        }

        return ResponseEntity.ok(allResponse);
    }

    @GetMapping("/jkh/{id}")
    public ResponseEntity<?> getJkhById(@PathVariable(name = "id") Long id) {
        Optional<Jkh> byId = jkhRepository.findById(id);

        if (byId.isPresent()) {
            Jkh jkh = byId.get();
            JkhResponseDto jkhResponseDto = new JkhResponseDto(jkh.getId(), jkh.getLegalAddress(), jkh.getInn(), jkh.getKpp(), jkh.getName(), jkh.getBankAccount(), jkh.getPhoneNumber());

            List<UserDto> userDtoStream = jkh.getEmployee().stream().map(emp -> {
                return new UserDto(emp.getId(), emp.getUsername(), emp.getEmail(), emp.getFullName(), emp.isBanned(), emp.getHouse() != null);
            }).toList();

            List<HouseDto> houseDtoStream = jkh.getClients().stream().map(client -> {
                User owner = client.getOwner();
                return new HouseDto(client.getId(), client.getAddress(), client.getSquare(), client.getCountOfPeople(), client.getJkh().getId(),
                        new UserDto(owner.getId(), owner.getUsername(), owner.getEmail(), owner.getFullName(), owner.isBanned(), owner.getHouse() != null));
            }).toList();

            jkhResponseDto.setEmployee(userDtoStream);
            jkhResponseDto.setClients(houseDtoStream);

            return ResponseEntity.ok(jkhResponseDto);
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Jkh not found!"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/jkh/create")
    public ResponseEntity<?> createJkh(@RequestBody JkhDto jkhDto) {
        System.out.println(jkhDto.getName() + " " + jkhDto.getPhoneNumber());
        Jkh jkh = new Jkh();
        jkh.setLegalAddress(jkhDto.getLegalAddress());
        jkh.setKpp(jkhDto.getKpp());
        jkh.setInn(jkhDto.getInn());
        jkh.setName(jkhDto.getName());
        jkh.setBankAccount(jkhDto.getBankAccount());
        jkh.setPhoneNumber(jkhDto.getPhoneNumber());

        jkhRepository.save(jkh);

        return ResponseEntity.ok(new ResponseDto(200, "Jkh created successfully!"));
    }

    @PostMapping("/jkh/{id}/add/employee/{userId}")
    public ResponseEntity<?> addEmployee(@PathVariable(name = "id") Long id, @PathVariable(name = "userId") Long userId) {
        Optional<Jkh> jkhOptional = jkhRepository.findById(id);
        Optional<User> userOptional = userService.findById(userId);
        if (jkhOptional.isPresent() && userOptional.isPresent()) {
            Jkh jkh = jkhOptional.get();
            User user = userOptional.get();

            if (!user.getRoles().stream().filter((item) -> {
                return item.getName().equals("ROLE_KSK");
            }).toList().isEmpty()) {
                jkh.addEmployee(user);
                jkhRepository.save(jkh);
                user.setJkh(jkh);
                userService.saveUser(user);

                return ResponseEntity.ok(new ResponseDto(200, "Employee added succesfully!"));
            }
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Jkh or User not found!"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/jkh/{id}/remove/employee/{userId}")
    public ResponseEntity<?> removeEmployee(@PathVariable(name = "id") Long id, @PathVariable(name = "userId") Long userId) {
        Optional<Jkh> jkhOptional = jkhRepository.findById(id);
        Optional<User> userOptional = userService.findById(userId);
        if (jkhOptional.isPresent() && userOptional.isPresent()) {
            Jkh jkh = jkhOptional.get();
            User user = userOptional.get();

            jkh.removeEmployee(user);
            jkhRepository.save(jkh);
            return ResponseEntity.ok(new ResponseDto(200, "Employee removed succesfully!"));
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Jkh or User not found!"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/users/{id}/add/house")
    public ResponseEntity<?> addHouse(@PathVariable(name = "id") Long id, @RequestBody HouseDto houseDto) {
        System.out.println(houseDto.getAddress() + " " + houseDto.getJkhId());
        Optional<User> userOptional = userService.findById(id);
        Optional<Jkh> jkhOptional = jkhRepository.findById(houseDto.getJkhId());

        if (userOptional.isPresent() && jkhOptional.isPresent()) {
            House house = new House();
            house.setAddress(houseDto.getAddress());
            house.setSquare(houseDto.getSquare());
            house.setCountOfPeople(houseDto.getCountOfPeople());
            house.setJkh(jkhOptional.get());
            house.setOwner(userOptional.get());
            houseRepository.save(house);

            User user = userOptional.get();
            Jkh jkh = jkhOptional.get();
            user.setHouse(house);
            userService.saveUser(user);

            jkh.addHouse(house);
            jkhRepository.save(jkh);

            return ResponseEntity.ok(new ResponseDto(200, "House added successfully!"));
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "User not found!"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/house/edit")
    public ResponseEntity<?> editHouse(@RequestBody HouseDto houseDto) {
        Optional<House> houseOptional = houseRepository.findById(houseDto.getId());

        if (houseOptional.isPresent()) {
            House house = houseOptional.get();
            house.setAddress(houseDto.getAddress());
            house.setSquare(houseDto.getSquare());
            house.setCountOfPeople(houseDto.getCountOfPeople());
            houseRepository.save(house);

            return ResponseEntity.ok(new ResponseDto(200, "House changed successfully!"));
        }
        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "House not found!"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/house/remove")
    public ResponseEntity<?> removeHouse(@RequestBody HouseDto houseDto) {
        Optional<House> houseOptional = houseRepository.findById(houseDto.getId());

        if (houseOptional.isPresent()) {
            Optional<User> userOptional = userService.findById(houseDto.getOwner().getId());
            Optional<Jkh> jkhOptional = jkhRepository.findById(houseDto.getJkhId());

            if (jkhOptional.isPresent() && userOptional.isPresent()) {
                User user = userOptional.get();
                Jkh jkh = jkhOptional.get();

                user.setHouse(null);
                jkh.removeHouse(houseOptional.get());
                userService.saveUser(user);
                jkhRepository.save(jkh);

                houseRepository.deleteById(houseOptional.get().getId());

                return ResponseEntity.ok(new ResponseDto(200, "House deleted successfully!"));
            }
        }
        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "House not found!"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/jkh/remove/client")
    public ResponseEntity<?> removeClient(@RequestBody HouseDto houseDto) {
        Optional<House> houseOptional = houseRepository.findById(houseDto.getId());

        if (houseOptional.isPresent()) {
            Optional<Jkh> jkhOptional = jkhRepository.findById(houseDto.getJkhId());

            if (jkhOptional.isPresent()) {
                Jkh jkh = jkhOptional.get();

                jkh.removeHouse(houseOptional.get());
                jkhRepository.save(jkh);

                return ResponseEntity.ok(new ResponseDto(200, "Client deleted successfully!"));
            }
        }
        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "House not found!"), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/users/{id}/house")
    public ResponseEntity<?> getUserHouse(@PathVariable(name = "id") Long id) {
        Optional<User> userOptional = userService.findById(id);

        if (userOptional.isPresent()) {
            House house = userOptional.get().getHouse();

            return ResponseEntity.ok(new HouseDto(house));
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "User not found!"), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/house/{id}")
    public ResponseEntity<?> getHouse(@PathVariable(name = "id") Long id) {
        Optional<House> houseOptional = houseRepository.findById(id);

        if (houseOptional.isPresent()) {
            return ResponseEntity.ok(new HouseDto(houseOptional.get()));
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "User not found!"), HttpStatus.BAD_REQUEST);
    }
}
