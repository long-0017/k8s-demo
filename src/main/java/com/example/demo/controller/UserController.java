package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RefreshScope
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${app.name}")
    private String appName;

    @GetMapping("/app-info")
    public String getAppName(){
        return appName;
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        log.info("getUser:{}", id);
        return userService.getUser(id);
    }

    @PostMapping
    public User saveUser(@RequestBody User user) {
        log.info("saveUser:{}", user);
        return userService.saveUser(user);
    }
}    