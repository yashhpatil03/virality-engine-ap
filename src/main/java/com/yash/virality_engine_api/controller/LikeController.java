package com.yash.virality_engine_api.controller;

import com.yash.virality_engine_api.entity.Like;
import com.yash.virality_engine_api.service.LikeService;
import org.springframework.web.bind.annotation.*;

@RestController
    @RequestMapping("/likes")
    public class LikeController {

        private final LikeService likeService;

        public LikeController(LikeService likeService) {
            this.likeService = likeService;
        }

        @PostMapping
        public Like likePost(@RequestBody Like like) {
            return likeService.addLike(like);
        }

        @GetMapping("/{postId}")
        public int getLikes(@PathVariable Long postId) {
            return likeService.getLikes(postId);
        }
    }

