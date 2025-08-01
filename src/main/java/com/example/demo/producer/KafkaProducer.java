package com.example.demo.producer;

import com.example.demo.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {

    private final KafkaTemplate<String, MessageDTO> kafkaTemplate;

    // 消息主题
    private static final String TOPIC_NAME = "mytopic";

    /**
     * 发送消息到Kafka
     */
    public void sendMessage(String content, String sender) {
        // 构建消息对象
        MessageDTO message = new MessageDTO();
        message.setId(UUID.randomUUID().toString());
        message.setContent(content);
        message.setSender(sender);
        message.setSendTime(LocalDateTime.now());

        // 发送消息
        ListenableFuture<SendResult<String, MessageDTO>> future =
                kafkaTemplate.send(TOPIC_NAME, message.getId(), message);

        // 处理发送结果
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, MessageDTO> result) {
                log.info("消息发送成功: {}, 分区: {}", message.getId(), result.getRecordMetadata().partition());
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("消息发送失败: {}, 原因: {}", message.getId(), ex.getMessage());
            }
        });
    }
}
