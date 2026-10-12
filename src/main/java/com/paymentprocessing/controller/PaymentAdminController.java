package com.paymentprocessing.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentprocessing.dto.transactionDto.AdminTransactionResponse;
import com.paymentprocessing.dto.transactionDto.AdminTransactionReviewRequest;
import com.paymentprocessing.dto.transactionDto.TransactionResponse;
import com.paymentprocessing.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment/admin")
@RequiredArgsConstructor
public class PaymentAdminController {

	private final PaymentService paymentService;

	@GetMapping("/blocked")
	public List<AdminTransactionResponse> getBlockedTransactions() {
		return paymentService.getBlockedTransactions();
	}

	@PutMapping("/review/{transactionId}")
	public TransactionResponse reviewBlockedTransaction(@PathVariable("transactionId") String transactionId,
			@RequestBody AdminTransactionReviewRequest request, Authentication authentication) {

		String adminEmail = authentication.getName();

		return paymentService.reviewBlockedTransaction(transactionId, request, adminEmail);
	}
}