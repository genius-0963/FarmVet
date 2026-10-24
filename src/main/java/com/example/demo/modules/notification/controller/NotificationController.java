package com.example.demo.modules.notification.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.modules.notification.entity.AppNotification;
import com.example.demo.modules.notification.service.NotificationService;
import com.example.demo.security.UserPrincipal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = (userPrincipal != null) ? userPrincipal.getId() : 1L;
        return notificationService.subscribe(userId);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppNotification>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long userId = (userPrincipal != null) ? userPrincipal.getId() : 1L;
        List<AppNotification> list = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read"));
    }
}
