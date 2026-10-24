package com.example.demo.modules.ai.service;

import com.example.demo.modules.ai.dto.FarmerFieldProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enterprise Multi-Turn Session Memory and Field Context State Machine.
 * Extracts crop, acreage, stage, and soil attributes across conversation turns,
 * preventing context drift and eliminating the need for farmers to repeat farm details.
 */
@Service
public class ConversationSessionService {

    private static final Logger log = LoggerFactory.getLogger(ConversationSessionService.class);

    public record ChatTurn(String role, String content, String provider, Instant timestamp) {}

    public static class SessionState {
        private final String sessionId;
        private final FarmerFieldProfile profile = new FarmerFieldProfile();
        private final List<ChatTurn> history = new ArrayList<>();
        private Instant lastAccessed = Instant.now();

        public SessionState(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public FarmerFieldProfile getProfile() {
            return profile;
        }

        public List<ChatTurn> getHistory() {
            return Collections.unmodifiableList(history);
        }

        public synchronized void addTurn(String role, String content, String provider) {
            history.add(new ChatTurn(role, content, provider, Instant.now()));
            if (history.size() > 14) {
                history.remove(0); // keep rolling 7 conversational rounds
            }
            this.lastAccessed = Instant.now();
        }

        public void resetProfile() {
            profile.setCrop(null);
            profile.setAcreage(null);
            profile.setSoilType(null);
            profile.setLocation(null);
            profile.setCropStage(null);
            profile.setDaysAfterSowing(null);
        }
    }

    private final Map<String, SessionState> sessions = new ConcurrentHashMap<>();

    // Regex extractors for Indian agricultural parameters
    private static final Pattern ACREAGE_PATTERN = Pattern.compile("(\\d+(\\.\\d+)?)\\s*(acres?|acre|bigha|hectares?|ha)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DAS_PATTERN = Pattern.compile("(\\d+)\\s*(das|days\\s*after\\s*sowing|days\\s*old)", Pattern.CASE_INSENSITIVE);

    private static final Map<String, String> CROP_PATTERNS = Map.ofEntries(
            Map.entry("wheat", "Wheat"),
            Map.entry("gehu", "Wheat"),
            Map.entry("tomato", "Tomato"),
            Map.entry("tamatar", "Tomato"),
            Map.entry("cotton", "Cotton"),
            Map.entry("kapas", "Cotton"),
            Map.entry("paddy", "Paddy / Rice"),
            Map.entry("rice", "Paddy / Rice"),
            Map.entry("dhan", "Paddy / Rice"),
            Map.entry("soybean", "Soybean"),
            Map.entry("potato", "Potato"),
            Map.entry("aloo", "Potato"),
            Map.entry("onion", "Onion"),
            Map.entry("pyaz", "Onion"),
            Map.entry("maize", "Maize / Corn"),
            Map.entry("corn", "Maize / Corn")
    );

    private static final Map<String, String> STAGE_PATTERNS = Map.of(
            "cri", "Crown Root Initiation (CRI)",
            "sowing", "Sowing / Seedling",
            "tillering", "Tillering",
            "flowering", "Flowering / Heading",
            "fruiting", "Fruit / Pod Formation",
            "harvest", "Maturity / Harvesting"
    );

    private static final Map<String, String> SOIL_PATTERNS = Map.of(
            "black", "Black Cotton Soil (Vertisol)",
            "clay", "Clay Loam",
            "sandy", "Sandy Loam",
            "red", "Red Loam Soil",
            "alluvial", "Alluvial Loam"
    );

    public SessionState getOrCreateSession(String sessionId) {
        String id = (sessionId != null && !sessionId.isBlank()) ? sessionId : "default_session";
        return sessions.computeIfAbsent(id, SessionState::new);
    }

    /**
     * Inspects prompt for context-reset commands or agricultural entities.
     */
    public SessionState processTurnContext(String sessionId, String prompt) {
        SessionState state = getOrCreateSession(sessionId);
        if (prompt == null || prompt.isBlank()) return state;

        String lower = prompt.toLowerCase(Locale.ROOT);

        // Check explicit reset request
        if (lower.contains("start over") || lower.contains("reset context") || lower.contains("new crop") || lower.contains("clear context")) {
            state.resetProfile();
            log.info("Reset context for session [{}]", sessionId);
            return state;
        }

        FarmerFieldProfile profile = state.getProfile();

        // 1. Detect Crop
        for (var entry : CROP_PATTERNS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                profile.setCrop(entry.getValue());
                break;
            }
        }

        // 2. Detect Acreage
        Matcher acreMatcher = ACREAGE_PATTERN.matcher(prompt);
        if (acreMatcher.find()) {
            try {
                double val = Double.parseDouble(acreMatcher.group(1));
                profile.setAcreage(val);
            } catch (Exception ignored) {}
        }

        // 3. Detect Days After Sowing (DAS)
        Matcher dasMatcher = DAS_PATTERN.matcher(prompt);
        if (dasMatcher.find()) {
            try {
                int das = Integer.parseInt(dasMatcher.group(1));
                profile.setDaysAfterSowing(das);
            } catch (Exception ignored) {}
        }

        // 4. Detect Crop Stage
        for (var entry : STAGE_PATTERNS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                profile.setCropStage(entry.getValue());
                break;
            }
        }

        // 5. Detect Soil Type
        for (var entry : SOIL_PATTERNS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                profile.setSoilType(entry.getValue());
                break;
            }
        }

        return state;
    }

    /**
     * Enriches a prompt with the active farmer profile context.
     */
    public String buildEnrichedPrompt(String sessionId, String rawPrompt) {
        SessionState state = getOrCreateSession(sessionId);
        FarmerFieldProfile profile = state.getProfile();

        if (!profile.hasContext()) {
            return rawPrompt;
        }

        // Prefix context summary
        return String.format("[Field Context: %s]\nQuestion: %s", profile.toContextSummary(), rawPrompt);
    }

    public void recordTurn(String sessionId, String role, String content, String provider) {
        SessionState state = getOrCreateSession(sessionId);
        state.addTurn(role, content, provider);
    }
}
