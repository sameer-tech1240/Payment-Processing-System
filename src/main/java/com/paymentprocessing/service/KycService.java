package com.paymentprocessing.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.paymentprocessing.dto.kycDto.KycResponse;
import com.paymentprocessing.dto.kycDto.KycSubmitRequest;
import com.paymentprocessing.dto.kycDto.KycVerificationRequest;
import com.paymentprocessing.entity.Kyc;
import com.paymentprocessing.entity.User;
import com.paymentprocessing.enums.KycStatus;
import com.paymentprocessing.repository.KycRepository;
import com.paymentprocessing.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KycService {

	private final KycRepository kycRepository;
	private final UserRepository userRepository;

	public KycResponse submitKyc(String email, KycSubmitRequest request) {

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		Kyc kyc = kycRepository.findByUser(user).orElse(null);

		if (kyc != null && kyc.getStatus() == KycStatus.PENDING) {
			throw new RuntimeException("KYC is already pending");
		}

		if (kyc != null && kyc.getStatus() == KycStatus.APPROVED) {
			throw new RuntimeException("KYC is already approved");
		}

		if (kyc == null) {
			kyc = new Kyc();
			kyc.setUser(user);
		}

		kyc.setFullName(request.getFullName());
		kyc.setDateOfBirth(request.getDateOfBirth());
		kyc.setGender(request.getGender());
		kyc.setPanNumber(request.getPanNumber());
		kyc.setAadhaarNumber(request.getAadhaarNumber());
		kyc.setAddress(request.getAddress());
		kyc.setCity(request.getCity());
		kyc.setState(request.getState());
		kyc.setPincode(request.getPincode());

		kyc.setStatus(KycStatus.PENDING);
		kyc.setRejectionReason(null);
		kyc.setSubmittedAt(LocalDateTime.now());
		kyc.setVerifiedAt(null);
		kyc.setVerifiedBy(null);

		Kyc savedKyc = kycRepository.save(kyc);

		return mapToResponse(savedKyc);
	}

	public KycResponse getKycStatus(String email) {

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		Kyc kyc = kycRepository.findByUser(user).orElse(null);

		if (kyc == null) {
			return KycResponse.builder().status(KycStatus.NOT_SUBMITTED).build();
		}

		return mapToResponse(kyc);
	}

	public List<KycResponse> getPendingKyc() {

		List<Kyc> kycList = kycRepository.findByStatus(KycStatus.PENDING);

		return kycList.stream().map(this::mapToResponse).toList();
	}

	public KycResponse verifyKyc(Long kycId, KycVerificationRequest request, String adminEmail) {

		Kyc kyc = kycRepository.findById(kycId).orElseThrow(() -> new RuntimeException("KYC not found"));

		if (kyc.getStatus() != KycStatus.PENDING) {
			throw new RuntimeException("KYC is not in pending status");
		}

		if (request.isApproved()) {

			kyc.setStatus(KycStatus.APPROVED);
			kyc.setRejectionReason(null);

		} else {

			if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {

				throw new RuntimeException("Rejection reason is required");
			}

			kyc.setStatus(KycStatus.REJECTED);
			kyc.setRejectionReason(request.getRejectionReason());
		}

		kyc.setVerifiedAt(LocalDateTime.now());
		kyc.setVerifiedBy(adminEmail);

		Kyc savedKyc = kycRepository.save(kyc);

		return mapToResponse(savedKyc);
	}

	private KycResponse mapToResponse(Kyc kyc) {

		return KycResponse.builder().id(kyc.getId()).fullName(kyc.getFullName()).status(kyc.getStatus())
				.rejectionReason(kyc.getRejectionReason()).submittedAt(kyc.getSubmittedAt())
				.verifiedAt(kyc.getVerifiedAt()).verifiedBy(kyc.getVerifiedBy()).build();
	}
}