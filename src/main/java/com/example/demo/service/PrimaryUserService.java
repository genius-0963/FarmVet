package com.example.demo.service;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.PrimaryUser;
import com.example.demo.repository.PrimaryUserRepository;

@Service
public class PrimaryUserService {

    @Autowired
    private PrimaryUserRepository primaryUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<PrimaryUser> getUserById(Long id) {
        return primaryUserRepository.findById(id);
    }

    public String registerUser(PrimaryUser registerRequest) {

        if (primaryUserRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }
        PrimaryUser user = new PrimaryUser();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setRole(registerRequest.getRole());
        primaryUserRepository.save(user);
        return "User registered successfully!";
    }
}
