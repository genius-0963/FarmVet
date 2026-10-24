package com.example.demo.model;

public enum Role {
    ROLE_FARMER,
    ROLE_EXPERT,
    ROLE_ADMIN;

    public static Role fromString(String roleStr) {
        if (roleStr == null || roleStr.trim().isEmpty()) {
            return ROLE_FARMER;
        }
        String formatted = roleStr.trim().toUpperCase();
        if (!formatted.startsWith("ROLE_")) {
            formatted = "ROLE_" + formatted;
        }
        try {
            return Role.valueOf(formatted);
        } catch (IllegalArgumentException e) {
            return ROLE_FARMER;
        }
    }
}
