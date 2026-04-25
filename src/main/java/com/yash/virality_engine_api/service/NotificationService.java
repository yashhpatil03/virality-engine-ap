package com.yash.virality_engine_api.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class NotificationService {

    private final RedisTemplate<String, String> redisTemplate;

    public NotificationService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Handle notification for bot interaction
     * If user received notification in last 15 min -> batch it
     * If not -> send immediately and set cooldown
     */
    public void handleBotInteraction(Long userId, String botName, String interactionType) {
        String cooldownKey = "user:" + userId + ":notif_cooldown";
        String pendingKey = "user:" + userId + ":pending_notifs";

        // Check if user is in cooldown period (received notification in last 15 min)
        Boolean hasCooldown = redisTemplate.hasKey(cooldownKey);

        if (Boolean.TRUE.equals(hasCooldown)) {
            // User is in cooldown - batch the notification
            String notificationMsg = botName + " " + interactionType + " on your post";
            redisTemplate.opsForList().rightPush(pendingKey, notificationMsg);
            System.out.println("📦 Batched notification for user " + userId + ": " + notificationMsg);
        } else {
            // No cooldown - send notification immediately
            System.out.println("🔔 Push Notification Sent to User " + userId +
                    ": " + botName + " " + interactionType + " on your post");

            // Set 15-minute cooldown
            redisTemplate.opsForValue().set(cooldownKey, "1", Duration.ofMinutes(15));
        }
    }

    /**
     * Get all pending notifications for a user
     */
    public List<String> getPendingNotifications(Long userId) {
        String pendingKey = "user:" + userId + ":pending_notifs";
        List<String> notifications = redisTemplate.opsForList().range(pendingKey, 0, -1);
        return notifications != null ? notifications : new ArrayList<>();
    }

    /**
     * Clear pending notifications for a user
     */
    public void clearPendingNotifications(Long userId) {
        String pendingKey = "user:" + userId + ":pending_notifs";
        redisTemplate.delete(pendingKey);
    }

    /**
     * Get all users who have pending notifications
     * This scans Redis for keys matching pattern "user:*:pending_notifs"
     */
    public Set<String> getAllUsersWithPendingNotifications() {
        String pattern = "user:*:pending_notifs";
        return redisTemplate.keys(pattern);
    }
}
