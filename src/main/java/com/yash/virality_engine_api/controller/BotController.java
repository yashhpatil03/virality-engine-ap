package com.yash.virality_engine_api.controller;

import com.yash.virality_engine_api.entity.Bot;
import com.yash.virality_engine_api.service.BotService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bots")
public class BotController {

    private final BotService botService;

    public BotController(BotService botService) {
        this.botService = botService;
    }

    @PostMapping
    public Bot createBot(@RequestBody Bot bot) {
        return botService.createBot(bot);
    }

    @GetMapping
    public List<Bot> getBots() {
        return botService.getAllBots();
    }
}