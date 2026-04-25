package com.yash.virality_engine_api.repository;

import com.yash.virality_engine_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}