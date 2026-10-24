package com.example.demo.modules.ai.dto;

public class FreeAiChatRequest {
    private String prompt;
    private String provider; // "AUTO", "GEMINI", "GROQ", "POLLINATIONS", "BUILTIN"
    private String customApiKey;
    private String pageContext;
    private String sessionId;

    public FreeAiChatRequest() {}

    public FreeAiChatRequest(String prompt, String provider, String customApiKey, String pageContext) {
        this.prompt = prompt;
        this.provider = provider;
        this.customApiKey = customApiKey;
        this.pageContext = pageContext;
    }

    public FreeAiChatRequest(String prompt, String provider, String customApiKey, String pageContext, String sessionId) {
        this.prompt = prompt;
        this.provider = provider;
        this.customApiKey = customApiKey;
        this.pageContext = pageContext;
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getCustomApiKey() {
        return customApiKey;
    }

    public void setCustomApiKey(String customApiKey) {
        this.customApiKey = customApiKey;
    }

    public String getPageContext() {
        return pageContext;
    }

    public void setPageContext(String pageContext) {
        this.pageContext = pageContext;
    }
}
