package com.paymentprocessing.dto.statementDto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class StatementRequest {

	private String accountNumber;

	private LocalDate fromDate;

	private LocalDate toDate;
}