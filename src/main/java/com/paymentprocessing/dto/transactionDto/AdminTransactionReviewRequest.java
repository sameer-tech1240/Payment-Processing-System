package com.paymentprocessing.dto.transactionDto;

import lombok.Data;

@Data
public class AdminTransactionReviewRequest {

	private boolean approved;

	private String reviewRemarks;
}