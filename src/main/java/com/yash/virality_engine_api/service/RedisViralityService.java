package com.yash.virality_engine_api.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RedisViralityService {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisViralityService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Update virality score based on interaction
    public void updateViralityScore(Long postId, String interactionType) {
        String key = "post:" + postId + ":virality_score";
        int points = 0;

        switch (interactionType) {
            case "BOT_REPLY":
                points = 1;
                break;
            case "LIKE":
                points = 20;
                break;
            case "COMMENT":
                points = 50;
                break;
        }

        redisTemplate.opsForValue().increment(key, points);
    }

    // Get current virality score
    public Integer getViralityScore(Long postId) {
        String key = "post:" + postId + ":virality_score";
        String score = redisTemplate.opsForValue().get(key);
        return score != null ? Integer.parseInt(score) : 0;
    }

    // Check horizontal cap (max 100 bot replies per post)
    public boolean canAddBotReply(Long postId) {
        String key = "post:" + postId + ":bot_count";
        String count = redisTemplate.opsForValue().get(key);
        int currentCount = count != null ? Integer.parseInt(count) : 0;
        return currentCount < 100;
    }

    // Increment bot reply counter
    public void incrementBotReplyCount(Long postId) {
        String key = "post:" + postId + ":bot_count";
        redisTemplate.opsForValue().increment(key);
    }

    // Check cooldown (bot cannot interact with same human more than once per 10 min)
    public boolean isBotOnCooldown(Long botId, Long humanId) {
        String key = "cooldown:bot_" + botId + ":human_" + humanId;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    // Set cooldown for bot-human interaction
    public void setBotCooldown(Long botId, Long humanId) {
        String key = "cooldown:bot_" + botId + ":human_" + humanId;
        redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(10));
    }
    // Check vertical cap (comment depth cannot exceed 20)
    public boolean isDepthAllowed(int currentDepth) {
        return currentDepth < 20;
    }

    // Check both cooldown and get remaining time (optional)
    public Long getCooldownRemainingSeconds(Long botId, Long humanId) {
        String key = "cooldown:bot_" + botId + ":human_" + humanId;
        Long ttl = redisTemplate.getExpire(key);
        return ttl > 0 ? ttl : 0;
    }
    // ATOMIC VERSION - Use Redis INCR which is atomic
    public boolean tryAddBotReply(Long postId, int maxLimit) {
        String key = "post:" + postId + ":bot_count";

        // Atomic increment - this is the key to race condition prevention
        Long newCount = redisTemplate.opsForValue().increment(key);

        if (newCount != null && newCount <= maxLimit) {
            // Within limit - success
            return true;
        } else {
            // Exceeded limit - rollback the increment
            redisTemplate.opsForValue().decrement(key);
            return false;
        }
    }

}
