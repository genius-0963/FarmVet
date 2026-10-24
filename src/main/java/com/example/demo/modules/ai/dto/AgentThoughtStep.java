package com.example.demo.modules.ai.dto;

import java.time.Instant;

public class AgentThoughtStep {
    private String stepName;
    private String thought;
    private String action;
    private String observation;
    private Instant timestamp = Instant.now();

    public AgentThoughtStep() {}

    public AgentThoughtStep(String stepName, String thought, String action, String observation) {
        this.stepName = stepName;
        this.thought = thought;
        this.action = action;
        this.observation = observation;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getThought() {
        return thought;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
