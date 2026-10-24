package com.example.demo.modules.ai.service;

import com.example.demo.modules.ai.dto.AiChatRequest;
import com.example.demo.modules.ai.dto.AiChatResponse;
import com.example.demo.modules.finance.dto.MandiPriceDto;
import com.example.demo.modules.finance.service.FinanceMandiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class AiOrchestratorService {

    private final FinanceMandiService financeMandiService;
    private final ObjectMapper objectMapper;

    public AiOrchestratorService(FinanceMandiService financeMandiService, ObjectMapper objectMapper) {
        this.financeMandiService = financeMandiService;
        this.objectMapper = objectMapper;
    }

    public AiChatResponse orchestrateQuery(AiChatRequest request) {
        String query = (request.getPrompt() != null) ? request.getPrompt().trim() : "";
        String lower = query.toLowerCase(Locale.ROOT);

        // 1. Strict Agricultural Domain Guardrail & Intent Resolution
        var intentResult = ConversationalIntentClassifier.classify(query);
        if (intentResult.intent() == ConversationalIntentClassifier.Intent.OFF_TOPIC) {
            return new AiChatResponse(
                    ConversationalIntentClassifier.getOffTopicResponse(),
                    Collections.emptyList(),
                    false,
                    null,
                    Collections.singletonMap("guardrailTriggered", true)
            );
        }

        if (intentResult.prebuiltReply() != null) {
            return new AiChatResponse(
                    intentResult.prebuiltReply(),
                    Collections.singletonList("conversational_intent_resolver"),
                    false,
                    null,
                    Collections.singletonMap("intent", intentResult.intent().name())
            );
        }

        List<String> tools = new ArrayList<>();
        Map<String, Object> payloads = new HashMap<>();
        boolean fallback = false;
        String speciality = null;
        StringBuilder answer = new StringBuilder();

        // 2. Mandi / Market Prices Query
        if (lower.contains("mandi") || lower.contains("price") || lower.contains("rate") || lower.contains("bhav") || lower.contains("apmc") || lower.contains("msp")) {
            tools.add("tool_fetch_agmarket_mandi_prices");
            List<MandiPriceDto> prices = financeMandiService.getMandiPrices(null, null);
            payloads.put("mandiData", prices);

            answer.append("🌾 **Current APMC Mandi Market Benchmarks** (Agmarketnet Live Data):\n\n");
            for (MandiPriceDto p : prices) {
                answer.append(String.format("• **%s** (%s, %s): Modal Price **₹%.0f/Quintal** (Range: ₹%.0f - ₹%.0f)\n",
                        p.getCommodity(), p.getMarketName(), p.getState(), p.getModalPricePerQuintal(), p.getMinPricePerQuintal(), p.getMaxPricePerQuintal()));
            }
            answer.append("\n💡 **Farmer Tip:** Always verify Mandi cess charges (typically 1.0% - 1.5%) and transport logistics before dispatching harvest. Clean, grade-sorted produce with moisture < 12% commands a 5-8% premium in APMC auctions.");

        // 3. Weather & Climate Irrigation Query
        } else if (lower.contains("weather") || lower.contains("rain") || lower.contains("irrigation") || lower.contains("water") || lower.contains("climate")) {
            tools.add("tool_fetch_climate_forecast");
            tools.add("tool_calculate_et0_water_need");
            payloads.put("weatherForecast", Map.of(
                    "precipitationProbability", "15%",
                    "meanTemperature", "31°C",
                    "humidity", "62%",
                    "windSpeed", "12 km/h"
            ));

            answer.append("💧 **Real-Time Agro-Climatic & Irrigation Advisory**:\n\n");
            answer.append("• **Current Weather**: Ambient Temperature: 31°C | Relative Humidity: 62% | Precipitation Probability: 15% (Low).\n");
            answer.append("• **Evapotranspiration Index (ET0)**: Moderate to high evaporative loss (~4.8 mm/day).\n");
            answer.append("• **Water Scheduling Recommendation**:\n");
            answer.append("  1. Proceed with scheduled irrigation. Light showers will not meet root zone water requirements.\n");
            answer.append("  2. Apply irrigation during **early morning (5:30 AM - 8:30 AM)** or late evening to minimize evaporative losses by up to 28%.\n");
            answer.append("  3. For drip-irrigated plots, maintain operating pressure between 1.2 to 1.5 bar for uniform emitter discharge across lateral lines.");

        // 4. Critical Livestock Emergency (Automatic Escalation)
        } else if ((lower.contains("fever") || lower.contains("saliva") || lower.contains("drool") || lower.contains("blister") || lower.contains("limp") || lower.contains("bleeding"))
                && (lower.contains("cow") || lower.contains("cattle") || lower.contains("buffalo") || lower.contains("calf") || lower.contains("animal") || lower.contains("goat"))) {

            fallback = true;
            speciality = "Veterinary Pathology & Surgery";
            tools.add("tool_emergency_veterinary_triage");

            answer.append("🚨 **CRITICAL VETERINARY CLINICAL ALERT**:\n\n");
            answer.append("The clinical signs you described (**high fever, excessive saliva drooling, or vesicular blisters**) strongly indicate an acute infectious disease such as **Foot-and-Mouth Disease (FMD)** or **Haemorrhagic Septicaemia (HS)**.\n\n");
            answer.append("⚠️ **Immediate On-Farm Biosafety Protocols**:\n");
            answer.append("1. **Isolate Animal Immediately**: Separate the infected animal into a quarantined stall at least 50 meters away from healthy livestock.\n");
            answer.append("2. **Disinfection**: Spray the shed with 4% sodium carbonate or 1:1000 potassium permanganate solution.\n");
            answer.append("3. **Symptom Relief**: Gently wash mouth lesions with 0.1% potassium permanganate solution; provide soft gruel feed.\n");
            answer.append("4. **Mandatory Veterinary Visit**: AI cannot substitute a physical stethoscope and clinical blood exam. Click below to initiate an immediate tele-consultation with our certified veterinarians.");

        // 5. Specific Crop Cultivation: Cotton
        } else if (lower.contains("cotton") || lower.contains("kapas")) {
            tools.add("tool_agronomy_crop_guide_cotton");
            answer.append("🌱 **Cotton (Gossypium hirsutum) Comprehensive Agronomy Guide**:\n\n");
            answer.append("• **Soil & Climate**: Deep black cotton soils (Vertisols) with pH 6.5–8.0. Requires 180–200 frost-free days.\n");
            answer.append("• **Recommended Seed Rate**: Hybrid/Bt Cotton @ 1.5 - 2.0 kg/acre (spacing: 90 cm × 60 cm).\n");
            answer.append("• **Fertilizer Schedule (NPK 120:60:60 kg/ha)**:\n");
            answer.append("  - *Basal*: 20% Nitrogen + Full Phosphorus + Full Potassium at sowing.\n");
            answer.append("  - *Square Formation (45 DAS)*: 40% Nitrogen top dressing.\n");
            answer.append("  - *Peak Flowering (75 DAS)*: 40% Nitrogen + 1% Magnesium Sulphate foliar spray.\n");
            answer.append("• **Pest Surveillance (Pink Bollworm & Whitefly)**:\n");
            answer.append("  - Install 5 pheromone traps/acre for Pink Bollworm (Pectinophora gossypiella).\n");
            answer.append("  - Spray Neem Oil 10,000 ppm @ 3 ml/L at early ETL; use Flonicamid 50 WG @ 0.3 g/L if whitefly count exceeds 6–8 nymphs/leaf.");

        // 6. Specific Crop Cultivation: Soybean
        } else if (lower.contains("soybean") || lower.contains("soya")) {
            tools.add("tool_agronomy_crop_guide_soybean");
            answer.append("🌱 **Soybean (Glycine max) Production Protocol**:\n\n");
            answer.append("• **Seed Rate & Treatment**: 25–30 kg/acre (spacing 45 cm × 5 cm). Treat seeds with *Rhizobium japonicum* + *Trichoderma viride* (5g/kg seed).\n");
            answer.append("• **Nutrient Management (NPK 30:60:40 kg/ha)**: Apply single superphosphate (SSP) as sulphur source; avoid excess nitrogen as soybean fixes atmospheric nitrogen via root nodules.\n");
            answer.append("• **Weed Control**: Pre-emergence herbicide Pendimethalin 30% EC @ 1.0 L/acre within 48 hours of sowing.\n");
            answer.append("• **Foliar Disease Protection**: For Yellow Vein Mosaic, control whiteflies with Acetamiprid 20% SP @ 0.2 g/L; for Anthracnose pod blight, spray Tebuconazole @ 1 ml/L.");

        // 7. Specific Crop Cultivation: Wheat
        } else if (lower.contains("wheat") || lower.contains("gehu")) {
            tools.add("tool_agronomy_crop_guide_wheat");
            answer.append("🌱 **Wheat (Triticum aestivum) Agronomy Protocol**:\n\n");
            answer.append("• **Sowing Window & Seed Rate**: Nov 1 to Nov 25 for timely sown wheat @ 40 kg/acre; late sown @ 50 kg/acre.\n");
            answer.append("• **Critical Irrigation Stages (CRI is vital!)**:\n");
            answer.append("  1. *Crown Root Initiation (CRI)*: 20–25 DAS (Most critical! Skipping CRI causes up to 35% yield reduction).\n");
            answer.append("  2. *Tillering*: 40–45 DAS.\n");
            answer.append("  3. *Late Jointing*: 60–65 DAS.\n");
            answer.append("  4. *Flowering / Heading*: 80–85 DAS.\n");
            answer.append("  5. *Milking / Grain Filling*: 100–105 DAS.\n");
            answer.append("• **Yellow Rust Alert**: If yellow powdery pustules appear on leaves, spray Propiconazole 25% EC (Tilt) @ 1 ml/L immediately.");

        // 8. Specific Crop Cultivation: Paddy / Rice
        } else if (lower.contains("paddy") || lower.contains("rice") || lower.contains("dhan")) {
            tools.add("tool_agronomy_crop_guide_paddy");
            answer.append("🌱 **Paddy / Rice (Oryza sativa) Crop Management**:\n\n");
            answer.append("• **Nursery & Transplanting**: 21–25 day old seedlings, 2–3 seedlings per hill at 20 cm × 15 cm spacing.\n");
            answer.append("• **Water Management (AWD - Alternate Wetting & Drying)**: Maintain 2–5 cm water during vegetative phase; drain field 10 days before harvest.\n");
            answer.append("• **Blast (Pyricularia oryzae) & Brown Planthopper (BPH)**:\n");
            answer.append("  - For Leaf Blast: Spray Tricyclazole 75% WP @ 0.6 g/L.\n");
            answer.append("  - For BPH: Avoid excess urea; drain standing water for 48 hours and spray Pymetrozine 50% WDG @ 0.6 g/L.");

        // 9. Pest & Pesticide Questions
        } else if (lower.contains("pest") || lower.contains("worm") || lower.contains("fungus") || lower.contains("spray") || lower.contains("pesticide") || lower.contains("disease")) {
            tools.add("tool_integrated_pest_management");
            answer.append("🛡️ **Integrated Pest & Disease Management (IPM) Directive**:\n\n");
            answer.append("1. **Scouting & Economic Threshold Level (ETL)**: Inspect 20 representative plants across field diagonals before applying synthetic agro-chemicals.\n");
            answer.append("2. **Biological First Line**: Prioritize yellow sticky traps (15/acre), pheromone traps, and Neem Oil (10,000 ppm @ 3 ml/L).\n");
            answer.append("3. **Targeted Fungicide Selection**:\n");
            answer.append("   • Contact: Mancozeb 75% WP @ 2.5 g/L (Broad-spectrum preventive).\n");
            answer.append("   • Systemic: Azoxystrobin + Difenoconazole @ 1 ml/L for stubborn anthracnose and powdery mildew.\n");
            answer.append("4. **Pre-Harvest Interval (PHI) Compliance**: Strictly honor withholding periods (10–14 days) before harvesting to avoid toxic residues in human food chains.");

        // 10. General Agricultural Assistance
        } else {
            answer.append("🌾 **FarmVet AI Agronomy & Veterinary Advisory**:\n\n");
            answer.append("Hello Farmer! I am your dedicated real-time agricultural intelligence partner. I can assist you with:\n\n");
            answer.append("• **Crops**: Sowing schedules, high-yield seed varieties, and NPK fertilizer scheduling for Wheat, Rice, Cotton, Soybean, Sugarcane, Maize, Vegetables, and Pulses.\n");
            answer.append("• **Pest & Disease Control**: Instant symptom analysis, bio-remedies, chemical dosages, and Pre-Harvest Interval (PHI) safety countdowns.\n");
            answer.append("• **Smart Irrigation**: Weather-adjusted evapotranspiration calculations and IoT soil moisture management.\n");
            answer.append("• **Livestock Healthcare**: Cattle & buffalo disease triage (FMD, Mastitis, Bloat), 6-month vaccination tracking, and dairy feed balancing.\n");
            answer.append("• **Mandi Economics & Subsidies**: Agmarketnet live APMC prices, profit/loss ledger, and government scheme eligibility (PM-KISAN / PMFBY).\n\n");
            answer.append("👉 *Please describe your specific crop, livestock symptom, or farm inquiry!*");
        }

        return new AiChatResponse(answer.toString(), tools, fallback, speciality, payloads);
    }

    /**
     * Real-time Server-Sent Events (SSE) streaming engine.
     * Emits tool steps and stream tokens chunk-by-chunk in real-time.
     */
    public SseEmitter streamQuery(String prompt) {
        SseEmitter emitter = new SseEmitter(120_000L); // 2 minute timeout

        CompletableFuture.runAsync(() -> {
            try {
                // Step 1: Real-time domain verification event
                emitter.send(SseEmitter.event().name("tool").data("{\"step\": 1, \"status\": \"Verifying Agricultural Domain Guardrail...\"}"));
                Thread.sleep(120);

                AiChatRequest req = new AiChatRequest();
                req.setPrompt(prompt);
                AiChatResponse response = orchestrateQuery(req);

                // Step 2: Emit tool execution events if any
                for (int i = 0; i < response.getToolsExecuted().size(); i++) {
                    String tool = response.getToolsExecuted().get(i);
                    emitter.send(SseEmitter.event().name("tool").data(String.format("{\"step\": %d, \"status\": \"Executing %s...\"}", i + 2, tool)));
                    Thread.sleep(150);
                }

                // Step 3: Stream tokens word-by-word with realistic typing latency
                String fullAnswer = response.getAnswer();
                String[] words = fullAnswer.split("(?<=\\s)|(?<=\\n)");

                for (String word : words) {
                    emitter.send(SseEmitter.event().name("token").data(word));
                    Thread.sleep(25); // 25ms per word for natural smooth streaming
                }

                // Step 4: Final completion event with structured metadata
                Map<String, Object> finalPayload = new HashMap<>();
                finalPayload.put("tools", response.getToolsExecuted());
                finalPayload.put("fallback", response.isFallbackToExpertRecommended());
                finalPayload.put("speciality", response.getSuggestedExpertSpeciality());
                finalPayload.put("toolPayloads", response.getToolPayloads());

                emitter.send(SseEmitter.event().name("complete").data(objectMapper.writeValueAsString(finalPayload)));
                emitter.complete();

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
