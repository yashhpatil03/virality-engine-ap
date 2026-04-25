package com.yash.virality_engine_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController
{
    @RequestMapping("/hlw")
    public String hello()
    {
        return "Hello World";
    }
}
