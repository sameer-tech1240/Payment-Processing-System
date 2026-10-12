package com.paymentprocessing.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentprocessing.dto.kycDto.KycResponse;
import com.paymentprocessing.dto.kycDto.KycSubmitRequest;
import com.paymentprocessing.dto.kycDto.KycVerificationRequest;
import com.paymentprocessing.service.KycService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
public class KycController {

	private final KycService kycService;

	@PostMapping("/submit")
	public ResponseEntity<KycResponse> submitKyc(Authentication authentication, @RequestBody KycSubmitRequest request) {

		String email = authentication.getName();

		KycResponse response = kycService.submitKyc(email, request);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/status")
	public ResponseEntity<KycResponse> getKycStatus(Authentication authentication) {

		String email = authentication.getName();

		KycResponse response = kycService.getKycStatus(email);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/admin/pending")
	public ResponseEntity<List<KycResponse>> getPendingKyc() {

		return ResponseEntity.ok(kycService.getPendingKyc());
	}

	@PutMapping("/admin/verify/{kycId}")
	public ResponseEntity<KycResponse> verifyKyc(@PathVariable("kycId") Long kycId, Authentication authentication,
			@RequestBody KycVerificationRequest request) {

		String adminEmail = authentication.getName();

		KycResponse response = kycService.verifyKyc(kycId, request, adminEmail);

		return ResponseEntity.ok(response);
	}
}