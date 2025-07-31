package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, User> redisTemplate;

    public User getUser(Long id) {
        // 先从Redis中获取
        String key = "user:" + id;
        User user = redisTemplate.opsForValue().get(key);
        log.info("1. getUser from redis({})", id);

        if (user != null) {
            log.info("2. user in redis({})", user);
            return user;
        }
        
        // Redis中没有则从数据库获取
        Optional<User> optionalUser = userRepository.findById(id);
        log.info("3. user in db({})", id);
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            // 将数据存入Redis，设置1小时过期
            redisTemplate.opsForValue().set(key, user, 1, TimeUnit.HOURS);
        }
        
        return user;
    }

    public User saveUser(User user) {
        User savedUser = userRepository.save(user);
        log.info("1. saveUser({})", savedUser);
        // 更新Redis中的数据
        String key = "user:" + savedUser.getId();
        log.info("2. update Redis, key({})", key);
        redisTemplate.opsForValue().set(key, savedUser, 1, TimeUnit.HOURS);
        return savedUser;
    }
}    