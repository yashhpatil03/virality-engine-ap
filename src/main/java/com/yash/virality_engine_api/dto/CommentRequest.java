package com.yash.virality_engine_api.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private String content;
    private Long authorId;
    private String authorType; // "USER" or "BOT"
    private Long parentCommentId; // Optional - for replies
}