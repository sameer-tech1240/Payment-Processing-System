package com.paymentprocessing.dto.authDto;

import lombok.Data;

@Data
public class LoginRequest {

    private String email;

    private String password;
}