package com.example.demo.modules.outbreak.service;

import com.example.demo.modules.notification.service.NotificationService;
import com.example.demo.modules.outbreak.entity.OutbreakIncident;
import com.example.demo.modules.outbreak.repository.OutbreakIncidentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OutbreakRadarService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private final OutbreakIncidentRepository repository;
    private final NotificationService notificationService;

    public OutbreakRadarService(OutbreakIncidentRepository repository, NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
        initSampleIncidentsIfEmpty();
    }

    private void initSampleIncidentsIfEmpty() {
        if (repository.count() == 0) {
            OutbreakIncident i1 = new OutbreakIncident();
            i1.setDiseaseName("Fall Armyworm (Spodoptera frugiperda)");
            i1.setAffectedHost("Maize / Corn");
            i1.setLatitude(18.5204);
            i1.setLongitude(73.8567);
            i1.setDistrict("Pune");
            i1.setState("Maharashtra");
            i1.setRadiusKm(30.0);
            i1.setSeverity("HIGH");
            i1.setReportCount(12);
            i1.setIsVerified(true);
            i1.setReportedDate(LocalDate.now().minusDays(2));
            repository.save(i1);

            OutbreakIncident i2 = new OutbreakIncident();
            i2.setDiseaseName("Lumpy Skin Disease (LSD)");
            i2.setAffectedHost("Cattle / Buffalo");
            i2.setLatitude(23.0225);
            i2.setLongitude(72.5714);
            i2.setDistrict("Ahmedabad");
            i2.setState("Gujarat");
            i2.setRadiusKm(45.0);
            i2.setSeverity("EPIDEMIC");
            i2.setReportCount(28);
            i2.setIsVerified(true);
            i2.setReportedDate(LocalDate.now().minusDays(1));
            repository.save(i2);
        }
    }

    public List<OutbreakIncident> getAllActiveOutbreaks() {
        return repository.findByIsVerifiedTrueOrderByReportedDateDesc();
    }

    public List<OutbreakIncident> findNearbyOutbreaks(double userLat, double userLon, double searchRadiusKm) {
        List<OutbreakIncident> all = repository.findByIsVerifiedTrueOrderByReportedDateDesc();
        List<OutbreakIncident> nearby = new ArrayList<>();

        for (OutbreakIncident incident : all) {
            double dist = calculateHaversineDistanceKm(userLat, userLon, incident.getLatitude(), incident.getLongitude());
            if (dist <= searchRadiusKm) {
                nearby.add(incident);
            }
        }
        return nearby;
    }

    public OutbreakIncident reportCommunityOutbreak(OutbreakIncident report, Long reportingFarmerId) {
        report.setReportedDate(LocalDate.now());
        report.setIsVerified(true); // Can default to true or require expert verification
        OutbreakIncident saved = repository.save(report);

        // Broadcast alert
        notificationService.createAndDispatchNotification(
                reportingFarmerId,
                "Outbreak Alert: " + report.getDiseaseName(),
                String.format("Disease incident recorded in %s. Check recommended preventative sprays on radar.", report.getDistrict()),
                "OUTBREAK_ALERT",
                report.getSeverity()
        );

        return saved;
    }

    private double calculateHaversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
