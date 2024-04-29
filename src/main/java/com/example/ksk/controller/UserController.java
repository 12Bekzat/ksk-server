package com.example.ksk.controller;

import com.example.ksk.dto.HouseDto;
import com.example.ksk.dto.JkhDto;
import com.example.ksk.dto.ResponseDto;
import com.example.ksk.dto.UserDto;
import com.example.ksk.entity.House;
import com.example.ksk.entity.Jkh;
import com.example.ksk.entity.User;
import com.example.ksk.error.AppError;
import com.example.ksk.repository.HouseRepository;
import com.example.ksk.repository.RoleRepository;
import com.example.ksk.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final HouseRepository houseRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        List<User> all = userService.findAll();

        all = all.stream().filter((User item) -> !item.getRoles().contains(roleRepository.findByName("ROLE_ADMIN").get())).toList();
        List<UserDto> allUsers = all.stream().map(emp -> {
            System.out.print(emp.getFullName() + " ");
            System.out.print(emp.getHouse() != null);
            System.out.println();
            return new UserDto(emp.getId(), emp.getUsername(), emp.getEmail(), emp.getFullName(), emp.isBanned(), emp.getHouse() != null);
        }).toList();

        return ResponseEntity.ok(allUsers);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable(name = "id") Long id) {
        Optional<User> byId = userService.findById(id);

        if (byId.isPresent()) {
            return ResponseEntity.ok(new UserDto(id, byId.get().getUsername(), byId.get().getEmail(), byId.get().getFullName(), byId.get().isBanned(), byId.get().getHouse() != null));
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "User not found!"), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/users/{id}/jkh")
    public ResponseEntity<?> getUserJkh(@PathVariable(name = "id") Long id) {
        Optional<User> byId = userService.findById(id);

        if (byId.isPresent()) {
            Jkh jkh = byId.get().getJkh();

            return ResponseEntity.ok(new JkhDto(jkh.getId(), jkh.getLegalAddress(), jkh.getInn(), jkh.getKpp(), jkh.getName(), jkh.getBankAccount(), jkh.getPhoneNumber()));
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "User not found!"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/users/{id}/ban")
    public ResponseEntity<?> banUser(@PathVariable(name = "id") Long id) {
        Optional<User> byId = userService.findById(id);

        if (byId.isPresent()) {
            User user = byId.get();
            user.setBanned(true);
            userService.saveUser(user);

            return ResponseEntity.ok(new UserDto(id, byId.get().getUsername(), byId.get().getEmail(), byId.get().getFullName(), byId.get().isBanned(), byId.get().getHouse() != null));
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "User not found!"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/users/{id}/unban")
    public ResponseEntity<?> unbanUser(@PathVariable(name = "id") Long id) {
        Optional<User> byId = userService.findById(id);

        if (byId.isPresent()) {
            User user = byId.get();
            user.setBanned(false);
            userService.saveUser(user);

            return ResponseEntity.ok(new UserDto(id, byId.get().getUsername(), byId.get().getEmail(), byId.get().getFullName(), byId.get().isBanned(), byId.get().getHouse() != null));
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "User not found!"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/users/{id}/remove")
    public ResponseEntity<?> removeUser(@PathVariable(name = "id") Long id) {
        userService.removeUser(id);

        return ResponseEntity.ok(new ResponseDto(200, "User deleted successfully!"));
    }

    @GetMapping("/users/employee")
    public ResponseEntity<?> getAllEmployee() {
        List<User> all = userService.findAll();

        all = all.stream().filter((User item) -> item.getRoles().contains(roleRepository.findByName("ROLE_KSK").get()) && item.getJkh() == null).toList();

        return ResponseEntity.ok(all);
    }

    @GetMapping("/password")
    public ResponseEntity<?> getPassword() {
        return ResponseEntity.ok(passwordEncoder.encode("admin1234"));
    }
}
