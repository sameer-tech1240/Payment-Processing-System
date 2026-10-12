package com.paymentprocessing.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paymentprocessing.dto.transactionDto.AdminTransactionResponse;
import com.paymentprocessing.dto.transactionDto.AdminTransactionReviewRequest;
import com.paymentprocessing.dto.transactionDto.TransactionHistoryResponse;
import com.paymentprocessing.dto.transactionDto.TransactionResponse;
import com.paymentprocessing.dto.transactionDto.TransferRequest;
import com.paymentprocessing.entity.BankAccount;
import com.paymentprocessing.entity.Transaction;
import com.paymentprocessing.entity.User;
import com.paymentprocessing.enums.AccountStatus;
import com.paymentprocessing.enums.TransactionStatus;
import com.paymentprocessing.enums.TransactionType;
import com.paymentprocessing.exception.ResourceNotFoundException;
import com.paymentprocessing.repository.BankAccountRepository;
import com.paymentprocessing.repository.TransactionRepository;
import com.paymentprocessing.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final BankAccountRepository bankAccountRepository;
	private final TransactionRepository transactionRepository;
	private final FraudDetectionService fraudDetectionService;
	private final AuditLogService auditLogService;
	private final UserRepository userRepository;

	@Transactional
	public TransactionResponse transferMoney(String email, TransferRequest request) {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		BankAccount sender = user.getBankAccount();

		if (sender == null) {
			throw new ResourceNotFoundException("Sender bank account not found");
		}

		if (request == null || request.getReceiverAccount() == null || request.getReceiverAccount().isBlank()) {

			return saveFailedTransaction(request, sender.getAccountNumber(), TransactionStatus.FAILED,
					"Receiver account is required");
		}

		if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

			return saveFailedTransaction(request, sender.getAccountNumber(), TransactionStatus.FAILED,
					"Invalid transfer amount");
		}

		BankAccount receiver = bankAccountRepository.findByAccountNumber(request.getReceiverAccount())
				.orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));

		boolean dailyLimitExceeded = exceedsDailyLimit(sender.getAccountNumber(), request.getAmount());

		String validationError = fraudDetectionService.validateTransfer(sender, receiver, request, dailyLimitExceeded);

		if (validationError != null) {

			if (isSecurityBlock(validationError)) {

				auditLogService.saveAuditLog(sender.getUser().getEmail(), "TRANSFER", "BLOCKED", validationError);

				return saveFailedTransaction(request, sender.getAccountNumber(), TransactionStatus.BLOCKED,
						validationError);
			}

			auditLogService.saveAuditLog(sender.getUser().getEmail(), "TRANSFER", "FAILED", validationError);

			return saveFailedTransaction(request, sender.getAccountNumber(), TransactionStatus.FAILED, validationError);
		}

		sender.setBalance(sender.getBalance().subtract(request.getAmount()));

		receiver.setBalance(receiver.getBalance().add(request.getAmount()));

		bankAccountRepository.save(sender);
		bankAccountRepository.save(receiver);

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

	public List<AdminTransactionResponse> getBlockedTransactions() {

		return transactionRepository.findByStatusOrderByCreatedAtDesc(TransactionStatus.BLOCKED).stream()
				.map(this::mapAdminTransaction).collect(Collectors.toList());
	}

	@Transactional
	public TransactionResponse reviewBlockedTransaction(String transactionId, AdminTransactionReviewRequest request,
			String adminEmail) {

		Transaction transaction = transactionRepository.findByTransactionId(transactionId)
				.orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

		if (transaction.getStatus() != TransactionStatus.BLOCKED) {

			throw new RuntimeException("Only BLOCKED transactions can be reviewed");
		}

		if (request == null) {
			throw new RuntimeException("Review request is required");
		}

		if (!request.isApproved()) {

			if (request.getReviewRemarks() == null || request.getReviewRemarks().isBlank()) {

				throw new RuntimeException("Review remarks are required for rejection");
			}

			transaction.setStatus(TransactionStatus.REJECTED);

			transaction.setReviewedBy(adminEmail);

			transaction.setReviewedAt(LocalDateTime.now());

			transaction.setReviewRemarks(request.getReviewRemarks());

			transaction.setRemarks("Transaction rejected by admin");

			transactionRepository.save(transaction);

			auditLogService.saveAuditLog(adminEmail, "TRANSFER_REVIEW", "REJECTED",
					"Transaction " + transactionId + " rejected. Reason: " + request.getReviewRemarks());

			return TransactionResponse.builder().transactionId(transactionId).status("REJECTED")
					.message("Transaction rejected successfully").build();
		}

		BankAccount sender = bankAccountRepository.findByAccountNumber(transaction.getSenderAccount())
				.orElseThrow(() -> new ResourceNotFoundException("Sender account not found"));

		BankAccount receiver = bankAccountRepository.findByAccountNumber(transaction.getReceiverAccount())
				.orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));

		if (sender.getAccountStatus() != AccountStatus.ACTIVE) {

			throw new RuntimeException("Sender account is not active");
		}

		if (receiver.getAccountStatus() != AccountStatus.ACTIVE) {

			throw new RuntimeException("Receiver account is not active");
		}

		if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

			throw new RuntimeException("Invalid transaction amount");
		}

		if (sender.getBalance().compareTo(transaction.getAmount()) < 0) {

			throw new RuntimeException("Insufficient balance for release");
		}

		sender.setBalance(sender.getBalance().subtract(transaction.getAmount()));

		receiver.setBalance(receiver.getBalance().add(transaction.getAmount()));

		bankAccountRepository.save(sender);
		bankAccountRepository.save(receiver);

		transaction.setStatus(TransactionStatus.SUCCESS);

		transaction.setReviewedBy(adminEmail);

		transaction.setReviewedAt(LocalDateTime.now());

		transaction.setReviewRemarks(request.getReviewRemarks());

		transaction.setRemarks("Transaction released by admin");

		transactionRepository.save(transaction);

		auditLogService.saveAuditLog(adminEmail, "TRANSFER_REVIEW", "SUCCESS",
				"Blocked transaction " + transactionId + " released successfully");

		return TransactionResponse.builder().transactionId(transactionId).status("SUCCESS")
				.message("Blocked transaction released successfully").build();
	}

	private TransactionResponse saveFailedTransaction(TransferRequest request, String senderAccount,
			TransactionStatus status, String remarks) {

		String txnId = UUID.randomUUID().toString();

		String receiverAccount = request != null ? request.getReceiverAccount() : null;

		BigDecimal amount = request != null ? request.getAmount() : null;

		Transaction transaction = Transaction.builder().transactionId(txnId).senderAccount(senderAccount)
				.receiverAccount(receiverAccount).amount(amount).transactionType(TransactionType.OUTBOUND)
				.status(status).remarks(remarks).build();

		transactionRepository.save(transaction);

		return TransactionResponse.builder().transactionId(txnId).status(status.name()).message(remarks).build();
	}

	private AdminTransactionResponse mapAdminTransaction(Transaction transaction) {

		String senderName = getAccountHolderName(transaction.getSenderAccount());

		String receiverName = getAccountHolderName(transaction.getReceiverAccount());

		return AdminTransactionResponse.builder().transactionId(transaction.getTransactionId())
				.senderAccount(transaction.getSenderAccount()).senderName(senderName)
				.receiverAccount(transaction.getReceiverAccount()).receiverName(receiverName)
				.amount(transaction.getAmount()).status(transaction.getStatus().name())
				.remarks(transaction.getRemarks()).createdAt(transaction.getCreatedAt())
				.reviewedBy(transaction.getReviewedBy()).reviewedAt(transaction.getReviewedAt())
				.reviewRemarks(transaction.getReviewRemarks()).build();
	}

	private boolean isSecurityBlock(String message) {

		return "Transaction flagged as suspicious".equals(message);
	}

	public List<TransactionHistoryResponse> getTransactionHistory(String email) {

		BankAccount account = getUserAccount(email);

		return transactionRepository
				.findBySenderAccountOrReceiverAccountOrderByCreatedAtDesc(account.getAccountNumber(),
						account.getAccountNumber())
				.stream().map(txn -> mapTransaction(txn, account.getAccountNumber())).collect(Collectors.toList());
	}

	public List<TransactionHistoryResponse> getMiniStatement(String email) {

		BankAccount account = getUserAccount(email);

		return transactionRepository
				.findTop5BySenderAccountOrReceiverAccountOrderByCreatedAtDesc(account.getAccountNumber(),
						account.getAccountNumber())
				.stream().map(txn -> mapTransaction(txn, account.getAccountNumber())).collect(Collectors.toList());
	}

	private TransactionHistoryResponse mapTransaction(Transaction txn, String accountNumber) {

		String direction;

		if (txn.getSenderAccount().equals(accountNumber)) {

			direction = "OUTBOUND";

		} else if (txn.getReceiverAccount().equals(accountNumber)) {

			direction = "INBOUND";

		} else {

			direction = "UNKNOWN";
		}

		String senderName = getAccountHolderName(txn.getSenderAccount());

		String receiverName = getAccountHolderName(txn.getReceiverAccount());

		return TransactionHistoryResponse.builder().transactionId(txn.getTransactionId())
				.senderAccount(txn.getSenderAccount()).senderName(senderName).receiverAccount(txn.getReceiverAccount())
				.receiverName(receiverName).amount(txn.getAmount()).status(txn.getStatus().name())
				.remarks(txn.getRemarks()).transactionDirection(direction).createdAt(txn.getCreatedAt()).build();
	}

	private String getAccountHolderName(String accountNumber) {

		if (accountNumber == null) {
			return "Unknown";
		}

		return bankAccountRepository.findByAccountNumber(accountNumber).map(BankAccount::getUser).map(User::getFullName)
				.orElse("Unknown");
	}

	private BankAccount getUserAccount(String email) {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (user.getBankAccount() == null) {
			throw new ResourceNotFoundException("Bank account not found");
		}

		return user.getBankAccount();
	}

	private boolean exceedsDailyLimit(String senderAccount, BigDecimal amount) {

		LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

		LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

		List<Transaction> todayTransactions = transactionRepository
				.findBySenderAccountAndCreatedAtBetween(senderAccount, startOfDay, endOfDay);

		BigDecimal totalTransferred = todayTransactions.stream()
				.filter(txn -> txn.getStatus() == TransactionStatus.SUCCESS).map(Transaction::getAmount)
				.filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

		return totalTransferred.add(amount).compareTo(BigDecimal.valueOf(100000)) > 0;
	}
}