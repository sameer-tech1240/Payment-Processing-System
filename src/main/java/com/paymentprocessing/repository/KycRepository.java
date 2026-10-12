package com.paymentprocessing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentprocessing.entity.Kyc;
import com.paymentprocessing.entity.User;
import com.paymentprocessing.enums.KycStatus;

public interface KycRepository extends JpaRepository<Kyc, Long> {

	Optional<Kyc> findByUser(User user);

	Optional<Kyc> findByUserId(Long userId);

	long countByStatus(KycStatus status);

	List<Kyc> findByStatus(KycStatus status);
}