package com.example.demo.modules.ai.dto;

import java.util.List;
import java.util.Map;

public class AgentExecutionResponse {
    private String agentReply;
    private List<AgentThoughtStep> agentSteps;
    private List<String> suggestedNextActions;
    private Map<String, Object> toolData;
    private boolean emergencyFallback;
    private String fallbackSpeciality;

    public AgentExecutionResponse() {}

    public AgentExecutionResponse(String agentReply, List<AgentThoughtStep> agentSteps, List<String> suggestedNextActions, Map<String, Object> toolData, boolean emergencyFallback, String fallbackSpeciality) {
        this.agentReply = agentReply;
        this.agentSteps = agentSteps;
        this.suggestedNextActions = suggestedNextActions;
        this.toolData = toolData;
        this.emergencyFallback = emergencyFallback;
        this.fallbackSpeciality = fallbackSpeciality;
    }

    public String getAgentReply() {
        return agentReply;
    }

    public void setAgentReply(String agentReply) {
        this.agentReply = agentReply;
    }

    public List<AgentThoughtStep> getAgentSteps() {
        return agentSteps;
    }

    public void setAgentSteps(List<AgentThoughtStep> agentSteps) {
        this.agentSteps = agentSteps;
    }

    public List<String> getSuggestedNextActions() {
        return suggestedNextActions;
    }

    public void setSuggestedNextActions(List<String> suggestedNextActions) {
        this.suggestedNextActions = suggestedNextActions;
    }

    public Map<String, Object> getToolData() {
        return toolData;
    }

    public void setToolData(Map<String, Object> toolData) {
        this.toolData = toolData;
    }

    public boolean isEmergencyFallback() {
        return emergencyFallback;
    }

    public void setEmergencyFallback(boolean emergencyFallback) {
        this.emergencyFallback = emergencyFallback;
    }

    public String getFallbackSpeciality() {
        return fallbackSpeciality;
    }

    public void setFallbackSpeciality(String fallbackSpeciality) {
        this.fallbackSpeciality = fallbackSpeciality;
    }
}
