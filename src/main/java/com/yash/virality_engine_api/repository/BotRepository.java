package com.yash.virality_engine_api.repository;

import com.yash.virality_engine_api.entity.Bot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotRepository extends JpaRepository<Bot, Long> {
}