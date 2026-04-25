package com.yash.virality_engine_api.service;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@EnableScheduling
public class ScheduledNotificationService {

    private final NotificationService notificationService;

    public ScheduledNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Runs every 5 minutes (for testing)
     * In production, this would run every 15 minutes
     * Scans all users with pending notifications and sends summarized batch
     */
    @Scheduled(fixedDelay = 300000) // 300,000 ms = 5 minutes
    public void processBatchedNotifications() {
        System.out.println("\n=== CRON Sweeper Running ===");

        // Get all users with pending notifications
        Set<String> pendingKeys = notificationService.getAllUsersWithPendingNotifications();

        if (pendingKeys == null || pendingKeys.isEmpty()) {
            System.out.println("No pending notifications to process");
            return;
        }

        for (String key : pendingKeys) {
            // Extract user ID from key pattern "user:X:pending_notifs"
            Long userId = extractUserIdFromKey(key);
            if (userId == null) continue;

            // Get all pending notifications for this user
            List<String> notifications = notificationService.getPendingNotifications(userId);

            if (notifications != null && !notifications.isEmpty()) {
                // Create summary message
                String summary = createSummaryMessage(notifications);
                System.out.println("📧 Summarized Push Notification for User " + userId + ": " + summary);

                // Clear the pending notifications after sending
                notificationService.clearPendingNotifications(userId);
            }
        }

        System.out.println("=== CRON Sweeper Finished ===\n");
    }

    private Long extractUserIdFromKey(String key) {
        try {
            // Key format: "user:123:pending_notifs"
            String[] parts = key.split(":");
            if (parts.length >= 2) {
                return Long.parseLong(parts[1]);
            }
        } catch (Exception e) {
            System.err.println("Error extracting user ID from key: " + key);
        }
        return null;
    }

    private String createSummaryMessage(List<String> notifications) {
        if (notifications.isEmpty()) {
            return "No interactions";
        }

        if (notifications.size() == 1) {
            return notifications.get(0);
        }

        // Get the first bot name and count others
        String firstNotification = notifications.get(0);
        String firstBotName = extractBotName(firstNotification);
        int othersCount = notifications.size() - 1;

        return firstBotName + " and " + othersCount + " others interacted with your posts";
    }

    private String extractBotName(String notification) {
        // Extract bot name from notification like "TestBot replied on your post"
        if (notification.contains(" replied")) {
            return notification.substring(0, notification.indexOf(" replied"));
        }
        if (notification.contains(" liked")) {
            return notification.substring(0, notification.indexOf(" liked"));
        }
        return "A bot";
    }
}
