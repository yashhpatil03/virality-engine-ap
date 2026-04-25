package com.yash.virality_engine_api.controller;

import com.yash.virality_engine_api.dto.CommentRequest;
import com.yash.virality_engine_api.dto.LikeRequest;
import com.yash.virality_engine_api.dto.PostRequest;
import com.yash.virality_engine_api.entity.Bot;
import com.yash.virality_engine_api.entity.Comment;
import com.yash.virality_engine_api.entity.Like;
import com.yash.virality_engine_api.entity.Post;
import com.yash.virality_engine_api.repository.BotRepository;
import com.yash.virality_engine_api.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final LikeService likeService;
    private final RedisViralityService redisViralityService;
    private final BotRepository botRepository;
    private final NotificationService notificationService;

    public PostController(PostService postService,
                          CommentService commentService,
                          LikeService likeService,
                          RedisViralityService redisViralityService,
                          NotificationService notificationService,
                          BotRepository botRepository) {  // Add this
        this.postService = postService;
        this.commentService = commentService;
        this.likeService = likeService;
        this.redisViralityService = redisViralityService;
        this.notificationService = notificationService;
        this.botRepository = botRepository;// Add this
    }

    // POST /api/posts
    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody PostRequest request) {
        try {
            Post post = new Post();
            post.setContent(request.getContent());
            post.setCreatedAt(LocalDateTime.now());

            // Handle author (can be User or Bot)
            if ("USER".equalsIgnoreCase(request.getAuthorType())) {
                post.setAuthorId(request.getAuthorId());
                post.setAuthorType("USER");
            } else if ("BOT".equalsIgnoreCase(request.getAuthorType())) {
                post.setAuthorId(request.getAuthorId());
                post.setAuthorType("BOT");
            } else {
                return ResponseEntity.badRequest().body("Invalid author type");
            }

            Post savedPost = postService.createPost(post);
            return ResponseEntity.ok(savedPost);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating post: " + e.getMessage());
        }
    }

    // POST /api/posts/{postId}/comments
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long postId,
                                        @RequestBody CommentRequest request) {
        try {
            // Check if post exists
            Post post = postService.getPostById(postId);

            // Calculate depth if this is a reply to another comment
            int depthLevel = 0;
            Long parentCommentId = request.getParentCommentId();

            if (parentCommentId != null) {
                depthLevel = commentService.calculateCommentDepth(parentCommentId);

                // Check vertical cap (max 20 levels)
                if (!commentService.isDepthAllowed(depthLevel)) {
                    return ResponseEntity.status(400)
                            .body("Comment depth cannot exceed 20 levels. Current depth: " + depthLevel);
                }
            }

            // For bot comments, check horizontal cap
            if ("BOT".equalsIgnoreCase(request.getAuthorType())) {
                // Check horizontal cap
                // Check horizontal cap using atomic operation
                if (!redisViralityService.tryAddBotReply(postId, 100)) {
                    return ResponseEntity.status(429)
                            .body("Too many bot replies. Maximum 100 bot replies per post.");
                }

                // Check cooldown (bot cannot interact with same human within 10 min)
                if ("USER".equalsIgnoreCase(post.getAuthorType())) {
                    if (redisViralityService.isBotOnCooldown(request.getAuthorId(), post.getAuthorId())) {
                        return ResponseEntity.status(429)
                                .body("Bot cannot interact with same user within 10 minutes. Please wait.");
                    }
                }
            }

            // Create and save comment
            Comment comment = new Comment();
            comment.setPostId(postId);
            comment.setContent(request.getContent());
            comment.setCreatedAt(LocalDateTime.now());
            comment.setDepthLevel(depthLevel);

            if ("USER".equalsIgnoreCase(request.getAuthorType())) {
                comment.setAuthorId(request.getAuthorId());
                comment.setAuthorType("USER");
            } else if ("BOT".equalsIgnoreCase(request.getAuthorType())) {
                comment.setAuthorId(request.getAuthorId());
                comment.setAuthorType("BOT");
            } else {
                return ResponseEntity.badRequest().body("Invalid author type");
            }

            // Set parent comment if this is a reply
            if (parentCommentId != null) {
                Comment parentComment = commentService.getCommentById(parentCommentId);
                comment.setParentComment(parentComment);
            }

            // SAVE TO DATABASE FIRST (within transaction)
            Comment savedComment = commentService.addComment(comment);

            // ONLY AFTER SUCCESSFUL DB SAVE, UPDATE REDIS
            if ("BOT".equalsIgnoreCase(request.getAuthorType())) {
                // Update Redis counters (atomic operations)
                redisViralityService.updateViralityScore(postId, "BOT_REPLY");
                redisViralityService.incrementBotReplyCount(postId);

                // Set cooldown to prevent immediate repeated interactions
                if ("USER".equalsIgnoreCase(post.getAuthorType())) {
                    redisViralityService.setBotCooldown(request.getAuthorId(), post.getAuthorId());
                }

                // Handle notification (with batching)
                String botName = botRepository.findById(request.getAuthorId())
                        .map(Bot::getName)
                        .orElse("Bot " + request.getAuthorId());

                notificationService.handleBotInteraction(
                        post.getAuthorId(),
                        botName,
                        "replied"
                );
            } else if ("USER".equalsIgnoreCase(request.getAuthorType())) {
                redisViralityService.updateViralityScore(postId, "COMMENT");
            }

            return ResponseEntity.ok(savedComment);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding comment: " + e.getMessage());
        }
    }

    // POST /api/posts/{postId}/like
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable Long postId,
                                      @RequestBody LikeRequest request) {
        try {
            // Check if post exists
            Post post = postService.getPostById(postId);

            // Check if user already liked this post
            if (likeService.hasUserLikedPost(postId, request.getUserId())) {
                return ResponseEntity.badRequest().body("User already liked this post");
            }

            Like like = new Like();
            like.setPostId(postId);
            like.setUserId(request.getUserId());
            like.setCreatedAt(LocalDateTime.now());

            Like savedLike = likeService.addLike(like);

            // Update virality score for like
            redisViralityService.updateViralityScore(postId, "LIKE");

            return ResponseEntity.ok(savedLike);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error liking post: " + e.getMessage());
        }
    }


    // Get virality score for a post (for testing)
    @GetMapping("/posts/{postId}/virality-score")
    public ResponseEntity<Integer> getViralityScore(@PathVariable Long postId) {
        int score = redisViralityService.getViralityScore(postId);
        return ResponseEntity.ok(score);
    }
    // Test endpoint to check pending notifications for a user
    @GetMapping("/users/{userId}/notifications/pending")
    public ResponseEntity<?> getPendingNotifications(@PathVariable Long userId) {
        List<String> notifications = notificationService.getPendingNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
}