package com.example.demo.modules.ai.service;

public class AgriDomainGuardrail {

    /**
     * Checks if a query is accepted by the conversational router (Agri query or valid conversational intent).
     * Returns false ONLY when query is genuinely off-topic.
     */
    public static boolean isAgriculturalQuery(String prompt) {
        var result = ConversationalIntentClassifier.classify(prompt);
        return result.intent() != ConversationalIntentClassifier.Intent.OFF_TOPIC;
    }

    public static String getGuardrailRefusalMessage() {
        return ConversationalIntentClassifier.getOffTopicResponse();
    }
}
