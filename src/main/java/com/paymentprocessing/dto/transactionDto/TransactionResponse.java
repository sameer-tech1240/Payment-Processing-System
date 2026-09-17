package com.paymentprocessing.dto.transactionDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class TransactionResponse {

	private String transactionId;

	private String status;

	private String message;
}