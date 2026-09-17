package com.paymentprocessing.dto.statementDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.paymentprocessing.dto.transactionDto.TransactionHistoryResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatementResponse {

	private String customerName;

	private String accountNumber;

	private String accountType;

	private String ifscCode;

	private String accountStatus;

	private String email;

	private String mobileNumber;

	private BigDecimal openingBalance;

	private BigDecimal closingBalance;

	private BigDecimal totalDebit;

	private BigDecimal totalCredit;

	private Integer totalTransactions;

	private String statementPeriod;

	private LocalDateTime generatedOn;

	private List<TransactionHistoryResponse> transactions;
}