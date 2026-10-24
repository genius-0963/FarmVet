package com.example.demo.modules.ai.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.modules.ai.dto.AgentExecutionResponse;
import com.example.demo.modules.ai.dto.AiChatRequest;
import com.example.demo.modules.ai.dto.AiChatResponse;
import com.example.demo.modules.ai.dto.FarmerFieldProfile;
import com.example.demo.modules.ai.dto.FreeAiChatRequest;
import com.example.demo.modules.ai.service.AiOrchestratorService;
import com.example.demo.modules.ai.service.ConversationSessionService;
import com.example.demo.modules.ai.service.FarmAgentService;
import com.example.demo.modules.ai.service.FreeAiService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiOrchestratorController {

    private final AiOrchestratorService orchestratorService;
    private final FarmAgentService farmAgentService;
    private final FreeAiService freeAiService;
    private final ConversationSessionService sessionService;

    public AiOrchestratorController(
            AiOrchestratorService orchestratorService,
            FarmAgentService farmAgentService,
            FreeAiService freeAiService,
            ConversationSessionService sessionService) {
        this.orchestratorService = orchestratorService;
        this.farmAgentService = farmAgentService;
        this.freeAiService = freeAiService;
        this.sessionService = sessionService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(@RequestBody AiChatRequest request) {
        AiChatResponse response = orchestratorService.orchestrateQuery(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam("prompt") String prompt) {
        return orchestratorService.streamQuery(prompt);
    }

    @PostMapping("/agent/execute")
    public ResponseEntity<ApiResponse<AgentExecutionResponse>> executeAgent(@RequestBody Map<String, String> payload) {
        String prompt = payload.getOrDefault("prompt", "");
        String pageContext = payload.getOrDefault("pageContext", "GENERAL");
        AgentExecutionResponse response = farmAgentService.executeAgent(prompt, pageContext);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping(value = "/agent/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAgent(
            @RequestParam("prompt") String prompt,
            @RequestParam(value = "pageContext", defaultValue = "GENERAL") String pageContext) {
        return farmAgentService.streamAgentExecution(prompt, pageContext);
    }

    @PostMapping("/free-chat")
    public ResponseEntity<ApiResponse<FreeAiService.FreeAiResult>> freeChat(@RequestBody FreeAiChatRequest request) {
        FreeAiService.FreeAiResult result = freeAiService.generateFreeAiResponseWithMeta(
                request.getPrompt(),
                request.getProvider(),
                request.getCustomApiKey(),
                request.getPageContext(),
                request.getSessionId()
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping(value = "/free-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamFreeAi(
            @RequestParam("prompt") String prompt,
            @RequestParam(value = "provider", defaultValue = "AUTO") String provider,
            @RequestParam(value = "customApiKey", required = false) String customApiKey,
            @RequestParam(value = "pageContext", defaultValue = "GENERAL") String pageContext,
            @RequestParam(value = "sessionId", required = false) String sessionId) {
        return freeAiService.streamFreeAiResponse(prompt, provider, customApiKey, pageContext, sessionId);
    }

    @PostMapping("/session/reset")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetSession(
            @RequestParam(value = "sessionId", defaultValue = "default_session") String sessionId) {
        sessionService.getOrCreateSession(sessionId).resetProfile();
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "sessionId", sessionId,
                "status", "CONTEXT_RESET_SUCCESS"
        )));
    }

    @GetMapping("/session/profile")
    public ResponseEntity<ApiResponse<FarmerFieldProfile>> getSessionProfile(
            @RequestParam(value = "sessionId", defaultValue = "default_session") String sessionId) {
        FarmerFieldProfile profile = sessionService.getOrCreateSession(sessionId).getProfile();
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }
}
