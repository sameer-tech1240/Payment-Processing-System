package com.paymentprocessing.dto.accountDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AccountResponse {


	private String accountNumber;

	private String ifscCode;

	private String accountStatus;

	private String message;
}