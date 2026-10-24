package com.example.demo.modules.pesticide;

import com.example.demo.modules.pesticide.dto.DosageCalculationDto;
import com.example.demo.modules.pesticide.repository.PesticideApplicationRepository;
import com.example.demo.modules.pesticide.service.PesticideDosageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class DosageCalculationTest {

    private PesticideDosageService dosageService;

    @BeforeEach
    void setUp() {
        PesticideApplicationRepository repository = Mockito.mock(PesticideApplicationRepository.class);
        dosageService = new PesticideDosageService(repository);
    }

    @Test
    void testMancozebDosageAndPhi() {
        DosageCalculationDto calculation = dosageService.calculateDosage("Mancozeb", 3.0);
        assertNotNull(calculation);
        assertEquals(1800.0, calculation.getTotalRequired());
        assertEquals(14, calculation.getPhiDays());
        assertEquals("Mancozeb 75% WP", calculation.getActiveIngredient());
    }

    @Test
    void testNeemOilDosageAndPhi() {
        DosageCalculationDto calculation = dosageService.calculateDosage("Neem Oil", 2.5);
        assertNotNull(calculation);
        assertEquals(2500.0, calculation.getTotalRequired());
        assertEquals(3, calculation.getPhiDays());
    }
}
