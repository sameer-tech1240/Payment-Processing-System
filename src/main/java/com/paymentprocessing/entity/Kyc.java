package com.paymentprocessing.entity;

import java.time.LocalDateTime;

import com.paymentprocessing.enums.KycStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "kyc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kyc {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "user_id", unique = true)
	private User user;

	private String fullName;

	private String dateOfBirth;

	private String gender;

	private String panNumber;

	private String aadhaarNumber;

	private String address;

	private String city;

	private String state;

	private String pincode;

	private String panDocument;

	private String addressDocument;

	private String photo;

	@Enumerated(EnumType.STRING)
	private KycStatus status;

	private String rejectionReason;

	private LocalDateTime submittedAt;

	private LocalDateTime verifiedAt;

	private String verifiedBy;
}