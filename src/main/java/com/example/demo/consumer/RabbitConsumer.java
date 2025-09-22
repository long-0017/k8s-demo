package com.example.demo.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RabbitConsumer {
    @RabbitListener(queues = "demo-queue")
    public void receive(String msg) {
        log.info("Received: {}", msg);
    }
}
