package com.yash.virality_engine_api.repository;

import com.yash.virality_engine_api.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostId(Long postId);

    Optional<Comment> findById(Long id);

    @Query("SELECT MAX(c.depthLevel) FROM Comment c WHERE c.postId = :postId")
    Integer getMaxDepthByPostId(@Param("postId") Long postId);

    long countByPostIdAndAuthorType(Long postId, String authorType);
}