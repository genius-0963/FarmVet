package com.example.demo.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.PrimaryUser;
import com.example.demo.repository.PrimaryUserRepository;
import com.example.demo.service.PrimaryUserService;

@CrossOrigin(origins = "http://farmvetconnect.vercel.app")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private PrimaryUserService userService;

    @Autowired
    private PrimaryUserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody PrimaryUser registerRequest) {
        String response = userService.registerUser(registerRequest);
        return ResponseEntity.ok(response);
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<PrimaryUser> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        Optional<PrimaryUser> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            PrimaryUser user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.ok(user);
            }
        }
        return ResponseEntity.status(401).build();
    }
}
