package com.yash.virality_engine_api.repository;

import com.yash.virality_engine_api.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}