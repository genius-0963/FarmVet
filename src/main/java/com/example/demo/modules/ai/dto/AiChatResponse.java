package com.example.demo.modules.ai.dto;

import java.util.List;
import java.util.Map;

public class AiChatResponse {
    private String answer;
    private List<String> toolsExecuted;
    private boolean fallbackToExpertRecommended;
    private String suggestedExpertSpeciality;
    private Map<String, Object> toolPayloads;

    public AiChatResponse() {}

    public AiChatResponse(String answer, List<String> toolsExecuted, boolean fallbackToExpertRecommended, String suggestedExpertSpeciality, Map<String, Object> toolPayloads) {
        this.answer = answer;
        this.toolsExecuted = toolsExecuted;
        this.fallbackToExpertRecommended = fallbackToExpertRecommended;
        this.suggestedExpertSpeciality = suggestedExpertSpeciality;
        this.toolPayloads = toolPayloads;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getToolsExecuted() {
        return toolsExecuted;
    }

    public void setToolsExecuted(List<String> toolsExecuted) {
        this.toolsExecuted = toolsExecuted;
    }

    public boolean isFallbackToExpertRecommended() {
        return fallbackToExpertRecommended;
    }

    public void setFallbackToExpertRecommended(boolean fallbackToExpertRecommended) {
        this.fallbackToExpertRecommended = fallbackToExpertRecommended;
    }

    public String getSuggestedExpertSpeciality() {
        return suggestedExpertSpeciality;
    }

    public void setSuggestedExpertSpeciality(String suggestedExpertSpeciality) {
        this.suggestedExpertSpeciality = suggestedExpertSpeciality;
    }

    public Map<String, Object> getToolPayloads() {
        return toolPayloads;
    }

    public void setToolPayloads(Map<String, Object> toolPayloads) {
        this.toolPayloads = toolPayloads;
    }
}
