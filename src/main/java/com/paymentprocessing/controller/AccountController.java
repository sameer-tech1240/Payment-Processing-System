package com.paymentprocessing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
	public AccountResponse createAccount(@RequestBody CreateAccountRequest request) {

		return accountService.createAccount(request);
	}

	@GetMapping("/details/{accountNumber}")
	public AccountDetailsResponse getAccountDetails(@PathVariable("accountNumber") String accountNumber) {

		return accountService.getAccountDetails(accountNumber);
	}

	@GetMapping("/me")
	public AccountDetailsResponse getMyAccount(

			@RequestHeader("Authorization") String authHeader) {

		String token = authHeader.replace("Bearer ", "");

		return accountService.getLoggedInUserAccount(token);
	}

	@PutMapping("/profile")
	public String updateProfile(@RequestHeader("Authorization") String authHeader,
			@RequestBody UpdateProfileRequest request) {

		String token = authHeader.replace("Bearer ", "");
		accountService.updateProfile(token, request);
		return "Profile Updated Successfully";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestHeader("Authorization") String authHeader,
			@RequestBody UpdatePasswordRequest request) {
		String token = authHeader.replace("Bearer ", "");
		accountService.changePassword(token, request);
		return "Password Updated Successfully";
	}
}