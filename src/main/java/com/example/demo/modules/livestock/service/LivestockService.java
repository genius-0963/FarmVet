package com.example.demo.modules.livestock.service;

import com.example.demo.common.exception.ResourceNotFoundException;
import com.example.demo.modules.livestock.entity.LivestockProfile;
import com.example.demo.modules.livestock.entity.LivestockVaccination;
import com.example.demo.modules.livestock.repository.LivestockProfileRepository;
import com.example.demo.modules.livestock.repository.LivestockVaccinationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class LivestockService {

    private final LivestockProfileRepository profileRepository;
    private final LivestockVaccinationRepository vaccinationRepository;

    public LivestockService(LivestockProfileRepository profileRepository, LivestockVaccinationRepository vaccinationRepository) {
        this.profileRepository = profileRepository;
        this.vaccinationRepository = vaccinationRepository;
    }

    public LivestockProfile registerAnimal(LivestockProfile profile) {
        if (profile.getTagId() == null || profile.getTagId().trim().isEmpty()) {
            throw new IllegalArgumentException("Animal Tag ID is required");
        }
        return profileRepository.save(profile);
    }

    public List<LivestockProfile> getAnimalsByFarmer(Long farmerId) {
        return profileRepository.findByFarmerId(farmerId);
    }

    public LivestockProfile getAnimalById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livestock animal not found with id: " + id));
    }

    public LivestockVaccination recordVaccination(LivestockVaccination record) {
        if (record.getAdministeredDate() == null) {
            record.setAdministeredDate(LocalDate.now());
        }
        if (record.getNextDueDate() == null) {
            // Default 6 months / 180 days booster cycle for common livestock vaccines (FMD / HS)
            record.setNextDueDate(record.getAdministeredDate().plusMonths(6));
        }
        return vaccinationRepository.save(record);
    }

    public List<LivestockVaccination> getVaccinations(Long animalId) {
        return vaccinationRepository.findByLivestockIdOrderByAdministeredDateDesc(animalId);
    }

    public Map<String, Object> diagnoseLivestockSymptoms(String species, String symptoms) {
        Map<String, Object> diagnosis = new HashMap<>();
        String s = (symptoms != null) ? symptoms.toLowerCase() : "";

        if (s.contains("fever") && (s.contains("blister") || s.contains("saliva") || s.contains("drool") || s.contains("limp"))) {
            diagnosis.put("probableDisease", "Foot and Mouth Disease (FMD)");
            diagnosis.put("urgency", "CRITICAL");
            diagnosis.put("actionPlan", "Quarantine immediately! Disinfect sheds with 4% sodium carbonate. Report to local veterinary officer within 24 hours.");
            diagnosis.put("supportiveCare", "Potassium permanganate wash (1:1000) for mouth lesions, soft feed (gruel).");
        } else if (s.contains("swollen udder") || s.contains("milk clot") || s.contains("hard udder")) {
            diagnosis.put("probableDisease", "Bovine Mastitis");
            diagnosis.put("urgency", "HIGH");
            diagnosis.put("actionPlan", "Collect sterile milk sample for culture. Administer intramammary antibiotic infusions under veterinary supervision.");
            diagnosis.put("supportiveCare", "Frequent hand-stripping of infected quarter, teat dip in chlorhexidine/iodophor.");
        } else if (s.contains("bloat") || s.contains("swollen flank") || s.contains("belly")) {
            diagnosis.put("probableDisease", "Acute Ruminal Tympany (Bloat)");
            diagnosis.put("urgency", "EMERGENCY");
            diagnosis.put("actionPlan", "Administer oral antifoaming agent (Simethicone or vegetable oil 200-300 ml). Walk the animal gently.");
            diagnosis.put("supportiveCare", "Keep animal head elevated; if asphyxiation threatens, vet must use trocar and cannula.");
        } else {
            diagnosis.put("probableDisease", "General Systemic Stress / Indigestion");
            diagnosis.put("urgency", "MODERATE");
            diagnosis.put("actionPlan", "Isolate in dry sheltered stall, provide clean water and fresh green fodder.");
            diagnosis.put("supportiveCare", "Monitor rectal temperature twice daily. Book veterinarian consultation if fever persists.");
        }

        return diagnosis;
    }
}
