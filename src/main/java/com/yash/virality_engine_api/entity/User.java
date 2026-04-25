package com.yash.virality_engine_api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users") // avoid reserved keyword issue
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private boolean isPremium;
}