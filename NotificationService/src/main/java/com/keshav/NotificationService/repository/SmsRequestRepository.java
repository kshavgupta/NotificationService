package com.keshav.NotificationService.repository;

import com.keshav.NotificationService.model.SmsRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsRequestRepository extends JpaRepository<SmsRequest, String> {
}
