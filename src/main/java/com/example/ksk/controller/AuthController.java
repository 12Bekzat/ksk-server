package com.example.ksk.controller;

import com.example.ksk.dto.*;
import com.example.ksk.entity.Image;
import com.example.ksk.entity.User;
import com.example.ksk.error.AppError;
import com.example.ksk.service.AuthService;
import com.example.ksk.service.UserService;
import com.example.ksk.utils.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth")
    public ResponseEntity<?> createAuthToken(@RequestBody JwtRequest jwtRequest) {
        return authService.createAuthToken(jwtRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegUserDto regUserDto) {
        return authService.registerUser(regUserDto);
    }

    @GetMapping("/my/info")
    public ResponseEntity<?> getInfo() {
        return authService.getInfo();
    }

    @GetMapping("/my/role")
    public ResponseEntity<?> getRoles() {
        return authService.getRoles();
    }

    @PostMapping("/my/set/logo")
    public ResponseEntity<?> setUserLogo(@RequestParam("file") MultipartFile file) throws IOException {
        return authService.setUserLogo(file);
    }

    @PostMapping("/my/set/change")
    public ResponseEntity<?> setUserEdit(@RequestBody UserEditDto editDto) {
        return authService.setUserEdit(editDto);
    }

    @GetMapping("/set/roles")
    public String setRoles() {
        return authService.setRoles();
    }

    @GetMapping("/set/admin")
    public String setAdmin() {
        return authService.createAdmin();
    }
}
