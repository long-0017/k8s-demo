package com.example.demo.controller;

import com.example.demo.producer.RabbitProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rabbit")
public class RabbitController {

    @Autowired
    private RabbitProducer rabbitProducer;

    @GetMapping("/send")
    public ResponseEntity<String> send(){
        rabbitProducer.send("hello");
        return ResponseEntity.ok("消息已发送");
    }


}
