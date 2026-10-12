package com.paymentprocessing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.paymentprocessing.security.JwtAuthFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.cors(Customizer.withDefaults())

				.csrf(csrf -> csrf.disable())

				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth

						// Public APIs
						.requestMatchers("/api/auth/register", "/api/auth/login").permitAll()

						// Admin APIs
						.requestMatchers("/api/kyc/admin/**").hasRole("ADMIN")

						.requestMatchers("/api/payment/admin/**").hasRole("ADMIN")

						// Logged-in users
						.requestMatchers("/api/kyc/**").authenticated()

						.requestMatchers("/api/account/**").authenticated()

						.requestMatchers("/api/payment/**").authenticated()

						.requestMatchers("/api/statement/**").authenticated()

						.requestMatchers("/api/audit/**").authenticated()

						// Everything else
						.anyRequest().authenticated())

				// JWT filter
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}