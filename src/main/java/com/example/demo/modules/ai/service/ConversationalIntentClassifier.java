package com.example.demo.modules.ai.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class ConversationalIntentClassifier {

    public enum Intent {
        GREETING,
        CAPABILITY,
        COURTESY,
        OFF_TOPIC,
        AGRI_QUERY
    }

    public record IntentResult(Intent intent, String prebuiltReply) {}

    // Multi-lingual & colloquial greeting patterns (English, Hindi, Hinglish, Regional)
    private static final Pattern GREETING_PATTERN = Pattern.compile(
            "^(h+i+|h+e+l+o+|h+e+y+|h+a+l+o+|namaste|namaskar|pranam|pranaam|ram\\s*ram|radhe\\s*radhe|jai\\s*shree\\s*krishna|jai\\s*kisan|shubh\\s*prabhat|good\\s*morning|good\\s*evening|good\\s*afternoon|good\\s*day|kem\\s*cho|sat\\s*sri\\s*akal|kisaan\\s*bhai|aap\\s*kaise\\s*ho|how\\s*are\\s*you|kya\\s*haal\\s*hai|salaam|adaab)[\\s\\!\\?\\.\\,\\-]*$",
            Pattern.CASE_INSENSITIVE
    );

    // Capability / Identity patterns
    private static final Pattern CAPABILITY_PATTERN = Pattern.compile(
            "^(who\\s*are\\s*you|what\\s*can\\s*you\\s*do|what\\s*is\\s*your\\s*name|what\\s*do\\s*you\\s*do|how\\s*can\\s*you\\s*help|tell\\s*me\\s*about\\s*yourself|help|help\\s*me|kya\\s*kar\\s*sakte\\s*ho|tum\\s*kaun\\s*ho|aap\\s*kaun\\s*hai)[\\s\\!\\?\\.]*$",
            Pattern.CASE_INSENSITIVE
    );

    // Courtesy / Acknowledgment patterns
    private static final Pattern COURTESY_PATTERN = Pattern.compile(
            "^(thanks|thank\\s*you|dhanyawad|dhanyavaad|shukriya|great|ok|okay|theek\\s*hai|accha|understood|got\\s*it|sure|yes|no|bye|goodbye|alvida)[\\s\\!\\?\\.]*$",
            Pattern.CASE_INSENSITIVE
    );

    // Off-topic indicators (programming, crypto, gaming, pop culture, politics)
    private static final String[] OFF_TOPIC_TRIGGERS = {
            "write code", "javascript", "python script", "java class", "html css", "algorithm", "leetcode",
            "cryptocurrency", "bitcoin", "ethereum", "blockchain", "forex trading",
            "movie review", "hollywood", "bollywood", "celebrity gossip", "pop music", "lyrics",
            "video game", "playstation", "xbox", "gta 5", "pubg", "free fire",
            "presidential election", "democrat republican", "foreign policy war",
            "car engine cylinder repair", "football cricket match score",
            "write an essay on love", "dating relationship advice"
    };

    // Core Agriculture & Veterinary vocabulary
    private static final Set<String> AGRI_VOCABULARY = new HashSet<>(Arrays.asList(
            // Soil & Farm
            "farm", "farmer", "farming", "crop", "crops", "field", "plot", "acre", "acres", "soil",
            "kisan", "kheti", "krishi", "land", "sowing", "seed", "seeds", "harvest", "harvesting",
            "yield", "tillage", "plow", "plough", "loam", "clay", "sand", "organic", "manure", "compost",
            "npk", "urea", "dap", "potash", "fertilizer", "fertilizers", "khad", "ph", "mulching",

            // Crops
            "wheat", "paddy", "rice", "cotton", "soybean", "soya", "sugarcane", "maize", "corn",
            "tomato", "onion", "potato", "mustard", "sarson", "groundnut", "peanut", "gram", "chana",
            "millet", "bajra", "jowar", "ragi", "tur", "arhar", "moong", "urad", "chilli", "mirch",
            "garlic", "ginger", "turmeric", "haldi", "mango", "banana", "grape", "grapes", "pomegranate",
            "citrus", "orange", "lemon", "apple", "papaya", "tea", "coffee", "pulses", "vegetable", "vegetables",

            // Water & Climate
            "irrigation", "water", "watering", "drip", "sprinkler", "flood", "rain", "rainfall", "weather",
            "climate", "drought", "moisture", "evapotranspiration", "humidity", "temperature", "borewell",

            // Pests & Pathology
            "pest", "pests", "insect", "insects", "worm", "caterpillar", "armyworm", "bollworm", "aphid",
            "aphids", "whitefly", "whiteflies", "thrips", "mite", "mites", "disease", "diseases", "fungus",
            "fungal", "blight", "rust", "mildew", "wilt", "rot", "mosaic", "leaf", "leaves", "stem",
            "root", "pod", "flower", "fruit", "pesticide", "pesticides", "fungicide", "fungicides", "herbicide",
            "insecticide", "spray", "spraying", "dosage", "phi", "pre-harvest", "neem", "mancozeb", "imidacloprid",
            "chlorpyrifos", "propiconazole",

            // Market & Government
            "mandi", "apmc", "bhav", "price", "prices", "rate", "rates", "market", "quintal", "msp",
            "agmarket", "cost", "expense", "revenue", "profit", "loss", "sale", "subsidy", "subsidies",
            "pm-kisan", "pmkisan", "pmfby", "fasal bima", "kcc", "kisan credit card", "scheme", "schemes",

            // Livestock & Vet Care
            "animal", "animals", "livestock", "cattle", "cow", "cows", "buffalo", "buffaloes", "bull", "ox",
            "calf", "calves", "goat", "goats", "sheep", "poultry", "chicken", "hen", "dairy", "milk",
            "lactation", "lactating", "udder", "mastitis", "fmd", "foot and mouth", "bloat", "fever",
            "saliva", "drool", "vaccine", "vaccines", "vaccination", "vet", "veterinary", "veterinarian",
            "deworming", "fodder", "silage", "feed"
    ));

    public static IntentResult classify(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return new IntentResult(Intent.GREETING, getGreetingResponse());
        }

        String cleaned = prompt.trim();
        String lower = cleaned.toLowerCase(Locale.ROOT);

        // 1. Check Greetings
        if (GREETING_PATTERN.matcher(cleaned).matches()) {
            return new IntentResult(Intent.GREETING, getGreetingResponse());
        }

        // 2. Check Capability / Identity
        if (CAPABILITY_PATTERN.matcher(cleaned).matches()) {
            return new IntentResult(Intent.CAPABILITY, getCapabilityResponse());
        }

        // 3. Check Courtesy / Acknowledgment
        if (COURTESY_PATTERN.matcher(cleaned).matches()) {
            return new IntentResult(Intent.COURTESY, getCourtesyResponse());
        }

        // 4. Check Explicit Off-Topic Triggers
        for (String trigger : OFF_TOPIC_TRIGGERS) {
            if (lower.contains(trigger)) {
                return new IntentResult(Intent.OFF_TOPIC, getOffTopicResponse());
            }
        }

        // 5. Check Agricultural Vocabulary
        String[] words = lower.split("[\\s,\\.\\?\\!\\;\\:\\-\\(\\)\\[\\]\"']+");
        for (String word : words) {
            if (AGRI_VOCABULARY.contains(word)) {
                return new IntentResult(Intent.AGRI_QUERY, null);
            }
        }

        // 6. Conversational fallback if query is very short or ambiguous
        if (cleaned.length() <= 15) {
            return new IntentResult(Intent.GREETING, getGreetingResponse());
        }

        // Default: If it's a longer query without forbidden off-topic words, let the agronomy engine handle it
        return new IntentResult(Intent.AGRI_QUERY, null);
    }

    public static String getGreetingResponse() {
        return "🌾 **Namaste Farmer! Welcome to FarmVet Copilot.**\n\n" +
                "I am your dedicated **AI Agronomist & Veterinary Care Specialist**, available 24/7 to support your farm and livestock.\n\n" +
                "**Here is how I can assist you today:**\n" +
                "• **Crop Nutrition & Fertilizer**: Calculate exact basal & top-dressing NPK dosages for Wheat, Paddy, Soybean, Cotton, and more.\n" +
                "• **Disease & Pest Diagnosis**: Identify leaf blights, rusts, bollworms, and safe pesticide spray schedules with Pre-Harvest Intervals (PHI).\n" +
                "• **Smart Irrigation**: Schedule watering based on current rainfall and soil moisture deficit.\n" +
                "• **Livestock Healthcare**: Triage cattle & buffalo fever, mastitis, FMD symptoms, and 6-month vaccination schedules.\n" +
                "• **Mandi Bhav & Subsidies**: Get live APMC market prices and check PM-KISAN or PMFBY crop insurance eligibility.\n\n" +
                "**What crop are you currently cultivating, or how is your herd doing?**";
    }

    public static String getCapabilityResponse() {
        return "🌱 **About FarmVet Copilot:**\n\n" +
                "I am an enterprise agricultural copilot engineered specifically for farmers, agronomists, and veterinarians.\n\n" +
                "**Key Capabilities:**\n" +
                "1. **Agro-Pathology Intelligence**: Rapid identification of fungal, bacterial, and insect crop damage.\n" +
                "2. **Chemical & Organic Formulations**: Balanced dosage per acre with chemical brand names, organic neem alternatives, and safety countdown days.\n" +
                "3. **Veterinary Clinical Triage**: Ear-tag animal tracking, symptom analysis, and instant escalation to tele-consultations for severe clinical illness.\n" +
                "4. **Financial Ledger & Mandi Analytics**: Agmarketnet benchmark rates, crop production cost accounting, and profit margins.\n\n" +
                "Ask me any question about your crops, soil, pests, or animals to get started!";
    }

    public static String getCourtesyResponse() {
        return "🌾 **You're very welcome!**\n\n" +
                "I am always right here on your screen whenever you need crop advice, pesticide dosage calculations, or livestock health support.\n\n" +
                "Wishing you healthy crops and a bountiful harvest! 🌱";
    }

    public static String getOffTopicResponse() {
        return "🌾 **FarmVet Domain Specialization Notice**\n\n" +
                "I am built exclusively as an **AI Agricultural & Veterinary Copilot** dedicated 100% to farmers, crop management, livestock healthcare, and farm economics.\n\n" +
                "I cannot assist with queries outside farming (such as general computer coding, entertainment, video games, or politics).\n\n" +
                "**Please ask me anything regarding:**\n" +
                "• **Crops**: Sowing calendars, seed varieties, NPK fertilizer dosages\n" +
                "• **Crop Protection**: Pest identification, fungicide remedies, PHI spray intervals\n" +
                "• **Irrigation & Soil**: Weather-based watering requirements, soil fertility\n" +
                "• **Livestock Healthcare**: Cattle & buffalo illness triage, vaccination schedules\n" +
                "• **Market Economics**: Mandi APMC rates, PM-KISAN government schemes";
    }
}
