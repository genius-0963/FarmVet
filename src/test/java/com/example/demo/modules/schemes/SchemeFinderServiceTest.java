package com.example.demo.modules.schemes;

import com.example.demo.modules.schemes.entity.GovtScheme;
import com.example.demo.modules.schemes.repository.GovtSchemeRepository;
import com.example.demo.modules.schemes.service.SchemeFinderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SchemeFinderServiceTest {

    @Test
    void testEligibilityEvaluation() {
        GovtSchemeRepository mockRepo = Mockito.mock(GovtSchemeRepository.class);
        GovtScheme pmKisan = new GovtScheme();
        pmKisan.setSchemeName("PM-KISAN");
        pmKisan.setMinLandAcres(0.1);
        pmKisan.setMaxLandAcres(25.0);
        pmKisan.setTargetState("ALL");
        pmKisan.setBeneficiaryType("ALL");

        when(mockRepo.findAll()).thenReturn(List.of(pmKisan));
        when(mockRepo.count()).thenReturn(1L);

        SchemeFinderService service = new SchemeFinderService(mockRepo);
        List<Map<String, Object>> evaluations = service.checkEligibility(3.5, "Maharashtra", "ALL");

        assertEquals(1, evaluations.size());
        assertTrue((Boolean) evaluations.get(0).get("isEligible"));
        assertEquals("HIGHLY_RECOMMENDED", evaluations.get(0).get("status"));
    }
}
