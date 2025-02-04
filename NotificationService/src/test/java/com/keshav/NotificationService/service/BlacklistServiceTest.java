package com.keshav.NotificationService.service;

import com.keshav.NotificationService.dto.BlacklistRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BlacklistServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private BlacklistService blacklistService;

    private final String BLACKLIST_KEY = "blacklist";

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testAddToBlacklist() {
        BlacklistRequestDto requestDto = new BlacklistRequestDto();
        requestDto.setPhoneNumbers(Arrays.asList("1234567890", "0987654321"));

        when(redisTemplate.execute(any(RedisCallback.class))).thenReturn(null);

        blacklistService.addToBlacklist(requestDto);

        verify(redisTemplate, times(1)).execute(any(RedisCallback.class));
    }

    @Test
    public void testRemoveFromBlacklist() {
        BlacklistRequestDto requestDto = new BlacklistRequestDto();
        requestDto.setPhoneNumbers(Arrays.asList("1234567890", "0987654321"));

        when(redisTemplate.execute(any(RedisCallback.class))).thenReturn(null);

        blacklistService.removeFromBlacklist(requestDto);

        verify(redisTemplate, times(1)).execute(any(RedisCallback.class));
    }

    @Test
    public void testGetBlacklist() {
        Set<String> keys = new HashSet<>(Arrays.asList("blacklist:1234567890", "blacklist:0987654321"));

        when(redisTemplate.keys(BLACKLIST_KEY + ":*")).thenReturn(keys);

        List<String> blacklist = blacklistService.getBlacklist();

        assertEquals(2, blacklist.size());
        assertTrue(blacklist.contains("1234567890"));
        assertTrue(blacklist.contains("0987654321"));

        verify(redisTemplate, times(1)).keys(BLACKLIST_KEY + ":*");
    }

    @Test
    public void testGetBlacklist_Empty() {
        when(redisTemplate.keys(BLACKLIST_KEY + ":*")).thenReturn(Collections.emptySet());

        List<String> blacklist = blacklistService.getBlacklist();

        assertTrue(blacklist.isEmpty());

        verify(redisTemplate, times(1)).keys(BLACKLIST_KEY + ":*");
    }

    @Test
    public void testIsBlacklisted() {
        String phoneNumber = "1234567890";

        when(redisTemplate.hasKey(BLACKLIST_KEY + ":" + phoneNumber)).thenReturn(true);

        boolean isBlacklisted = blacklistService.isBlacklisted(phoneNumber);

        assertTrue(isBlacklisted);

        verify(redisTemplate, times(1)).hasKey(BLACKLIST_KEY + ":" + phoneNumber);
    }

    @Test
    public void testIsBlacklisted_NotBlacklisted() {
        String phoneNumber = "1234567890";

        when(redisTemplate.hasKey(BLACKLIST_KEY + ":" + phoneNumber)).thenReturn(false);

        boolean isBlacklisted = blacklistService.isBlacklisted(phoneNumber);

        assertFalse(isBlacklisted);

        verify(redisTemplate, times(1)).hasKey(BLACKLIST_KEY + ":" + phoneNumber);
    }
}

