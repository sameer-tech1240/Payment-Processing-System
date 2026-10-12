package com.paymentprocessing.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.paymentprocessing.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authorizationHeader = request.getHeader("Authorization");

		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authorizationHeader.substring(7);

		try {

			String email = jwtUtil.extractEmail(token);
			String role = jwtUtil.extractRole(token);

			if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email,
						null, Collections.singletonList(authority));

				SecurityContextHolder.getContext().setAuthentication(authentication);
			}

		} catch (Exception e) {
			// Invalid/expired JWT
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}
}