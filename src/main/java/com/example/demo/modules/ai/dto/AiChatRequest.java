package com.example.demo.modules.ai.dto;

public class AiChatRequest {
    private String prompt;
    private String contextCategory; // CROP, LIVESTOCK, MARKET, IRRIGATION, GENERAL
    private String language;
    private Double latitude;
    private Double longitude;

    public AiChatRequest() {}

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getContextCategory() {
        return contextCategory;
    }

    public void setContextCategory(String contextCategory) {
        this.contextCategory = contextCategory;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
