package com.paymentprocessing.exception;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {

	private LocalDateTime timestamp;

	private int status;

	private String message;
}