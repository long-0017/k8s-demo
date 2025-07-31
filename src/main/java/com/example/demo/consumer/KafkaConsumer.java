package com.example.demo.consumer;

import com.example.demo.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaConsumer {

    /**
     * 监听消息主题
     */
    @KafkaListener(topics = "mytopic", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessage(ConsumerRecord<String, MessageDTO> record, Acknowledgment acknowledgment) {
        try {
            MessageDTO message = record.value();
            log.info("收到消息: ID={}, 发送者={}, 内容={}, 分区={}, 偏移量={}", message.getId(), message.getSender(),
                    message.getContent(), record.partition(), record.offset());

            // 业务处理逻辑...

            // 手动提交偏移量
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("处理消息出错: {}", e.getMessage());
            // 可以根据需要决定是否提交偏移量
        }
    }
}
