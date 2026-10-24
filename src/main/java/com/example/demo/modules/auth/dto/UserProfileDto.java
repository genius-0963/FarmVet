package com.example.demo.modules.auth.dto;

public class UserProfileDto {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String preferredLanguage;
    private String kycStatus;
    private String kycDocumentUrl;

    public UserProfileDto() {}

    public UserProfileDto(Long id, String username, String email, String phone, String role, String preferredLanguage, String kycStatus, String kycDocumentUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.preferredLanguage = preferredLanguage;
        this.kycStatus = kycStatus;
        this.kycDocumentUrl = kycDocumentUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public String getKycDocumentUrl() {
        return kycDocumentUrl;
    }

    public void setKycDocumentUrl(String kycDocumentUrl) {
        this.kycDocumentUrl = kycDocumentUrl;
    }
}
