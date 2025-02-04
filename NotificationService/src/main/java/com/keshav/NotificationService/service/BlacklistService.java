package com.keshav.NotificationService.service;

import com.keshav.NotificationService.dto.BlacklistRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service class for managing blacklisted phone numbers using Redis.
 * This class provides methods to add, remove, and check blacklisted phone numbers,
 * as well as retrieve the entire blacklist.
 */
@Service
public class BlacklistService {
    private static final Logger log = LoggerFactory.getLogger(BlacklistService.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final String BLACKLIST_KEY = "blacklist";

    /**
     * Constructs a new BlacklistService with the specified RedisTemplate.
     * @param redisTemplate the RedisTemplate to be used for Redis operations
     */
    public BlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Adds the provided phone numbers to the blacklist in Redis.
     * Each phone number is stored with a TTL (Time-To-Live) of 7 days.
     * If the operation fails, no numbers are added (transaction is discarded).
     * @param blacklistRequestDto the DTO containing the list of phone numbers to be blacklisted
     * @throws RuntimeException if the redis transaction fails
     */
    public void addToBlacklist(BlacklistRequestDto blacklistRequestDto) {
        List<String> phoneNumbers = blacklistRequestDto.getPhoneNumbers();

        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.multi();
            try {
                for (String phoneNumber : phoneNumbers) {
                    byte[] key = redisTemplate.getStringSerializer().serialize("blacklist:" + phoneNumber);
                    byte[] value = redisTemplate.getStringSerializer().serialize("BLACKLISTED");
                    connection.setEx(key, TimeUnit.DAYS.toSeconds(7), value);
                }
                return connection.exec();
            } catch (Exception e) {
                connection.discard();
                throw new RuntimeException("Redis transaction failed", e);
            }
        });

        log.info("Successfully added numbers to blacklist: {}", phoneNumbers);
    }


    /**
     * Removes the provided phone numbers from the blacklist in Redis.
     * If the operation fails, no numbers are removed (transaction is discarded).
     * @param blacklistRequestDto the DTO containing the list of phone numbers to be removed from the blacklist
     * @throws RuntimeException if the redis transaction fails
     */
    public void removeFromBlacklist(BlacklistRequestDto blacklistRequestDto) {
        List<String> phoneNumbers = blacklistRequestDto.getPhoneNumbers();

        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.multi();
            try {
                for (String phoneNumber : phoneNumbers) {
                    byte[] key = redisTemplate.getStringSerializer().serialize(BLACKLIST_KEY + ":" + phoneNumber);
                    connection.del(key);
                }
                return connection.exec();
            } catch (Exception e) {
                connection.discard();
                throw new RuntimeException("Redis transaction failed", e);
            }
        });

        log.info("Successfully removed numbers from blacklist: {}", phoneNumbers);
    }


    /**
     * Retrieves all the phone numbers currently in the blacklist.
     * @return a list of blacklisted phone numbers
     */
    public List<String> getBlacklist() {
        Set<String> keys = redisTemplate.keys(BLACKLIST_KEY + ":*");

        if (keys.isEmpty()) {
            return new ArrayList<>();
        }

        return keys.stream()
                .map(key -> key.replace(BLACKLIST_KEY + ":", ""))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a specific phone number is blacklisted.
     * @param phoneNumber the phone number to check
     * @return true if the phone number is blacklisted, false otherwise
     */
    public boolean isBlacklisted(String phoneNumber) {
        return redisTemplate.hasKey(BLACKLIST_KEY + ":" + phoneNumber);
    }
}
