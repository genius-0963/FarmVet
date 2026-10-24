package com.example.demo.modules.notification.service;

import com.example.demo.modules.notification.entity.AppNotification;
import com.example.demo.modules.notification.repository.AppNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Transactional
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final AppNotificationRepository repository;
    private final Map<Long, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public NotificationService(AppNotificationRepository repository) {
        this.repository = repository;
    }

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(180_000L); // 3 minutes timeout

        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((e) -> removeEmitter(userId, emitter));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected to FarmVet Notification Stream"));
        } catch (IOException e) {
            removeEmitter(userId, emitter);
        }

        return emitter;
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }

    public AppNotification createAndDispatchNotification(Long userId, String title, String message, String alertType, String severity) {
        AppNotification notification = new AppNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setAlertType(alertType);
        notification.setSeverity(severity);
        notification.setCreatedAt(Instant.now());
        notification.setIsRead(false);

        AppNotification saved = repository.save(notification);

        // Push real-time SSE event to online subscribers
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("ALERT")
                            .data(saved));
                } catch (Exception e) {
                    removeEmitter(userId, emitter);
                }
            }
        }

        log.info("[NOTIFICATION DISPATCH] Dispatched alert to userId={}: title='{}'", userId, title);
        return saved;
    }

    public List<AppNotification> getUserNotifications(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long notificationId) {
        repository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            repository.save(n);
        });
    }
}
