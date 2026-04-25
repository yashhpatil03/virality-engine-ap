package com.yash.virality_engine_api.controller;

import com.yash.virality_engine_api.service.ViralityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
    @RequestMapping("/virality")
    public class ViralityController {

        private final ViralityService viralityService;

        public ViralityController(ViralityService viralityService) {
            this.viralityService = viralityService;
        }

        @GetMapping("/{postId}")
        public int getScore(@PathVariable Long postId) {
            return viralityService.calculateScore(postId);
        }
    }

