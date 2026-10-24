package com.example.demo.modules.ai.service;

import com.example.demo.modules.ai.dto.FarmerFieldProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Production Multi-Provider AI Gateway & High-Performance Pipeline.
 * Orchestrates:
 *  1. Intent classification & Guardrail sanitization
 *  2. Multi-turn session memory & Dynamic prompt enrichment
 *  3. In-memory semantic query caching (< 5ms response time)
 *  4. External provider routing (Gemini, Groq, OpenRouter, Pollinations, Local Agro-AI)
 *  5. Agrochemical CIB&RC safety interception & PHI compliance
 */
@Service
public class FreeAiService {

    private static final Logger log = LoggerFactory.getLogger(FreeAiService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final FarmAgentService farmAgentService;
    private final ConversationSessionService sessionService;
    private final ChemicalSafetyInterceptor chemicalSafetyInterceptor;
    private final SemanticCacheService semanticCacheService;

    @Value("${gemini.api.key:}")
    private String defaultGeminiKey;

    @Value("${groq.api.key:}")
    private String defaultGroqKey;

    @Value("${openrouter.api.key:}")
    private String defaultOpenRouterKey;

    public FreeAiService(
            ObjectMapper objectMapper,
            FarmAgentService farmAgentService,
            ConversationSessionService sessionService,
            ChemicalSafetyInterceptor chemicalSafetyInterceptor,
            SemanticCacheService semanticCacheService) {
        this.objectMapper = objectMapper;
        this.farmAgentService = farmAgentService;
        this.sessionService = sessionService;
        this.chemicalSafetyInterceptor = chemicalSafetyInterceptor;
        this.semanticCacheService = semanticCacheService;

        this.restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(4000);
        factory.setReadTimeout(12000);
        this.restTemplate.setRequestFactory(factory);
    }

    public record FreeAiResult(
            String reply,
            String providerUsed,
            boolean guardrailPassed,
            FarmerFieldProfile farmerProfile,
            String safetyNotice,
            boolean cacheHit,
            long latencyMs
    ) {
        public FreeAiResult(String reply, String providerUsed, boolean guardrailPassed) {
            this(reply, providerUsed, guardrailPassed, null, null, false, 0L);
        }
    }

    public FreeAiResult generateFreeAiResponseWithMeta(String prompt, String provider, String customApiKey, String pageContext) {
        return generateFreeAiResponseWithMeta(prompt, provider, customApiKey, pageContext, "default_session");
    }

    public FreeAiResult generateFreeAiResponseWithMeta(String prompt, String provider, String customApiKey, String pageContext, String sessionId) {
        long startTime = System.currentTimeMillis();
        String sessId = (sessionId != null && !sessionId.isBlank()) ? sessionId : "default_session";

        // 1. Conversational Intent Routing & Guardrail Check
        var intentResult = ConversationalIntentClassifier.classify(prompt);
        if (intentResult.intent() == ConversationalIntentClassifier.Intent.OFF_TOPIC) {
            long latency = System.currentTimeMillis() - startTime;
            sessionService.recordTurn(sessId, "user", prompt, null);
            sessionService.recordTurn(sessId, "assistant", intentResult.prebuiltReply(), "Domain Guardrail");
            return new FreeAiResult(intentResult.prebuiltReply(), "Domain Guardrail", false, null, null, false, latency);
        }
        if (intentResult.prebuiltReply() != null) {
            long latency = System.currentTimeMillis() - startTime;
            sessionService.recordTurn(sessId, "user", prompt, null);
            sessionService.recordTurn(sessId, "assistant", intentResult.prebuiltReply(), "FarmVet Copilot");
            FarmerFieldProfile currentProfile = sessionService.getOrCreateSession(sessId).getProfile();
            return new FreeAiResult(intentResult.prebuiltReply(), "FarmVet Copilot", true, currentProfile, null, false, latency);
        }

        // 2. Session Context Extraction & Profile Update
        var sessionState = sessionService.processTurnContext(sessId, prompt);
        FarmerFieldProfile profile = sessionState.getProfile();
        String effectiveCrop = (profile.getCrop() != null) ? profile.getCrop() : pageContext;

        // 3. High-Performance Semantic Cache Check (< 5ms response)
        var cachedOpt = semanticCacheService.get(prompt, effectiveCrop);
        if (cachedOpt.isPresent()) {
            var cached = cachedOpt.get();
            long latency = System.currentTimeMillis() - startTime;
            sessionService.recordTurn(sessId, "user", prompt, null);
            sessionService.recordTurn(sessId, "assistant", cached.response(), cached.providerUsed());
            log.info("Serving query via Semantic Cache in {}ms for session [{}]", latency, sessId);
            return new FreeAiResult(cached.response(), cached.providerUsed(), true, profile, null, true, latency);
        }

        // 4. Prompt Context Enrichment
        String enrichedPrompt = sessionService.buildEnrichedPrompt(sessId, prompt);

        // 5. Provider Execution
        String chosenProvider = (provider != null) ? provider.toUpperCase(Locale.ROOT) : "AUTO";
        String trimmedKey = (customApiKey != null) ? customApiKey.trim() : "";
        String rawReply = null;
        String providerUsed = null;

        // Force Builtin Agro-AI
        if ("BUILTIN".equals(chosenProvider)) {
            rawReply = farmAgentService.executeAgent(enrichedPrompt, pageContext).getAgentReply();
            providerUsed = "FarmVet Built-in Agro-AI";
        }

        // Auto-detect Custom Key Provider if provided
        if (rawReply == null && !trimmedKey.isEmpty()) {
            if (trimmedKey.startsWith("gsk_") || "GROQ".equals(chosenProvider)) {
                try {
                    rawReply = callGroqFreeTier(enrichedPrompt, trimmedKey);
                    providerUsed = "Groq LLaMA-3.3-70B (Free Tier)";
                } catch (Exception e) {
                    log.warn("Custom Groq API failed: {}. Falling back.", e.getMessage());
                }
            } else if (trimmedKey.startsWith("sk-or-") || "OPENROUTER".equals(chosenProvider)) {
                try {
                    rawReply = callOpenRouterFreeTier(enrichedPrompt, trimmedKey);
                    providerUsed = "OpenRouter Free LLaMA-3.3 (Free Tier)";
                } catch (Exception e) {
                    log.warn("Custom OpenRouter API failed: {}. Falling back.", e.getMessage());
                }
            } else if (trimmedKey.startsWith("AIza") || "GEMINI".equals(chosenProvider) || "AUTO".equals(chosenProvider)) {
                try {
                    rawReply = callGeminiFreeTier(enrichedPrompt, trimmedKey);
                    providerUsed = "Google Gemini 1.5 Flash (Free Tier)";
                } catch (Exception e) {
                    log.warn("Custom Gemini API failed: {}. Falling back.", e.getMessage());
                }
            }
        }

        // Server-configured default keys (if set in environment)
        if (rawReply == null && defaultGeminiKey != null && !defaultGeminiKey.isBlank() && ("GEMINI".equals(chosenProvider) || "AUTO".equals(chosenProvider))) {
            try {
                rawReply = callGeminiFreeTier(enrichedPrompt, defaultGeminiKey);
                providerUsed = "Google Gemini 1.5 Flash (Server Free Tier)";
            } catch (Exception e) {
                log.warn("Default Gemini Key failed: {}. Falling back.", e.getMessage());
            }
        }

        if (rawReply == null && defaultGroqKey != null && !defaultGroqKey.isBlank() && ("GROQ".equals(chosenProvider) || "AUTO".equals(chosenProvider))) {
            try {
                rawReply = callGroqFreeTier(enrichedPrompt, defaultGroqKey);
                providerUsed = "Groq LLaMA-3.3-70B (Server Free Tier)";
            } catch (Exception e) {
                log.warn("Default Groq Key failed: {}. Falling back.", e.getMessage());
            }
        }

        if (rawReply == null && defaultOpenRouterKey != null && !defaultOpenRouterKey.isBlank() && ("OPENROUTER".equals(chosenProvider) || "AUTO".equals(chosenProvider))) {
            try {
                rawReply = callOpenRouterFreeTier(enrichedPrompt, defaultOpenRouterKey);
                providerUsed = "OpenRouter Free Tier";
            } catch (Exception e) {
                log.warn("Default OpenRouter Key failed: {}. Falling back.", e.getMessage());
            }
        }

        // Explicit Pollinations Request
        if (rawReply == null && "POLLINATIONS".equals(chosenProvider)) {
            try {
                rawReply = callPollinationsFree(enrichedPrompt);
                providerUsed = "Open-Source Free LLM (Pollinations AI)";
            } catch (Exception e) {
                log.warn("Pollinations Free API failed: {}. Falling back to Agro-AI.", e.getMessage());
            }
        }

        // Default & Instant Fallback: FarmVet Built-in Agro-AI Engine
        if (rawReply == null) {
            rawReply = farmAgentService.executeAgent(enrichedPrompt, pageContext).getAgentReply();
            providerUsed = "FarmVet Built-in Agro-AI (Zero Cost & Unlimited)";
        }

        // 6. Regulatory Agrochemical Safety Interceptor & PHI Audit
        var safetyAudit = chemicalSafetyInterceptor.auditPrescription(enrichedPrompt, rawReply, null);
        String finalReply = rawReply;
        String safetyNotice = null;

        if (!safetyAudit.approved() && safetyAudit.blockedHazard()) {
            finalReply = safetyAudit.safeRecommendation();
            safetyNotice = "🛡️ CIB&RC Regulatory Alert: Banned chemical was detected and safely intercepted.";
        } else if (safetyAudit.warningNotice() != null && !safetyAudit.warningNotice().isBlank()) {
            safetyNotice = safetyAudit.warningNotice();
        }

        // Append 15L Knapsack Tank Dilution Math if spray treatment is discussed
        String lowerReply = finalReply.toLowerCase(Locale.ROOT);
        if ((lowerReply.contains("spray") || lowerReply.contains("fungicide") || lowerReply.contains("pesticide") || lowerReply.contains("insecticide"))
                && !finalReply.contains("Knapsack Tank Dilution Math")) {
            finalReply = finalReply + "\n\n" + safetyAudit.tankMixGuidance();
        }

        // 7. Store in Semantic Cache for instant repeat response
        semanticCacheService.put(prompt, effectiveCrop, finalReply, providerUsed);

        // 8. Record Multi-Turn Conversation
        sessionService.recordTurn(sessId, "user", prompt, null);
        sessionService.recordTurn(sessId, "assistant", finalReply, providerUsed);

        long totalLatency = System.currentTimeMillis() - startTime;
        return new FreeAiResult(finalReply, providerUsed, true, profile, safetyNotice, false, totalLatency);
    }

    public String generateFreeAiResponse(String prompt, String provider, String customApiKey, String pageContext) {
        return generateFreeAiResponseWithMeta(prompt, provider, customApiKey, pageContext, "default_session").reply();
    }

    public SseEmitter streamFreeAiResponse(String prompt, String provider, String customApiKey, String pageContext) {
        return streamFreeAiResponse(prompt, provider, customApiKey, pageContext, "default_session");
    }

    public SseEmitter streamFreeAiResponse(String prompt, String provider, String customApiKey, String pageContext, String sessionId) {
        SseEmitter emitter = new SseEmitter(120_000L);
        CompletableFuture.runAsync(() -> {
            try {
                // Guardrail Intent Pre-check
                var intent = ConversationalIntentClassifier.classify(prompt);
                if (intent.intent() == ConversationalIntentClassifier.Intent.OFF_TOPIC) {
                    emitter.send(SseEmitter.event().name("guardrail_refusal").data(ConversationalIntentClassifier.getOffTopicResponse()));
                    emitter.complete();
                    return;
                }

                String providerLabel = provider != null ? provider : "Agro-AI";
                emitter.send(SseEmitter.event().name("status").data("Analyzing agronomic data via " + providerLabel + "..."));

                FreeAiResult result = generateFreeAiResponseWithMeta(prompt, provider, customApiKey, pageContext, sessionId);

                // Stream response in smooth natural chunks
                String reply = result.reply();
                int chunkSize = 20;
                for (int i = 0; i < reply.length(); i += chunkSize) {
                    int end = Math.min(i + chunkSize, reply.length());
                    emitter.send(SseEmitter.event().name("token").data(reply.substring(i, end)));
                    try {
                        Thread.sleep(15);
                    } catch (InterruptedException ignored) {}
                }

                Map<String, Object> meta = Map.of(
                        "providerUsed", result.providerUsed(),
                        "guardrailPassed", result.guardrailPassed(),
                        "cacheHit", result.cacheHit(),
                        "latencyMs", result.latencyMs(),
                        "safetyNotice", result.safetyNotice() != null ? result.safetyNotice() : "",
                        "farmerProfile", result.farmerProfile() != null ? result.farmerProfile() : Collections.emptyMap(),
                        "status", "SUCCESS"
                );
                emitter.send(SseEmitter.event().name("complete").data(objectMapper.writeValueAsString(meta)));
                emitter.complete();
            } catch (Exception e) {
                log.error("Error streaming Free AI response", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("Free AI error: " + e.getMessage()));
                    emitter.complete();
                } catch (Exception ignored) {}
            }
        });
        return emitter;
    }

