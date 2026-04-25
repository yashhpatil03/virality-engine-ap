package com.yash.virality_engine_api.dto;

import lombok.Data;
@Data
public class PostRequest {
    private String content;
    private Long authorId;
    private String authorType; // "USER" or "BOT"
}