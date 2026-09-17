package com.paymentprocessing.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.paymentprocessing.dto.authDto.AuthResponse;
import com.paymentprocessing.dto.authDto.LoginRequest;
import com.paymentprocessing.dto.authDto.RegisterRequest;
import com.paymentprocessing.entity.User;
import com.paymentprocessing.repository.UserRepository;
import com.paymentprocessing.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final JwtUtil jwtUtil;

	private final AuditLogService auditLogService;

	public String register(RegisterRequest request) {

		if (userRepository.findByEmail(request.getEmail()).isPresent()) {

		    throw new RuntimeException(
		            "Email already exists");
		}

		User user = User.builder().fullName(request.getFullName()).email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword())).role("USER").build();

		userRepository.save(user);

		auditLogService.saveAuditLog(user.getEmail(), "REGISTER", "SUCCESS", "User registered successfully");

		return "User Registered Successfully";
	}

	public AuthResponse login(LoginRequest request) {

		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() ->
		        new RuntimeException(
		                "Invalid Email"));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

			auditLogService.saveAuditLog(user.getEmail(), "LOGIN", "FAILED", "Invalid password");

			throw new RuntimeException("Invalid Password");
		}

		String token = jwtUtil.generateToken(user.getEmail());

		auditLogService.saveAuditLog(user.getEmail(), "LOGIN", "SUCCESS", "User logged in successfully");

		return new AuthResponse(token, "Login Successful");
	}
}