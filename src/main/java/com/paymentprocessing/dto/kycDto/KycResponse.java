package com.paymentprocessing.dto.kycDto;


import java.time.LocalDateTime;

import com.paymentprocessing.enums.KycStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class KycResponse {

    private Long id;
    private String fullName;
    private KycStatus status;
    private String rejectionReason;
    private LocalDateTime submittedAt;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
}