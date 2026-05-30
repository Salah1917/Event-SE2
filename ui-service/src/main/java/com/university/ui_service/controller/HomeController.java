package com.university.ui_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/events")
    public String eventsList() {
        return "events/list";
    }

    @GetMapping("/events/create")
    public String createEvent() {
        return "events/create";
    }

    @GetMapping("/events/{id}")
    public String eventDetail(@PathVariable String id) {
        return "events/detail";
    }

    @GetMapping("/profile")
    public String profile() {
        return "user-profile";
    }
}
