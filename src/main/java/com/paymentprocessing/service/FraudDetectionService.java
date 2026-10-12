package com.paymentprocessing.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.paymentprocessing.dto.transactionDto.TransferRequest;
import com.paymentprocessing.entity.BankAccount;
import com.paymentprocessing.enums.AccountStatus;

@Service
public class FraudDetectionService {

	public String validateTransfer(BankAccount sender, BankAccount receiver, TransferRequest request,
			boolean dailyLimitExceeded) {

		if (receiver.getAccountStatus() != AccountStatus.ACTIVE) {
			return "Receiver account is not active";
		}

		if (sender.getAccountNumber().equals(receiver.getAccountNumber())) {
			return "Self transfer not allowed";
		}

		if (sender.getAccountStatus() != AccountStatus.ACTIVE) {
			return "Sender account is not active";
		}

		if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			return "Invalid transfer amount";
		}

		if (sender.getBalance().compareTo(request.getAmount()) < 0) {
			return "Insufficient balance";
		}

		if (request.getAmount().compareTo(BigDecimal.valueOf(200000)) > 0) {
			return "Transaction flagged as suspicious";
		}

		if (dailyLimitExceeded) {
			return "Daily transaction limit exceeded";
		}

		return null;
	}
}