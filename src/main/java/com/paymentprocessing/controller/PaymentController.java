package com.paymentprocessing.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentprocessing.dto.transactionDto.TransactionHistoryResponse;
import com.paymentprocessing.dto.transactionDto.TransactionResponse;
import com.paymentprocessing.dto.transactionDto.TransferRequest;
import com.paymentprocessing.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping("/transfer")
	public TransactionResponse transferMoney(Authentication authentication, @RequestBody TransferRequest request) {

		String email = authentication.getName();

		return paymentService.transferMoney(email, request);
	}

	@GetMapping("/history")
	public List<TransactionHistoryResponse> getHistory(Authentication authentication) {

		String email = authentication.getName();

		return paymentService.getTransactionHistory(email);
	}

	@GetMapping("/mini-statement")
	public List<TransactionHistoryResponse> getMiniStatement(Authentication authentication) {

		String email = authentication.getName();

		return paymentService.getMiniStatement(email);
	}
}