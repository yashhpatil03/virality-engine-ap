package com.yash.virality_engine_api.repository;

import com.yash.virality_engine_api.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    int countByPostId(Long postId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);
}