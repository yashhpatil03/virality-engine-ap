package com.yash.virality_engine_api.service;

import com.yash.virality_engine_api.entity.*;
import com.yash.virality_engine_api.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BotService {

    private final BotRepository botRepository;
    private final CommentRepository commentRepository;

    public BotService(BotRepository botRepository, CommentRepository commentRepository) {
        this.botRepository = botRepository;
        this.commentRepository = commentRepository;
    }

    public void generateBotComments(Post post) {

        List<Bot> bots = botRepository.findAll();

        for (Bot bot : bots) {

            if (Math.random() > 0.5) { // random behavior

                Comment comment = new Comment();
                comment.setPostId(post.getId());
                comment.setAuthorId(bot.getId());
                comment.setAuthorType("BOT");
                comment.setContent("Bot " + bot.getName() + " reacts!");
                comment.setDepthLevel(0);

                commentRepository.save(comment);
            }
        }
    }
    public Bot createBot(Bot bot) {
        return botRepository.save(bot);
    }

    public List<Bot> getAllBots() {
        return botRepository.findAll();
    }
}