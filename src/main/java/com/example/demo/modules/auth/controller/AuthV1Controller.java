package com.example.demo.modules.auth.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.common.exception.ResourceNotFoundException;
import com.example.demo.common.exception.UnauthorizedException;
import com.example.demo.common.storage.FileStorageService;
import com.example.demo.model.PrimaryUser;
import com.example.demo.model.Role;
import com.example.demo.modules.auth.dto.AuthResponse;
import com.example.demo.modules.auth.dto.LoginRequest;
import com.example.demo.modules.auth.dto.RegisterRequest;
import com.example.demo.modules.auth.dto.UserProfileDto;
import com.example.demo.repository.PrimaryUserRepository;
import com.example.demo.security.JwtTokenProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthV1Controller {

    private final PrimaryUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final FileStorageService fileStorageService;

    public AuthV1Controller(PrimaryUserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            JwtTokenProvider tokenProvider,
                            FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }

        Role role = Role.fromString(request.getRole());
        String lang = (request.getPreferredLanguage() != null && !request.getPreferredLanguage().isBlank())
                ? request.getPreferredLanguage() : "en";

        PrimaryUser newUser = new PrimaryUser();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPhone(request.getPhone());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(role.name());
        newUser.setPreferredLanguage(lang);
        newUser.setKycStatus("PENDING");

        PrimaryUser saved = userRepository.save(newUser);

        String token = tokenProvider.generateTokenForUser(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getRole());
        AuthResponse authResponse = new AuthResponse(token, saved.getId(), saved.getUsername(), saved.getEmail(), saved.getRole(), saved.getPreferredLanguage());

        return ResponseEntity.ok(ApiResponse.ok(authResponse, "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        PrimaryUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String token = tokenProvider.generateTokenForUser(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
        AuthResponse authResponse = new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getPreferredLanguage());

        return ResponseEntity.ok(ApiResponse.ok(authResponse, "Login successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        PrimaryUser user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserProfileDto dto = new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getPreferredLanguage(),
                user.getKycStatus(),
                user.getKycDocumentUrl()
        );
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping(value = "/kyc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileDto>> uploadKycDocument(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        if (userDetails == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        PrimaryUser user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String storedPath = fileStorageService.storeFile(file, "kyc-documents");
        user.setKycDocumentUrl(storedPath);
        user.setKycStatus("SUBMITTED");
        userRepository.save(user);

        UserProfileDto dto = new UserProfileDto(
                user.getId(), user.getUsername(), user.getEmail(), user.getPhone(),
                user.getRole(), user.getPreferredLanguage(), user.getKycStatus(), user.getKycDocumentUrl()
        );
        return ResponseEntity.ok(ApiResponse.ok(dto, "KYC document uploaded successfully"));
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<UserProfileDto>> updatePreferences(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        PrimaryUser user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (payload.containsKey("preferredLanguage")) {
            user.setPreferredLanguage(payload.get("preferredLanguage"));
            userRepository.save(user);
        }

        UserProfileDto dto = new UserProfileDto(
                user.getId(), user.getUsername(), user.getEmail(), user.getPhone(),
                user.getRole(), user.getPreferredLanguage(), user.getKycStatus(), user.getKycDocumentUrl()
        );
        return ResponseEntity.ok(ApiResponse.ok(dto, "Preferences updated"));
    }
}
