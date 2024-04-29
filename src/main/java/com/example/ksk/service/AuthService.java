package com.example.ksk.service;

import com.example.ksk.dto.*;
import com.example.ksk.entity.Image;
import com.example.ksk.entity.User;
import com.example.ksk.error.AppError;
import com.example.ksk.utils.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    private final EmailSenderService emailSenderService;

    public ResponseEntity<?> createAuthToken(@RequestBody JwtRequest jwtRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(jwtRequest.getUsername(), jwtRequest.getPassword()));
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new AppError(HttpStatus.UNAUTHORIZED.value(), "Incorrect login or password"), HttpStatus.UNAUTHORIZED);
        }
        UserDetails userDetails = userService.loadUserByUsername(jwtRequest.getUsername());
        String token = jwtTokenUtils.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token));
    }

    public ResponseEntity<?> registerUser(@RequestBody RegUserDto regUserDto) {
        if (userService.findByUsername(regUserDto.getUsername()).isPresent()) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "User is already exist!"), HttpStatus.BAD_REQUEST);
        }
        User user = userService.createNewUser(regUserDto);
        emailSenderService.sendEmail(regUserDto.getEmail(), "Пароль от системы Urban Pluse Application!",
                "Здравствуйте, спасибо что воспользовались нашей системой!\nНаша команда очень рад помочь вам с услугами ЖКХ. Чтобы" +
                        " авторизоваться в наше приложение воспользуйтесь этим паролем! Затем смените этот пароль для безопасности вашего аккаунта!\n\nВаш пароль - " + regUserDto.getPassword());

        return ResponseEntity.ok(new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), user.isBanned(), user.getHouse() != null));
    }

    public ResponseEntity<?> getInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            System.out.println(authentication.getPrincipal());
            User user = userService.findByUsername(String.valueOf(authentication.getPrincipal())).get();
            return ResponseEntity.ok(new UserImageDto(
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getAvatar() == null ? -1 : user.getAvatar().getId()
            ));
        }

        return new ResponseEntity<>(new AppError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized user!"), HttpStatus.UNAUTHORIZED);
    }

    public ResponseEntity<?> getRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.findByUsername(String.valueOf(authentication.getPrincipal())).get();
            return ResponseEntity.ok(user.getRoles());
        }

        return new ResponseEntity<>(new AppError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized user!"), HttpStatus.UNAUTHORIZED);
    }

    public ResponseEntity<?> setUserLogo(MultipartFile file) throws IOException {
        System.out.println("Success image");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.findByUsername(String.valueOf(authentication.getPrincipal())).get();
            Image avatar = new Image();

            if (file.getSize() != 0) {
                System.out.println("Success image");
                avatar.setName(file.getName());
                avatar.setOriginalFileName(avatar.getOriginalFileName());
                avatar.setContentType(file.getContentType());
                avatar.setSize(file.getSize());
                avatar.setBytes(file.getBytes());

                user.setAvatar(avatar);
                userService.saveUser(user);

                return ResponseEntity.ok(new UserImageDto(user.getId(), user.getUsername(), user.getFullName(), user.getAvatar().getId()));
            }

            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Unauthorized user!"), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(new AppError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized user!"), HttpStatus.UNAUTHORIZED);
    }

    public ResponseEntity<?> setUserEdit(@RequestBody UserEditDto editDto) {
        if (!editDto.getPassword().equals(editDto.getConfirmPassword()) && editDto.isMe()) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Passwords are not equal!"), HttpStatus.BAD_REQUEST);
        }

        Optional<User> byUsername = userService.findByUsername(String.valueOf(editDto.getUsername()));

        if (byUsername.isPresent()) {
            User user = byUsername.get();
            user.setFullName(editDto.getFullName());
            user.setEmail(editDto.getEmail());
            if (editDto.isMe()) {
                user.setPassword(editDto.getPassword());
                userService.saveUserWithEncrypt(user);
            } else {
                userService.saveUser(user);
            }

            return ResponseEntity.ok(new UserDto(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.isBanned(),
                    user.getHouse() != null
            ));
        }
        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "User not found!"), HttpStatus.UNAUTHORIZED);
    }

    public String setRoles() {
        userService.setRoles();
        return "Ok!";
    }

    public String createAdmin() {
        userService.createAdmin();
        return "Ok!";
    }
}
