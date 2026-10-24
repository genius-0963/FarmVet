package com.example.demo.modules.schemes.service;

import com.example.demo.modules.schemes.entity.GovtScheme;
import com.example.demo.modules.schemes.repository.GovtSchemeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SchemeFinderService {

    private final GovtSchemeRepository repository;

    public SchemeFinderService(GovtSchemeRepository repository) {
        this.repository = repository;
        initSampleSchemesIfEmpty();
    }

    private void initSampleSchemesIfEmpty() {
        if (repository.count() == 0) {
            GovtScheme s1 = new GovtScheme();
            s1.setSchemeName("PM-KISAN (Pradhan Mantri Kisan Samman Nidhi)");
            s1.setMinistry("Ministry of Agriculture & Farmers Welfare");
            s1.setDescription("Direct income support of Rs. 6,000 per year in 3 equal installments for all landholding farmer families.");
            s1.setMinLandAcres(0.01);
            s1.setMaxLandAcres(50.0);
            s1.setTargetState("ALL");
            s1.setBeneficiaryType("ALL");
            s1.setBenefitSummary("₹6,000/year directly transferred to Aadhaar-seeded bank account.");
            s1.setApplicationGuidance("Apply online on pmkisan.gov.in with Aadhaar card, land record (7/12 extract or Jamabandi), and active bank passbook.");
            s1.setOfficialPortalUrl("https://pmkisan.gov.in");
            repository.save(s1);

            GovtScheme s2 = new GovtScheme();
            s2.setSchemeName("Pradhan Mantri Krishi Sinchayee Yojana (Per Drop More Crop)");
            s2.setMinistry("Department of Agriculture & Farmers Welfare");
            s2.setDescription("Financial subsidy of up to 55% for small/marginal farmers and 45% for other farmers to install micro-irrigation (Drip & Sprinkler).");
            s2.setMinLandAcres(0.5);
            s2.setMaxLandAcres(12.5);
            s2.setTargetState("ALL");
            s2.setBeneficiaryType("SMALL_AND_MARGINAL");
            s2.setBenefitSummary("Up to 55% capital subsidy on Drip and Sprinkler irrigation systems.");
            s2.setApplicationGuidance("Submit farm map, soil test report, water source proof, and land ownership documents to District Agriculture Officer.");
            s2.setOfficialPortalUrl("https://pmksy.gov.in");
            repository.save(s2);

            GovtScheme s3 = new GovtScheme();
            s3.setSchemeName("Pradhan Mantri Fasal Bima Yojana (PMFBY)");
            s3.setMinistry("Ministry of Agriculture");
            s3.setDescription("Comprehensive crop insurance against non-preventable natural risks from pre-sowing to post-harvest.");
            s3.setMinLandAcres(0.1);
            s3.setMaxLandAcres(100.0);
            s3.setTargetState("ALL");
            s3.setBeneficiaryType("ALL");
            s3.setBenefitSummary("Nominal premium: 2% for Kharif crops, 1.5% for Rabi food/oilseeds, and 5% for commercial/horticultural crops.");
            s3.setApplicationGuidance("Enroll via Common Service Centers (CSC) or portal before cut-off date with sowing certificate and land ownership deed.");
            s3.setOfficialPortalUrl("https://pmfby.gov.in");
            repository.save(s3);
        }
    }

    public List<GovtScheme> getAllSchemes() {
        return repository.findAll();
    }

    public List<Map<String, Object>> checkEligibility(double landAcres, String state, String beneficiaryCategory) {
        List<GovtScheme> allSchemes = repository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (GovtScheme scheme : allSchemes) {
            boolean landEligible = true;
            if (scheme.getMinLandAcres() != null && landAcres < scheme.getMinLandAcres()) {
                landEligible = false;
            }
            if (scheme.getMaxLandAcres() != null && landAcres > scheme.getMaxLandAcres()) {
                landEligible = false;
            }

            boolean stateEligible = "ALL".equalsIgnoreCase(scheme.getTargetState())
                    || (state != null && scheme.getTargetState().equalsIgnoreCase(state.trim()));

            boolean categoryEligible = "ALL".equalsIgnoreCase(scheme.getBeneficiaryType())
                    || (beneficiaryCategory != null && scheme.getBeneficiaryType().equalsIgnoreCase(beneficiaryCategory.trim()));

            boolean isEligible = landEligible && stateEligible && categoryEligible;

            Map<String, Object> eval = new HashMap<>();
            eval.put("scheme", scheme);
            eval.put("isEligible", isEligible);
            if (isEligible) {
                eval.put("status", "HIGHLY_RECOMMENDED");
                eval.put("matchReason", String.format("Matches land size (%.1f acres) and regional jurisdiction.", landAcres));
            } else {
                eval.put("status", "INELIGIBLE");
                eval.put("matchReason", "Does not satisfy land ceiling or targeted beneficiary classification.");
            }
            result.add(eval);
        }

        return result;
    }
}
