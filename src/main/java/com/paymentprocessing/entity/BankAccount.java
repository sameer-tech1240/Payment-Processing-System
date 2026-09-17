package com.paymentprocessing.entity;

import java.math.BigDecimal;

import com.paymentprocessing.enums.AccountStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String accountNumber;

	private String ifscCode;

	private BigDecimal balance;

	@Enumerated(EnumType.STRING)
	private AccountStatus accountStatus;

	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	private String accountType;

	private String nomineeName;

	private String mobileNumber;
}
