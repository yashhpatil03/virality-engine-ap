package com.yash.virality_engine_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@Data
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;

    private Long authorId;

    private String authorType; // "USER" or "BOT"

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer depthLevel = 0; // Default 0 for top-level comments

    private LocalDateTime createdAt;

    // For threaded comments (parent-child relationship)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Comment> replies = new java.util.ArrayList<>();
}