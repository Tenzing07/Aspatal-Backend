package com.aspatal.hospitalmanagementbackend.controller;

import com.aspatal.hospitalmanagementbackend.dto.LoginDto;
import com.aspatal.hospitalmanagementbackend.dto.RegisterDto;
import com.aspatal.hospitalmanagementbackend.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseEntity<String>> register(@Valid @RequestBody RegisterDto request) throws MessagingException {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token) {
        String result = userService.verifyUser(token);
        if (result.equals("Account verified successfully!")) {
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", "http://localhost:4200/login").build();
        }
        return ResponseEntity.badRequest().body("Invalid verification token!");
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseEntity<?>> login(@Valid @RequestBody LoginDto request) {
        return ResponseEntity.ok(userService.login(request));
    }
}