    private String callGeminiFreeTier(String prompt, String apiKey) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        String systemInstruction = "You are FarmVet AI, an elite Agricultural Agronomist and Veterinary Healthcare Assistant. " +
                "You strictly answer queries about crops, soil health, irrigation schedules, fertilizer NPK dosages, plant pathology, pesticides with Pre-Harvest Intervals (PHI), livestock healthcare (cattle, buffalo, poultry), and Mandi market prices. " +
                "Refuse non-agricultural queries politely. Format responses in clear markdown with bullet points.";

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> systemPart = Map.of("role", "user", "parts", List.of(Map.of("text", systemInstruction)));
        Map<String, Object> userPart = Map.of("role", "user", "parts", List.of(Map.of("text", prompt)));

        requestBody.put("contents", List.of(systemPart, userPart));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode textNode = root.at("/candidates/0/content/parts/0/text");
            if (!textNode.isMissingNode()) {
                return textNode.asText();
            }
        }
        throw new RuntimeException("Empty response from Gemini API");
    }

    private String callGroqFreeTier(String prompt, String apiKey) throws Exception {
        String url = "https://api.groq.com/openai/v1/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are FarmVet AI, an agricultural agronomist and veterinarian. Answer only farm, crop, and animal health topics."),
                Map.of("role", "user", "content", prompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode content = root.at("/choices/0/message/content");
            if (!content.isMissingNode()) {
                return content.asText();
            }
        }
        throw new RuntimeException("Empty response from Groq API");
    }

    private String callPollinationsFree(String prompt) throws Exception {
        String url = "https://text.pollinations.ai/";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are FarmVet AI, an agricultural agronomist. Answer strictly regarding crops, fertilizers, and farm issues."),
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("model", "openai");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }
        throw new RuntimeException("Pollinations API failed");
    }

    private String callOpenRouterFreeTier(String prompt, String apiKey) throws Exception {
        String url = "https://openrouter.ai/api/v1/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "meta-llama/llama-3.3-70b-instruct:free");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are FarmVet AI, an expert agricultural agronomist and veterinarian. Answer only crop, soil, fertilizer, livestock, and mandi market questions in clear markdown format."),
                Map.of("role", "user", "content", prompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", "http://localhost:3000");
        headers.set("X-Title", "FarmVet AI Copilot");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode content = root.at("/choices/0/message/content");
            if (!content.isMissingNode()) {
                return content.asText();
            }
        }
        throw new RuntimeException("Empty response from OpenRouter API");
    }
}
