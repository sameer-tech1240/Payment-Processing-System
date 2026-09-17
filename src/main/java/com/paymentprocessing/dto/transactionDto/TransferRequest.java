package com.paymentprocessing.dto.transactionDto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransferRequest {

	private String senderAccount;

	private String receiverAccount;

	private BigDecimal amount;
}