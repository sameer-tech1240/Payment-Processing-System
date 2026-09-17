package com.paymentprocessing.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.paymentprocessing.entity.AuditLog;
import com.paymentprocessing.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService {

	private final AuditLogRepository auditLogRepository;

	public void saveAuditLog(String username, String action, String status, String description) {

		AuditLog auditLog = AuditLog.builder().username(username).action(action).status(status).description(description)
				.createdAt(LocalDateTime.now()).build();

		auditLogRepository.save(auditLog);
	}
}