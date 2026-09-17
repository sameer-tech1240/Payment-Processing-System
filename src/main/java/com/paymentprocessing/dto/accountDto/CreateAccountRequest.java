package com.paymentprocessing.dto.accountDto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CreateAccountRequest {

	private String email;

	private String accountType;

	private String nomineeName;

	private String mobileNumber;

	private BigDecimal initialBalance;
}