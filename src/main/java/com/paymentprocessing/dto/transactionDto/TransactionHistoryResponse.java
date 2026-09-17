package com.paymentprocessing.dto.transactionDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionHistoryResponse {

	private String transactionId;

	private String senderAccount;

	private String receiverAccount;

	private BigDecimal amount;

	private String status;

	private String remarks;

	private String transactionDirection;

	private LocalDateTime createdAt;
}