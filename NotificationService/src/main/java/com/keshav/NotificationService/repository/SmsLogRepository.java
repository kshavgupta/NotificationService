package com.keshav.NotificationService.repository;

import com.keshav.NotificationService.model.SmsLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.LocalDateTime;

public interface SmsLogRepository extends ElasticsearchRepository<SmsLog, String> {

    Page<SmsLog> findByPhoneNumberAndSentAtBetween(String phoneNumber, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    @Query("{\"match_phrase\": {\"message\": \"?0\"}}")
    Page<SmsLog> findByMessagePhrase(String text, Pageable pageable);
}
