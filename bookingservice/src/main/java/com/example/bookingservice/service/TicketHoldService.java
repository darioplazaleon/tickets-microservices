package com.example.bookingservice.service;

import com.example.bookingservice.entity.TicketHold;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketHoldService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final Duration HOLD_TTL = Duration.ofMinutes(15);

    private String key(String holdId) {
        return "ticketHold:" + holdId;
    }

    public void holdTickets(String holdId, TicketHold ticketHold) {
        redisTemplate.opsForValue().set(key(holdId), ticketHold, HOLD_TTL);
    }

    public Optional<TicketHold> getTicketHold(String holdId) {
        return Optional.ofNullable((TicketHold) redisTemplate.opsForValue().get(key(holdId)));
    }

    public void releaseHold(String holdId) {
        redisTemplate.delete(key(holdId));
    }
}
