package com.afrudeen.notification.controller;

import com.afrudeen.notification.entity.Notification;
import com.afrudeen.notification.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications")

public class NotificationController {

    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/my")
    @Operation(summary = "My notifications, newest first")
    public List<Notification> mine(@RequestHeader("X-User-Id") Long userId,
                                   @RequestHeader("X-User-Role") String role) {
        if ("ADMIN".equals(role)) {
            return repository.findByAudienceOrderByCreatedAtDesc("ADMIN");
        }
        return repository.findByUserIdAndAudienceNotOrderByCreatedAtDesc(userId, "ADMIN");
    }

}
