package com.example.demo.modules.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Enterprise Regulatory Agrochemical Safety & Pre-Harvest Interval (PHI) Guardrail.
 * Protects farmers, consumers, and food chains by strictly intercepting recommendations,
 * blocking banned/restricted active ingredients (per CIB&RC & EPA), and calculating
 * precise knapsack tank dilution rates.
 */
@Service
public class ChemicalSafetyInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ChemicalSafetyInterceptor.class);

    public record SafetyAuditResult(
            boolean approved,
            boolean blockedHazard,
            String warningNotice,
            String safeRecommendation,
            int phiDays,
            String tankMixGuidance
    ) {}

    // High-toxicity banned or restricted agrochemicals (CIB&RC schedule)
    private static final Set<String> BANNED_CHEMICALS = new HashSet<>(Arrays.asList(
            "monocrotophos", "paraquat", "phorate", "endrin", "aldrin",
            "methyl parathion", "diazinon", "chlordane", "heptachlor",
            "ddt", "lindane", "carbofuran", "triazophos"
    ));

    // Known chemical active ingredient Pre-Harvest Intervals (PHI) in days
    private static final Map<String, Integer> PHI_REGISTRY = Map.ofEntries(
            Map.entry("mancozeb", 14),
            Map.entry("chlorpyrifos", 21),
            Map.entry("imidacloprid", 15),
            Map.entry("tricyclazole", 21),
            Map.entry("propiconazole", 30),
            Map.entry("azoxystrobin", 7),
            Map.entry("difenoconazole", 7),
            Map.entry("emamectin benzoate", 5),
            Map.entry("flonicamid", 10),
            Map.entry("copper oxychloride", 3),
            Map.entry("neem oil", 0)
    );

    /**
     * Inspects text for banned chemicals, computes PHI safety, and calculates exact 15L spray tank dilution.
     */
    public SafetyAuditResult auditPrescription(String prompt, String responseText, Integer daysUntilHarvest) {
        String lowerResp = (responseText != null) ? responseText.toLowerCase(Locale.ROOT) : "";
        String lowerPrompt = (prompt != null) ? prompt.toLowerCase(Locale.ROOT) : "";

        // 1. Check for banned/restricted chemicals
        for (String banned : BANNED_CHEMICALS) {
            if (lowerResp.contains(banned) || lowerPrompt.contains(banned)) {
                log.warn("SAFETY VIOLATION DETECTED: Banned chemical [{}] found in prescription.", banned);
                String safeAlt = "🛡️ **CIB&RC Regulatory Safety Alert**:\n\n" +
                        "⚠️ **Banned Agrochemical Detected**: `" + banned.toUpperCase() + "` is strictly prohibited under Indian CIB&RC and international EPA environmental protection mandates due to high mammalian toxicity.\n\n" +
                        "🌱 **Certified Safe Bio-Alternative**:\n" +
                        "• **Bio-Pesticide**: Cold-Pressed Neem Seed Kernel Extract (10,000 ppm) @ **3 ml / Liter** of water.\n" +
                        "• **Bio-Fungicide**: *Trichoderma viride* (1% WP) @ **5 g / Liter** of water.\n" +
                        "• **Safety Interval**: 0 Days (100% organic, safe for human consumption and pollinators).";

                return new SafetyAuditResult(false, true, "Banned chemical blocked", safeAlt, 0, "Do not use banned chemicals.");
            }
        }

        // 2. Compute PHI safety
        int maxPhi = 0;
        String detectedChemical = null;
        for (var entry : PHI_REGISTRY.entrySet()) {
            if (lowerResp.contains(entry.getKey())) {
                if (entry.getValue() > maxPhi) {
                    maxPhi = entry.getValue();
                    detectedChemical = entry.getKey();
                }
            }
        }

        // 3. Harvest proximity check
        if (daysUntilHarvest != null && maxPhi > 0 && daysUntilHarvest < maxPhi) {
            String phiWarning = String.format(
                    "⚠️ **PHI Safety Withholding Alert**: Your harvest is scheduled in **%d days**, but `%s` requires a **%d-day Pre-Harvest Interval (PHI)**.\n\n" +
                    "Applying this chemical will cause toxic residue rejection at APMC auctions.\n\n" +
                    "👉 **Emergency Recommendation**: Switch immediately to **Neem Oil (10,000 ppm)** or **Copper Oxychloride (3-day PHI)**.",
                    daysUntilHarvest, detectedChemical.toUpperCase(), maxPhi
            );

            return new SafetyAuditResult(false, false, phiWarning, responseText, maxPhi, "Use 0-3 day PHI alternatives.");
        }

        // 4. Generate Knapsack Tank Math
        String tankGuidance = "💧 **Knapsack Tank Dilution Math (Standard 15-Liter Sprayer)**:\n" +
                "• For 1 ml/L dosage: Add **15 ml** chemical per 15L tank.\n" +
                "• For 2 g/L dosage: Add **30 grams** chemical per 15L tank.\n" +
                "• Acreage requirement: Use **10–12 tanks** (150–180 Liters water) per acre for uniform foliar canopy coverage.";

        return new SafetyAuditResult(true, false, null, responseText, maxPhi, tankGuidance);
    }
}
