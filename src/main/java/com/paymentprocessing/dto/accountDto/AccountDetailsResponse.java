package com.paymentprocessing.dto.accountDto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountDetailsResponse {

	private Long userId;

	private Boolean hasAccount;

	private String fullName;

	private String email;

	private String accountNumber;

	private String ifscCode;

	private BigDecimal balance;

	private String accountStatus;

	private String mobileNumber;

	private String nomineeName;
}