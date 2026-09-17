package com.paymentprocessing.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	public TransactionResponse transferMoney(@RequestBody TransferRequest request) {

		return paymentService.transferMoney(request);
	}

	@GetMapping("/history/{accountNumber}")
	public List<TransactionHistoryResponse> getHistory(@PathVariable("accountNumber") String accountNumber) {

		return paymentService.getTransactionHistory(accountNumber);
	}

	@GetMapping("/mini-statement/{accountNumber}")
	public List<TransactionHistoryResponse> getMiniStatement(@PathVariable("accountNumber") String accountNumber) {
		return paymentService.getMiniStatement(accountNumber);
	}
}