package com.yash.virality_engine_api.service;

import com.yash.virality_engine_api.entity.Like;
import com.yash.virality_engine_api.repository.LikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;

    public LikeService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    @Transactional
    public Like addLike(Like like) {
        return likeRepository.save(like);
    }

    public int getLikes(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    public boolean hasUserLikedPost(Long postId, Long userId) {
        return likeRepository.existsByPostIdAndUserId(postId, userId);
    }
}