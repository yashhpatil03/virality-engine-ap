package com.yash.virality_engine_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long authorId;

    private String authorType; // USER or BOT

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;
}