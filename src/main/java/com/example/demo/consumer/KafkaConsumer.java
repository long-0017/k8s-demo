package com.example.demo.consumer;

import com.example.demo.dto.MessageDTO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    /**
     * 监听消息主题
     */
    @KafkaListener(topics = "mytopic", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessage(ConsumerRecord<String, MessageDTO> record, Acknowledgment acknowledgment) {
        try {
            MessageDTO message = record.value();
            System.out.println("收到消息: " +
                    "ID=" + message.getId() +
                    ", 发送者=" + message.getSender() +
                    ", 内容=" + message.getContent() +
                    ", 分区=" + record.partition() +
                    ", 偏移量=" + record.offset());

            // 业务处理逻辑...

            // 手动提交偏移量
            acknowledgment.acknowledge();
        } catch (Exception e) {
            System.err.println("处理消息出错: " + e.getMessage());
            // 可以根据需要决定是否提交偏移量
        }
    }
}
