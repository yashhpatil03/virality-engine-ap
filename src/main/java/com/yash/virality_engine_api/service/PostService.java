package com.yash.virality_engine_api.service;

import com.yash.virality_engine_api.entity.Post;
import com.yash.virality_engine_api.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final BotService botService;

    public PostService(PostRepository postRepository, BotService botService) {
        this.postRepository = postRepository;
        this.botService = botService;
    }

    @Transactional
    public Post createPost(Post post) {
        Post saved = postRepository.save(post);
        // REMOVED the problematic line that was causing null pointer
        // If you want auto bot comments, uncomment the line below:
        // botService.generateBotComments(saved);
        return saved;
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
    }
}