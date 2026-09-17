package com.paymentprocessing.dto.authDto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String fullName;

    private String email;

    private String password;
}
