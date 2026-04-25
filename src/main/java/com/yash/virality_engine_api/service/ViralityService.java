package com.yash.virality_engine_api.service;

import com.yash.virality_engine_api.entity.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import com.yash.virality_engine_api.repository.*;
import org.springframework.stereotype.Service;

@Service
public class ViralityService {

    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final BotRepository botRepository;

    public ViralityService(LikeRepository likeRepository,
                           CommentRepository commentRepository,
                           BotRepository botRepository) {
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.botRepository = botRepository;
    }

    public int calculateScore(Long postId) {
        int likes = likeRepository.countByPostId(postId);

        int comments = (int) commentRepository.findAll()
                .stream()
                .filter(c -> c.getPostId().equals(postId))
                .count();

        return likes + (comments * 2); // weight comments more
    }


    private final Random random = new Random();

    public void generateVirality(Post post) {

        List<Bot> bots = botRepository.findAll();

        for (Bot bot : bots) {

            // 50% chance to comment
            if (random.nextBoolean()) {

                Comment comment = new Comment();
                comment.setPostId(post.getId());
                comment.setAuthorId(bot.getId());
                comment.setAuthorType("BOT");
                comment.setContent(getRandomComment(bot));
                comment.setCreatedAt(LocalDateTime.now());
                comment.setDepthLevel(0);

                Comment saved = commentRepository.save(comment);

                generateReplies(saved, bot, 1);
            }
        }
    }
    private void generateReplies(Comment parent, Bot bot, int depth) {

        if (depth > 3) return;

        if (random.nextInt(100) < 40) {

            Comment reply = new Comment();
            reply.setPostId(parent.getPostId());
            reply.setAuthorId(bot.getId());
            reply.setAuthorType("BOT");
            reply.setContent("Reply level " + depth);
            reply.setCreatedAt(LocalDateTime.now());
            reply.setDepthLevel(depth);

            Comment savedReply = commentRepository.save(reply);

            generateReplies(savedReply, bot, depth + 1);
        }
    }
    private String getRandomComment(Bot bot) {

        String[] texts = {
                "Nice post!",
                "Interesting 🤔",
                "I agree!",
                "Explain more!",
                "🔥🔥🔥",
                "Going viral!",
                "Not convinced...",
                "Loved this!",
                "Controversial 👀"
        };

        return texts[random.nextInt(texts.length)] + " - " + bot.getName();
    }

}
