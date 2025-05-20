package com.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderExpireService {

    private static final String EXPIRY_KEY = "orderExpiryZSet";
    private final RedisTemplate<String, Object> redis;
}
