package com.paymentprocessing.dto.transactionDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AdminTransactionResponse {

	private String transactionId;

	private String senderAccount;

	private String senderName;

	private String receiverAccount;

	private String receiverName;

	private BigDecimal amount;

	private String status;

	private String remarks;

	private LocalDateTime createdAt;

	private String reviewedBy;

	private LocalDateTime reviewedAt;

	private String reviewRemarks;
}