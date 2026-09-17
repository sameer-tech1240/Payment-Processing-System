package com.paymentprocessing.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentprocessing.dto.authDto.AuthResponse;
import com.paymentprocessing.dto.authDto.LoginRequest;
import com.paymentprocessing.dto.authDto.RegisterRequest;
import com.paymentprocessing.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public String register(@RequestBody RegisterRequest request) {

		return authService.register(request);
	}

	@PostMapping("/login")
	public AuthResponse login(@RequestBody LoginRequest request) {

		return authService.login(request);
	}
}