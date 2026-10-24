package com.example.demo.modules.pesticide.service;

import com.example.demo.modules.pesticide.dto.DosageCalculationDto;
import com.example.demo.modules.pesticide.entity.PesticideApplication;
import com.example.demo.modules.pesticide.repository.PesticideApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PesticideDosageService {

    private final PesticideApplicationRepository repository;

    public PesticideDosageService(PesticideApplicationRepository repository) {
        this.repository = repository;
    }

    public DosageCalculationDto calculateDosage(String pesticideName, Double acres) {
        double safeAcres = (acres != null && acres > 0) ? acres : 1.0;
        String pName = (pesticideName != null) ? pesticideName.trim() : "Broad-Spectrum";

        double dosagePerAcre;
        String activeIngredient;
        int phiDays;
        String unit;

        switch (pName.toLowerCase()) {
            case "mancozeb":
                activeIngredient = "Mancozeb 75% WP";
                dosagePerAcre = 600.0; // grams
                unit = "grams";
                phiDays = 14;
                break;
            case "chlorpyrifos":
                activeIngredient = "Chlorpyrifos 20% EC";
                dosagePerAcre = 500.0; // ml
                unit = "ml";
                phiDays = 21;
                break;
            case "neem oil":
                activeIngredient = "Azadirachtin 10,000 ppm";
                dosagePerAcre = 1000.0; // ml
                unit = "ml";
                phiDays = 3;
                break;
            default:
                activeIngredient = "Imidacloprid 17.8% SL";
                dosagePerAcre = 150.0; // ml
                unit = "ml";
                phiDays = 10;
                break;
        }

        double totalRequired = dosagePerAcre * safeAcres;
        String instructions = String.format("Dilute in %d Liters of water (approx 150-200 L/acre). Wear PPE, face mask, and chemical-resistant gloves. Do not harvest until %d days have elapsed.",
                (int)(safeAcres * 180), phiDays);

        return new DosageCalculationDto(pName, activeIngredient, safeAcres, dosagePerAcre, unit, totalRequired, phiDays, instructions);
    }

    public PesticideApplication logApplication(PesticideApplication application) {
        if (application.getApplicationDate() == null) {
            application.setApplicationDate(LocalDate.now());
        }

        DosageCalculationDto dosage = calculateDosage(application.getPesticideName(), application.getAcres());
        application.setActiveIngredient(dosage.getActiveIngredient());
        application.setDosagePerAcre(dosage.getDosagePerAcre());
        application.setTotalVolumeCalculated(dosage.getTotalRequired());
        application.setPreHarvestIntervalDays(dosage.getPhiDays());
        application.setSafeHarvestDate(application.getApplicationDate().plusDays(dosage.getPhiDays()));
        application.setReapplicationIntervalDays(14);
        application.setNextRecommendedSprayDate(application.getApplicationDate().plusDays(14));
        application.setSafetyWarning(dosage.getSafetyInstructions());
        application.setStatus("LOGGED");

        return repository.save(application);
    }

    public List<PesticideApplication> getFarmerHistory(Long farmerId) {
        return repository.findByFarmerIdOrderByApplicationDateDesc(farmerId);
    }
}
