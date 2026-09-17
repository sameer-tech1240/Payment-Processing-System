package com.paymentprocessing.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.paymentprocessing.dto.statementDto.StatementRequest;
import com.paymentprocessing.dto.statementDto.StatementResponse;
import com.paymentprocessing.dto.transactionDto.TransactionHistoryResponse;
import com.paymentprocessing.entity.BankAccount;
import com.paymentprocessing.entity.Transaction;
import com.paymentprocessing.exception.ResourceNotFoundException;
import com.paymentprocessing.repository.BankAccountRepository;
import com.paymentprocessing.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatementService {

	private final TransactionRepository transactionRepository;
	private final BankAccountRepository bankAccountRepository;

	public StatementResponse generateStatement(StatementRequest request) {

		BankAccount account = bankAccountRepository.findByAccountNumber(request.getAccountNumber())
				.orElseThrow(() -> new ResourceNotFoundException("Account not found"));

		LocalDateTime fromDate = request.getFromDate().atStartOfDay();

		LocalDateTime toDate = request.getToDate().atTime(23, 59, 59);

		List<Transaction> transactions = transactionRepository.getStatementTransactions(request.getAccountNumber(),
				fromDate, toDate);

		// Only SUCCESS transactions affect balance
		List<Transaction> successfulTransactions = transactions.stream()
				.filter(txn -> txn.getStatus().name().equals("SUCCESS")).collect(Collectors.toList());

		BigDecimal totalDebit = successfulTransactions.stream()
				.filter(txn -> txn.getSenderAccount().equals(request.getAccountNumber())).map(Transaction::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalCredit = successfulTransactions.stream()
				.filter(txn -> txn.getReceiverAccount().equals(request.getAccountNumber())).map(Transaction::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		List<TransactionHistoryResponse> transactionResponses = successfulTransactions.stream().map(txn -> {

			String direction = txn.getSenderAccount().equals(request.getAccountNumber()) ? "DEBIT" : "CREDIT";

			return TransactionHistoryResponse.builder().transactionId(txn.getTransactionId())
					.senderAccount(txn.getSenderAccount()).receiverAccount(txn.getReceiverAccount())
					.amount(txn.getAmount()).status(txn.getStatus().name()).remarks(txn.getRemarks())
					.transactionDirection(direction).createdAt(txn.getCreatedAt()).build();
		}).collect(Collectors.toList());

		BigDecimal openingBalance = account.getBalance().subtract(totalCredit).add(totalDebit);

		return StatementResponse.builder().customerName(account.getUser().getFullName())
				.accountNumber(account.getAccountNumber()).accountType(account.getAccountType())
				.ifscCode(account.getIfscCode()).accountStatus(account.getAccountStatus().name())
				.email(account.getUser().getEmail()).mobileNumber(account.getMobileNumber())
				.openingBalance(openingBalance).closingBalance(account.getBalance()).totalDebit(totalDebit)
				.totalCredit(totalCredit).statementPeriod(request.getFromDate() + " to " + request.getToDate())
				.totalTransactions(successfulTransactions.size()).generatedOn(LocalDateTime.now())
				.transactions(transactionResponses).build();
	}
}