package com.paymentprocessing.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.paymentprocessing.dto.transactionDto.TransactionHistoryResponse;
import com.paymentprocessing.dto.transactionDto.TransactionResponse;
import com.paymentprocessing.dto.transactionDto.TransferRequest;
import com.paymentprocessing.entity.BankAccount;
import com.paymentprocessing.entity.Transaction;
import com.paymentprocessing.enums.TransactionStatus;
import com.paymentprocessing.enums.TransactionType;
import com.paymentprocessing.exception.ResourceNotFoundException;
import com.paymentprocessing.repository.BankAccountRepository;
import com.paymentprocessing.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final BankAccountRepository bankAccountRepository;

	private final TransactionRepository transactionRepository;

	private final FraudDetectionService fraudDetectionService;

	private final AuditLogService auditLogService;

	public TransactionResponse transferMoney(TransferRequest request) {

		BankAccount sender = bankAccountRepository.findByAccountNumber(request.getSenderAccount())
				.orElseThrow(() -> new ResourceNotFoundException("Sender account not found"));

		BankAccount receiver = bankAccountRepository.findByAccountNumber(request.getReceiverAccount())
				.orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));

		boolean dailyLimitExceeded = exceedsDailyLimit(sender.getAccountNumber(), request.getAmount());
		String validationError = fraudDetectionService.validateTransfer(sender, receiver, request, dailyLimitExceeded);

		if (validationError != null) {

			if (validationError.contains("blocked") || validationError.contains("suspicious")
					|| validationError.contains("limit")) {

				auditLogService.saveAuditLog(sender.getUser().getEmail(), "TRANSFER", "BLOCKED", validationError);

				return saveFailedTransaction(request, TransactionStatus.BLOCKED, validationError);
			}

			auditLogService.saveAuditLog(sender.getUser().getEmail(), "TRANSFER", "FAILED", validationError);

			return saveFailedTransaction(request, TransactionStatus.FAILED, validationError);
		}

		// DEBIT SENDER
		sender.setBalance(sender.getBalance().subtract(request.getAmount()));

		// CREDIT RECEIVER
		receiver.setBalance(receiver.getBalance().add(request.getAmount()));

		bankAccountRepository.save(sender);
		bankAccountRepository.save(receiver);

		// SAVE TRANSACTION
		String txnId = UUID.randomUUID().toString();

		Transaction transaction = Transaction.builder().transactionId(txnId).senderAccount(sender.getAccountNumber())
				.receiverAccount(receiver.getAccountNumber()).amount(request.getAmount())
				.transactionType(TransactionType.OUTBOUND).status(TransactionStatus.SUCCESS)
				.remarks("Transaction Successful").build();

		transactionRepository.save(transaction);

		auditLogService.saveAuditLog(sender.getUser().getEmail(), "TRANSFER", "SUCCESS",
				"Transferred " + request.getAmount() + " to " + receiver.getAccountNumber());

		return TransactionResponse.builder().transactionId(txnId).status("SUCCESS")
				.message("Money transferred successfully").build();
	}

	private TransactionResponse saveFailedTransaction(TransferRequest request, TransactionStatus status,
			String remarks) {

		String txnId = UUID.randomUUID().toString();

		Transaction transaction = Transaction.builder().transactionId(txnId).senderAccount(request.getSenderAccount())
				.receiverAccount(request.getReceiverAccount()).amount(request.getAmount())
				.transactionType(TransactionType.OUTBOUND).status(status).remarks(remarks).build();

		transactionRepository.save(transaction);

		return TransactionResponse.builder().transactionId(txnId).status(status.name()).message(remarks).build();
	}

	public List<TransactionHistoryResponse> getTransactionHistory(String accountNumber) {
		return transactionRepository
				.findBySenderAccountOrReceiverAccountOrderByCreatedAtDesc(accountNumber, accountNumber).stream()
				.map(txn -> {

					String direction = txn.getSenderAccount().equals(accountNumber) ? "OUTBOUND" : "INBOUND";

					return TransactionHistoryResponse.builder().transactionId(txn.getTransactionId())
							.senderAccount(txn.getSenderAccount()).receiverAccount(txn.getReceiverAccount())
							.amount(txn.getAmount()).status(txn.getStatus().name()).remarks(txn.getRemarks())
							.transactionDirection(direction).createdAt(txn.getCreatedAt()).build();
				}).collect(Collectors.toList());
	}

	public List<TransactionHistoryResponse> getMiniStatement(String accountNumber) {
		return transactionRepository
				.findTop5BySenderAccountOrReceiverAccountOrderByCreatedAtDesc(accountNumber, accountNumber).stream()
				.map(txn -> {

					String direction = txn.getSenderAccount().equals(accountNumber) ? "OUTBOUND" : "INBOUND";

					return TransactionHistoryResponse.builder().transactionId(txn.getTransactionId())
							.senderAccount(txn.getSenderAccount()).receiverAccount(txn.getReceiverAccount())
							.amount(txn.getAmount()).status(txn.getStatus().name()).remarks(txn.getRemarks())
							.transactionDirection(direction).createdAt(txn.getCreatedAt()).build();
				}).collect(Collectors.toList());
	}

	private boolean exceedsDailyLimit(String senderAccount, BigDecimal amount) {

		LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

		LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

		List<Transaction> todayTransactions = transactionRepository
				.findBySenderAccountAndCreatedAtBetween(senderAccount, startOfDay, endOfDay);

		BigDecimal totalTransferred = todayTransactions.stream()
				.filter(txn -> txn.getStatus() == TransactionStatus.SUCCESS).map(Transaction::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		return totalTransferred.add(amount).compareTo(BigDecimal.valueOf(100000)) > 0;
	}
}