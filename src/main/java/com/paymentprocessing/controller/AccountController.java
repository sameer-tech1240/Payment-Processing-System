package com.paymentprocessing.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentprocessing.dto.accountDto.AccountDetailsResponse;
import com.paymentprocessing.dto.accountDto.AccountResponse;
import com.paymentprocessing.dto.accountDto.CreateAccountRequest;
import com.paymentprocessing.dto.profileDto.UpdatePasswordRequest;
import com.paymentprocessing.dto.profileDto.UpdateProfileRequest;
import com.paymentprocessing.service.AccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

	private final AccountService accountService;

	@PostMapping("/create")
	public AccountResponse createAccount(Authentication authentication, @RequestBody CreateAccountRequest request) {

		String email = authentication.getName();

		return accountService.createAccount(email, request);
	}

	@GetMapping("/details/{accountNumber}")
	public AccountDetailsResponse getAccountDetails(@PathVariable("accountNumber") String accountNumber) {

		return accountService.getAccountDetails(accountNumber);
	}

	@GetMapping("/me")
	public AccountDetailsResponse getMyAccount(Authentication authentication) {

		String email = authentication.getName();

		return accountService.getLoggedInUserAccount(email);
	}

	@PutMapping("/profile")
	public String updateProfile(Authentication authentication, @RequestBody UpdateProfileRequest request) {

		String email = authentication.getName();

		accountService.updateProfile(email, request);

		return "Profile Updated Successfully";
	}

	@PostMapping("/change-password")
	public String changePassword(Authentication authentication, @RequestBody UpdatePasswordRequest request) {

		String email = authentication.getName();

		accountService.changePassword(email, request);

		return "Password Updated Successfully";
	}
}