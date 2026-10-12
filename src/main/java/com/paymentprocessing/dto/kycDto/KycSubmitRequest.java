package com.paymentprocessing.dto.kycDto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KycSubmitRequest {

	private String fullName;
	private String dateOfBirth;
	private String gender;
	private String panNumber;
	private String aadhaarNumber;
	private String address;
	private String city;
	private String state;
	private String pincode;

}