package com.paymentprocessing.dto.profileDto;

import lombok.Data;

@Data
public class UpdatePasswordRequest {

	private String currentPassword;

	private String newPassword;

	private String confirmPassword;
}