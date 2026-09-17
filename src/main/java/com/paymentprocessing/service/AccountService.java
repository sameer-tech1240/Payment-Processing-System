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
import com.paymentprocessing.entity.User;
import com.paymentprocessing.enums.AccountStatus;
import com.paymentprocessing.repository.BankAccountRepository;
import com.paymentprocessing.repository.UserRepository;
import com.paymentprocessing.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final UserRepository userRepository;

	private final BankAccountRepository bankAccountRepository;

	private final PasswordEncoder passwordEncoder;

	private final JwtUtil jwtUtil;

	public AccountResponse createAccount(CreateAccountRequest request) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (user.getBankAccount() != null) {
			throw new RuntimeException("Account already exists");

		}

		String accountNumber = generateAccountNumber();

		BankAccount bankAccount = BankAccount.builder().accountNumber(accountNumber).ifscCode("SBIN0001234")
				.balance(request.getInitialBalance()).accountType(request.getAccountType())
				.nomineeName(request.getNomineeName()).mobileNumber(request.getMobileNumber())
				.accountStatus(AccountStatus.ACTIVE).user(user).build();

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

		return AccountDetailsResponse.builder().fullName(account.getUser().getFullName())
				.email(account.getUser().getEmail()).accountNumber(account.getAccountNumber())
				.ifscCode(account.getIfscCode()).balance(account.getBalance())
				.accountStatus(account.getAccountStatus().name()).mobileNumber(account.getMobileNumber())
				.nomineeName(account.getNomineeName()).build();
	}

	public AccountDetailsResponse getLoggedInUserAccount(String token) {

		String email = jwtUtil.extractEmail(token);

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		if (user.getBankAccount() == null) {
			System.out.println("hasAccount TRUE response");
			return AccountDetailsResponse.builder().hasAccount(false).userId(user.getId()).fullName(user.getFullName())
					.email(user.getEmail()).build();
		}

		BankAccount account = user.getBankAccount();
		AccountDetailsResponse response = AccountDetailsResponse.builder().hasAccount(true).userId(user.getId())
				.fullName(user.getFullName()).email(user.getEmail()).accountNumber(account.getAccountNumber())
				.ifscCode(account.getIfscCode()).balance(account.getBalance())
				.accountStatus(account.getAccountStatus().name()).mobileNumber(account.getMobileNumber())
				.nomineeName(account.getNomineeName()).build();

		System.out.println("HAS ACCOUNT = " + response.getHasAccount());

		return response;
	}

	public void updateProfile(String token, UpdateProfileRequest request) {

		String email = jwtUtil.extractEmail(token);
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
		BankAccount account = user.getBankAccount();
		account.setMobileNumber(request.getMobileNumber());
		account.setNomineeName(request.getNomineeName());
		bankAccountRepository.save(account);
	}

	public void changePassword(String token, UpdatePasswordRequest request) {

		String email = jwtUtil.extractEmail(token);
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
