package com.example.demo.controller;

import com.example.demo.producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
@RequiredArgsConstructor
public class KafkaController {

    private final KafkaProducer kafkaProducer;

    /**
     * 提供HTTP接口发送Kafka消息
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(
            @RequestParam String content,
            @RequestParam(defaultValue = "system") String sender) {

        try {
            kafkaProducer.sendMessage(content, sender);
            return ResponseEntity.ok("消息已发送");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("发送失败: " + e.getMessage());
        }
    }
}
