package com.yash.virality_engine_api.service;

import com.yash.virality_engine_api.entity.Comment;
import com.yash.virality_engine_api.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Transactional
    public Comment addComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
    }

    public int calculateCommentDepth(Long parentCommentId) {
        if (parentCommentId == null) {
            return 0; // Top-level comment
        }

        Comment parentComment = getCommentById(parentCommentId);
        return parentComment.getDepthLevel() + 1;
    }

    public boolean isDepthAllowed(int depth) {
        return depth < 20; // Max depth is 20 levels (0-19 = 20 levels total)
    }
    public List<Comment> getCommentsByPost(Long postId) {
        return commentRepository.findByPostId(postId);
    }
}