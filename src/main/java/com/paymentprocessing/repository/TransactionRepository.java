package com.paymentprocessing.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.paymentprocessing.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	List<Transaction> findBySenderAccountOrReceiverAccount(String senderAccount, String receiverAccount);

	List<Transaction> findBySenderAccountOrReceiverAccountOrderByCreatedAtDesc(String senderAccount,
			String receiverAccount);

	List<Transaction> findTop5BySenderAccountOrReceiverAccountOrderByCreatedAtDesc(String senderAccount,
			String receiverAccount);

	List<Transaction> findBySenderAccountAndCreatedAtBetween(String senderAccount, LocalDateTime start,
			LocalDateTime end);

	@Query("""
			SELECT t
			FROM Transaction t
			WHERE
			(t.senderAccount = :account
			OR t.receiverAccount = :account)
			AND
			t.createdAt BETWEEN :fromDate AND :toDate
			ORDER BY t.createdAt DESC
			""")
	List<Transaction> getStatementTransactions(@Param("account") String account,
			@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);
}