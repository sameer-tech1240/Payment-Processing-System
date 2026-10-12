package com.paymentprocessing.service;

import java.math.BigDecimal;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.paymentprocessing.dto.accountDto.AccountDetailsResponse;
import com.paymentprocessing.dto.accountDto.AccountResponse;
import com.paymentprocessing.dto.accountDto.CreateAccountRequest;
import com.paymentprocessing.dto.profileDto.UpdatePasswordRequest;
import com.paymentprocessing.dto.profileDto.UpdateProfileRequest;
import com.paymentprocessing.entity.BankAccount;
import com.paymentprocessing.entity.Kyc;
import com.paymentprocessing.entity.User;
import com.paymentprocessing.enums.AccountStatus;
import com.paymentprocessing.enums.KycStatus;
import com.paymentprocessing.repository.BankAccountRepository;
import com.paymentprocessing.repository.KycRepository;
import com.paymentprocessing.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final UserRepository userRepository;
	private final BankAccountRepository bankAccountRepository;
	private final PasswordEncoder passwordEncoder;
	private final KycRepository kycRepository;

	public AccountResponse createAccount(String email, CreateAccountRequest request) {

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		Kyc kyc = kycRepository.findByUser(user).orElseThrow(() -> new RuntimeException("KYC not submitted"));

		if (kyc.getStatus() != KycStatus.APPROVED) {
			throw new RuntimeException("Bank account can be created only after KYC approval");
		}

		if (user.getBankAccount() != null) {
			throw new RuntimeException("Account already exists");
		}

		if (request.getMobileNumber() == null || request.getMobileNumber().length() != 10) {

			throw new RuntimeException("Invalid Mobile Number");
		}

		if (request.getNomineeName() == null || request.getNomineeName().isBlank()) {

			throw new RuntimeException("Nominee Name Required");
		}

		if ("SAVINGS".equalsIgnoreCase(request.getAccountType())
				&& request.getInitialBalance().compareTo(BigDecimal.valueOf(1000)) < 0) {

			throw new RuntimeException("Minimum balance for Savings Account is ₹1000");
		}

		if ("CURRENT".equalsIgnoreCase(request.getAccountType())
				&& request.getInitialBalance().compareTo(BigDecimal.valueOf(5000)) < 0) {

			throw new RuntimeException("Minimum balance for Current Account is ₹5000");
		}

		String accountNumber = generateAccountNumber();

		BankAccount bankAccount = BankAccount.builder().accountNumber(accountNumber).ifscCode("SBIN0001234")
				.balance(request.getInitialBalance()).accountType(request.getAccountType())
				.nomineeName(request.getNomineeName()).mobileNumber(request.getMobileNumber())
				.accountStatus(AccountStatus.ACTIVE).user(user).build();

		bankAccountRepository.save(bankAccount);

		return AccountResponse.builder().accountNumber(accountNumber).ifscCode("SBIN0001234").accountStatus("ACTIVE")
				.message("Bank Account Created Successfully").build();
	}

	private String generateAccountNumber() {

		Random random = new Random();

		return "4587" + (10000000 + random.nextInt(90000000));
	}

	public AccountDetailsResponse getAccountDetails(String accountNumber) {

		BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new RuntimeException("Account not found"));

		return AccountDetailsResponse.builder().hasAccount(true).userId(account.getUser().getId())
				.fullName(account.getUser().getFullName()).email(account.getUser().getEmail())
				.accountNumber(account.getAccountNumber()).ifscCode(account.getIfscCode()).balance(account.getBalance())
				.accountStatus(account.getAccountStatus().name()).mobileNumber(account.getMobileNumber())
				.nomineeName(account.getNomineeName()).build();
	}

	public AccountDetailsResponse getLoggedInUserAccount(String email) {

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		if (user.getBankAccount() == null) {

			return AccountDetailsResponse.builder().hasAccount(false).userId(user.getId()).fullName(user.getFullName())
					.email(user.getEmail()).build();
		}

		BankAccount account = user.getBankAccount();

		return AccountDetailsResponse.builder().hasAccount(true).userId(user.getId()).fullName(user.getFullName())
				.email(user.getEmail()).accountNumber(account.getAccountNumber()).ifscCode(account.getIfscCode())
				.balance(account.getBalance()).accountStatus(account.getAccountStatus().name())
				.mobileNumber(account.getMobileNumber()).nomineeName(account.getNomineeName()).build();
	}

	public void updateProfile(String email, UpdateProfileRequest request) {

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		BankAccount account = user.getBankAccount();

		if (account == null) {
			throw new RuntimeException("Bank account not found");
		}

		account.setMobileNumber(request.getMobileNumber());
		account.setNomineeName(request.getNomineeName());

		bankAccountRepository.save(account);
	}

	public void changePassword(String email, UpdatePasswordRequest request) {

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {

			throw new RuntimeException("Current Password Incorrect");
		}

		if (!request.getNewPassword().equals(request.getConfirmPassword())) {

			throw new RuntimeException("Passwords do not match");
		}

		if (request.getNewPassword().length() < 6) {
			throw new RuntimeException("Password must be at least 6 characters");
		}

		user.setPassword(passwordEncoder.encode(request.getNewPassword()));

		userRepository.save(user);
	}
}