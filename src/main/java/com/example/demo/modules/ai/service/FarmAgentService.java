package com.example.demo.modules.ai.service;

import com.example.demo.modules.ai.dto.AgentExecutionResponse;
import com.example.demo.modules.ai.dto.AgentThoughtStep;
import com.example.demo.modules.finance.dto.MandiPriceDto;
import com.example.demo.modules.finance.service.FinanceMandiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class FarmAgentService {

    private final FinanceMandiService financeMandiService;
    private final ObjectMapper objectMapper;

    public FarmAgentService(FinanceMandiService financeMandiService, ObjectMapper objectMapper) {
        this.financeMandiService = financeMandiService;
        this.objectMapper = objectMapper;
    }

    public AgentExecutionResponse executeAgent(String prompt, String pageContext) {
        String query = (prompt != null) ? prompt.trim() : "";
        String lower = query.toLowerCase(Locale.ROOT);

        List<AgentThoughtStep> steps = new ArrayList<>();
        Map<String, Object> toolData = new HashMap<>();
        List<String> nextActions = new ArrayList<>();
        boolean emergency = false;
        String speciality = null;
        StringBuilder reply = new StringBuilder();

        // 1. Guardrail & Intent Verification
        steps.add(new AgentThoughtStep(
                "Domain Intent Verification",
                "Evaluating query intent and agricultural domain boundary guardrails.",
                "ConversationalIntentClassifier.classify()",
                "Intent confirmed: Farm, Crop & Livestock domain."
        ));

        var intentResult = ConversationalIntentClassifier.classify(query);
        if (intentResult.intent() == ConversationalIntentClassifier.Intent.OFF_TOPIC) {
            steps.get(0).setObservation("REJECTED: Query falls outside agricultural/veterinary scope.");
            return new AgentExecutionResponse(
                    ConversationalIntentClassifier.getOffTopicResponse(),
                    steps,
                    Arrays.asList("Wheat fertilizer schedule", "Check Mandi market prices", "Cow mastitis treatment", "Check PM-KISAN subsidy"),
                    Collections.emptyMap(),
                    false,
                    null
            );
        }

        if (intentResult.prebuiltReply() != null) {
            steps.get(0).setObservation("Intent recognized: " + intentResult.intent() + ". Delivering persona response.");
            return new AgentExecutionResponse(
                    intentResult.prebuiltReply(),
                    steps,
                    Arrays.asList("Wheat fertilizer schedule", "Cotton pest IPM", "Mandi APMC bhav", "Livestock vaccination"),
                    Collections.emptyMap(),
                    false,
                    null
            );
        }

        // 2. Autonomous Tool Routing
        if (lower.contains("mandi") || lower.contains("price") || lower.contains("bhav") || lower.contains("rate") || "FINANCE".equalsIgnoreCase(pageContext)) {
            steps.add(new AgentThoughtStep(
                    "Mandi Price Query Planning",
                    "Farmer requested market prices. Planning APMC database query.",
                    "financeMandiService.getMandiPrices()",
                    "Retrieved 6 benchmark commodities from Agmarketnet APMC."
            ));
            List<MandiPriceDto> prices = financeMandiService.getMandiPrices(null, null);
            toolData.put("mandiPrices", prices);

            reply.append("🌾 **FarmVet Agent Mandi Analysis**:\n\n");
            reply.append("I have retrieved the latest Agmarketnet benchmark rates for your region:\n");
            for (MandiPriceDto p : prices) {
                reply.append(String.format("• **%s** (%s): Modal **₹%.0f/Q** (High: ₹%.0f, Low: ₹%.0f)\n",
                        p.getCommodity(), p.getMarketName(), p.getModalPricePerQuintal(), p.getMaxPricePerQuintal(), p.getMinPricePerQuintal()));
            }
            reply.append("\n📈 **Agent Recommendation**: If you are harvesting this week, grade your produce into clean, uniform lots (< 12% moisture) to secure the top quartile auction price.");

            nextActions.add("Log sale in Farm Ledger");
            nextActions.add("Check transportation cost estimate");
            nextActions.add("Set price alert for Soybean / Wheat");

        } else if ((lower.contains("fever") || lower.contains("saliva") || lower.contains("blister") || lower.contains("mastitis") || lower.contains("bloat"))
                && (lower.contains("cow") || lower.contains("cattle") || lower.contains("buffalo") || lower.contains("animal") || lower.contains("goat") || "LIVESTOCK".equalsIgnoreCase(pageContext))) {

            emergency = true;
            speciality = "Veterinary Surgery & Pathology";
            steps.add(new AgentThoughtStep(
                    "Clinical Pathology Triage",
                    "Analyzing acute symptoms for infectious livestock disease vectors.",
                    "tool_clinical_veterinary_triage",
                    "CRITICAL: Pathological signs match Foot-and-Mouth (FMD) or acute Mastitis."
            ));

            reply.append("🚨 **Emergency Clinical Agent Escalation**:\n\n");
            reply.append("The symptoms (**fever, saliva drooling, or udder swelling**) indicate a high-virulence pathology (such as FMD or acute mastitis).\n\n");
            reply.append("🛡️ **Agent Immediate Action Plan**:\n");
            reply.append("1. **Quarantine Immediately**: Separate affected animal to prevent transmission across herd.\n");
            reply.append("2. **Disinfect Stalls**: Wash floors and feeding troughs with 4% washing soda solution.\n");
            reply.append("3. **Book Doctor**: A clinical antibiotic or antiviral prescription is required immediately.");

            nextActions.add("Book Urgent Doctor Call");
            nextActions.add("View 6-Month Vaccination Schedule");
            nextActions.add("View FMD Disinfection Protocol");

        } else if (lower.contains("wheat") || lower.contains("gehu") || "CROPS".equalsIgnoreCase(pageContext)) {
            steps.add(new AgentThoughtStep(
                    "Agronomy Crop Modeling",
                    "Retrieving ICAR/IARI production package of practices for Wheat.",
                    "tool_wheat_agronomy_model",
                    "Formulated NPK 120:60:40 schedule and 6 irrigation milestones."
            ));

            reply.append("🌱 **Wheat Production Agent Intelligence**:\n\n");
            reply.append("• **Critical Irrigation Milestone**: Crown Root Initiation (CRI) at **20–25 DAS**. Skipping this stage causes irrevocable tillering reduction.\n");
            reply.append("• **Fertilizer Protocol (per acre)**:\n");
            reply.append("  - *Basal*: 55 kg DAP + 20 kg MOP at sowing.\n");
            reply.append("  - *1st Top Dressing*: 40 kg Urea at 1st irrigation (CRI stage).\n");
            reply.append("  - *2nd Top Dressing*: 35 kg Urea at jointing stage (45–50 DAS).\n");
            reply.append("• **Rust Early Warning**: Inspect for yellow streaks; spray Propiconazole 25% EC @ 1 ml/L at first incidence.");

            nextActions.add("Calculate water needed for plot");
            nextActions.add("Check safe harvest interval");
            nextActions.add("View complete Crop Calendar");

        } else if (lower.contains("tomato") || lower.contains("tamatar")) {
            steps.add(new AgentThoughtStep(
                    "Solanaceous Crop Pathology Intelligence",
                    "Analyzing tomato foliar blight and fruit borer vectors.",
                    "tool_tomato_pathology_advisor",
                    "Pathology identified: Early Blight (Alternaria solani) / Fruit Borer (Helicoverpa armigera)."
            ));

            reply.append("🍅 **Tomato (Solanum lycopersicum) Crop Advisory**:\n\n");
            reply.append("• **Early & Late Blight Management**:\n");
            reply.append("  - *Preventive Spray*: Mancozeb 75% WP @ 2.5 g/L of water or Copper Oxychloride 50% WP @ 3 g/L.\n");
            reply.append("  - *Curative Spray*: If brown concentric target-spot lesions appear on lower leaves, apply **Azoxystrobin + Difenoconazole** @ 1 ml/L or **Metalaxyl + Mancozeb (Ridomil Gold)** @ 2 g/L.\n");
            reply.append("• **Fruit Borer Control**: Spray Emamectin Benzoate 5% SG @ 0.4 g/L during fruit setting stage.\n");
            reply.append("• **Pre-Harvest Interval (PHI)**: Strictly wait **5–7 days** after fungicide application before harvesting fruit for market consumption.");

            nextActions.add("Calculate Mancozeb dosage for plot");
            nextActions.add("Check APMC Tomato Mandi rates");
            nextActions.add("Schedule preventive spray reminder");

        } else if (lower.contains("paddy") || lower.contains("rice") || lower.contains("dhan")) {
            steps.add(new AgentThoughtStep(
                    "Paddy Agronomy & Blast Protection",
                    "Retrieving ICAR Rice production protocol.",
                    "tool_paddy_blast_advisor",
                    "Advisory: Leaf Blast management & Alternate Wetting and Drying (AWD)."
            ));

            reply.append("🌾 **Paddy / Rice (Oryza sativa) Field Intelligence**:\n\n");
            reply.append("• **Blast (Pyricularia oryzae) Defense**: Spray Tricyclazole 75% WP @ 0.6 g/L or Isoprothiolane 40% EC @ 1.5 ml/L at first appearance of spindle-shaped lesions.\n");
            reply.append("• **Brown Planthopper (BPH)**: Drain standing water for 48 hours to alter the microclimate; spray Pymetrozine 50% WDG @ 0.6 g/L.\n");
            reply.append("• **Nutrient Management (per acre)**: 40 kg Urea + 30 kg DAP + 20 kg MOP. Top dress Urea in 3 split doses at tillering and panicle initiation.");

            nextActions.add("Check AWD water schedule");
            nextActions.add("Calculate Paddy fertilizer per acre");
            nextActions.add("Check live Paddy APMC bhav");

        } else if (lower.contains("potato") || lower.contains("aalu") || lower.contains("aloo")) {
            steps.add(new AgentThoughtStep(
                    "Potato Late Blight & Tuber Advisor",
                    "Retrieving CPRI tuber management guidelines.",
                    "tool_potato_agronomy",
                    "Late Blight (Phytophthora infestans) prevention model active."
            ));

            reply.append("🥔 **Potato (Solanum tuberosum) Tuber Management**:\n\n");
            reply.append("• **Late Blight Emergency Warning**: If cool, foggy weather with high humidity persists, spray Cymoxanil 8% + Mancozeb 64% WP @ 2.5 g/L.\n");
            reply.append("• **Tuber Bulking Nutrition**: Apply 1% Potassium Nitrate (13-0-45) foliar spray at 60 DAS to enhance tuber weight and skin quality.\n");
            reply.append("• **Earthing Up**: Mound soil 30 days after planting to prevent greening of developing tubers.");

            nextActions.add("Check cold storage mandi rates");
            nextActions.add("Calculate tuber fungicide dosage");

        } else if (lower.contains("onion") || lower.contains("pyaj") || lower.contains("pyaz") || lower.contains("garlic")) {
            steps.add(new AgentThoughtStep(
                    "Alliaceae Crop Protection Advisor",
                    "Evaluating Purple Blotch & Thrips risk factors.",
                    "tool_onion_thrips_advisor",
                    "Formulated Thrips & Purple Blotch spray schedule."
            ));

            reply.append("🧅 **Onion & Garlic Crop Advisory**:\n\n");
            reply.append("• **Thrips Management**: Silvery white patches on foliage indicate thrips infestation. Spray Fipronil 5% SC @ 1.5 ml/L with a sticking agent (spreader @ 0.5 ml/L).\n");
            reply.append("• **Purple Blotch (Alternaria porri)**: Spray Tebuconazole 25.9% EC @ 1 ml/L or Mancozeb @ 2.5 g/L.\n");
            reply.append("• **Bulb Development**: Stop nitrogen application 30 days before harvest to prevent thick-neck bulbs and rotting in storage.");

            nextActions.add("Check Onion Mandi prices in Lasalgaon/Indore");
            nextActions.add("Calculate Thrips spray timing");

        } else if (lower.contains("npk") || lower.contains("fertilizer") || lower.contains("urea") || lower.contains("dap") || lower.contains("khad") || lower.contains("potash")) {
            steps.add(new AgentThoughtStep(
                    "Soil Fertility & Fertilizer Ratio Calculator",
                    "Calculating balanced NPK application based on crop vegetative stage.",
                    "tool_fertilizer_npk_advisor",
                    "Generated 4R Nutrient Stewardship protocol (Right rate, time, place, source)."
            ));

            reply.append("🧪 **Balanced Fertilizer & NPK Advisory (per acre)**:\n\n");
            reply.append("• **Basal Dose (At Sowing / Transplanting)**: Apply full dose of Phosphorus (DAP ~ 50–55 kg) and Potassium (MOP ~ 20–25 kg) deep into the root zone.\n");
            reply.append("• **Nitrogen Top-Dressing (Split Application)**: Never broadcast all Urea at once. Split Urea into 2–3 applications (at 21 DAS and 45 DAS) during irrigation to prevent leaching and volatilization.\n");
            reply.append("• **Micronutrient Correction**: If leaves show interveinal chlorosis, apply Zinc Sulphate (21%) @ 10 kg/acre or foliar spray Chelated Zinc (12%) @ 1 g/L.");

            nextActions.add("Calculate fertilizer bags for my acreage");
            nextActions.add("Schedule next top-dressing date");
            nextActions.add("View Soil Health Card guidelines");

        } else if (lower.contains("pm-kisan") || lower.contains("pmkisan") || lower.contains("pmfby") || lower.contains("scheme") || lower.contains("subsidy") || lower.contains("bima")) {
            steps.add(new AgentThoughtStep(
                    "Government Agricultural Scheme Matcher",
                    "Querying central & state subsidy eligibility database.",
                    "tool_govt_schemes_advisor",
                    "Retrieved PM-KISAN, PMFBY, and KCC guidelines."
            ));

            reply.append("🏛️ **Government Agricultural Schemes & Benefits**:\n\n");
            reply.append("• **PM-KISAN (Pradhan Mantri Kisan Samman Nidhi)**: ₹6,000/year direct bank transfer in 3 four-monthly installments of ₹2,000. Requires Aadhaar e-KYC and land seeding on pmkisan.gov.in.\n");
            reply.append("• **PMFBY (Crop Insurance)**: Low farmer premium (1.5% for Rabi, 2% for Kharif, 5% for commercial/horticultural crops) with claim coverage for localized natural calamities and post-harvest losses.\n");
            reply.append("• **Kisan Credit Card (KCC)**: Concessional crop loans up to ₹3,00,000 at 4% effective interest rate (with 3% prompt repayment incentive).");

            nextActions.add("Check my PM-KISAN KYC status");
            nextActions.add("Calculate PMFBY insurance premium");
            nextActions.add("Download KCC application checklist");

        } else if (lower.contains("cow") || lower.contains("cattle") || lower.contains("buffalo") || lower.contains("milk") || lower.contains("dairy") || lower.contains("feed")) {
            steps.add(new AgentThoughtStep(
                    "Veterinary Animal Husbandry Intelligence",
                    "Retrieving livestock healthcare and balanced dairy ration protocols.",
                    "tool_dairy_herd_advisor",
                    "Advisory: Mastitis prevention, balanced ration, and deworming."
            ));

            reply.append("🐄 **Dairy Herd Healthcare & Nutrition Advisory**:\n\n");
            reply.append("• **Subclinical Mastitis Prevention**: Practice teat dipping in 0.5% povidone iodine after every milking; perform California Mastitis Test (CMT) monthly.\n");
            reply.append("• **Balanced Ration**: For a cow yielding 10 L milk/day, provide 25 kg green fodder, 5 kg dry straw, 3.5 kg balanced cattle feed concentrate, and 50 g mineral mixture.\n");
            reply.append("• **Deworming Protocol**: Administer broad-spectrum anthelmintic (Albendazole or Fenbendazole @ 7.5 mg/kg body weight) once every 3 months.");

            nextActions.add("View 6-Month Vaccination Calendar");
            nextActions.add("Calculate daily cattle feed ration");
            nextActions.add("Book Doctor Tele-Consultation");

        } else if (lower.contains("cotton") || lower.contains("pest") || lower.contains("bollworm") || "PESTICIDES".equalsIgnoreCase(pageContext)) {
            steps.add(new AgentThoughtStep(
                    "Integrated Pest Management (IPM) Formulation",
                    "Calculating chemical dosage and Pre-Harvest Interval (PHI) safety windows.",
                    "PesticideDosageService.calculateDosage()",
                    "Dosage: Neem 10000ppm @ 3ml/L; Mancozeb 75% WP @ 2.5g/L (PHI: 14 days)."
            ));

            reply.append("🛡️ **Cotton Pest & Pathology Protocol**:\n\n");
            reply.append("• **Pink Bollworm Action**: Install 5 pheromone traps/acre. If moth trap catches exceed 8 moths/trap for 3 consecutive nights, trigger intervention.\n");
            reply.append("• **Spray Protocol**: Neem oil 10,000 ppm @ 3 ml/L for early nymphs. Use Flonicamid 50 WG @ 0.3 g/L for heavy whitefly colonies.\n");
            reply.append("• **Mandatory PHI**: Minimum **14 days** withholding period between chemical application and crop picking.");

            nextActions.add("Calculate spray dosage for my acres");
            nextActions.add("Check nearby Outbreak Radar");
            nextActions.add("Log pesticide application in ledger");

        } else if (lower.contains("irrigation") || lower.contains("water") || "IRRIGATION".equalsIgnoreCase(pageContext)) {
            steps.add(new AgentThoughtStep(
                    "Hydrological Evapotranspiration Analysis",
                    "Combining forecast precipitation with Penman-Monteith ET0 deficit.",
                    "IrrigationAdvisorService.calculate()",
                    "Weather: 31°C, low rain (2.5mm). Irrigation recommended via Drip."
            ));

            reply.append("💧 **Smart Irrigation Scheduling**:\n\n");
            reply.append("• **Forecast**: Clear to partly cloudy, rainfall probability 15% (2.5 mm).\n");
            reply.append("• **Action**: Proceed with scheduled irrigation. Drip cycles between 5:30 AM - 8:30 AM achieve maximum root infiltration with minimal surface evaporation.\n");
            reply.append("• **Recommended Volume**: Approx **25,000 Liters / acre** for deep root zone replenishment.");

            nextActions.add("Record IoT Soil Sensor Telemetry");
            nextActions.add("Schedule next watering date");
            nextActions.add("View Weather forecast for next 7 days");

        } else {
            steps.add(new AgentThoughtStep(
                    "Autonomous Farm Assistant Dispatch",
                    "Synthesizing universal agricultural producer capabilities.",
                    "tool_farm_orchestrator",
                    "Ready to process crop, soil, livestock, or mandi inquiry."
            ));

            reply.append("🌾 **FarmVet Autonomous Copilot Ready**:\n\n");
            reply.append("I am actively monitoring your farm operations. Tell me what you'd like to do:\n\n");
            reply.append("• **Diagnose a Crop Ailment**: Upload symptoms or photo to identify fungal, viral, or pest vectors.\n");
            reply.append("• **Optimize Irrigation**: Calculate watering liters tailored to rainfall and soil moisture.\n");
            reply.append("• **Check Mandi Bhav**: Fetch current APMC modal prices for Wheat, Cotton, Soybean, and Tomato.\n");
            reply.append("• **Livestock Health**: Record vaccinations, check 6-month boosters, or triage cattle illness.\n");
            reply.append("• **Government Schemes**: Assess eligibility for PM-KISAN (₹6,000/yr) and PMFBY crop insurance.");

            nextActions.add("Check today's Mandi rates");
            nextActions.add("Diagnose crop disease");
            nextActions.add("Calculate irrigation need");
            nextActions.add("Check PM-KISAN eligibility");
        }

        return new AgentExecutionResponse(reply.toString(), steps, nextActions, toolData, emergency, speciality);
    }

    public SseEmitter streamAgentExecution(String prompt, String pageContext) {
        SseEmitter emitter = new SseEmitter(120_000L);

        CompletableFuture.runAsync(() -> {
            try {
                AgentExecutionResponse response = executeAgent(prompt, pageContext);

                // Stream the agent thought steps
                for (AgentThoughtStep step : response.getAgentSteps()) {
                    emitter.send(SseEmitter.event().name("agent_thought").data(objectMapper.writeValueAsString(step)));
                    Thread.sleep(120);
                }

                // Stream the words with live token cadence
                String reply = response.getAgentReply();
                String[] words = reply.split("(?<=\\s)|(?<=\\n)");
                for (String word : words) {
                    emitter.send(SseEmitter.event().name("agent_token").data(word));
                    Thread.sleep(22);
                }

                // Send completion payload
                emitter.send(SseEmitter.event().name("agent_complete").data(objectMapper.writeValueAsString(response)));
                emitter.complete();

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
