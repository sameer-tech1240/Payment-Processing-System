package com.paymentprocessing.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentprocessing.dto.statementDto.StatementRequest;
import com.paymentprocessing.dto.statementDto.StatementResponse;
import com.paymentprocessing.service.AuditLogService;
import com.paymentprocessing.service.PdfGeneratorService;
import com.paymentprocessing.service.StatementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/statement")
@RequiredArgsConstructor
public class StatementController {

	private final StatementService statementService;

	private final PdfGeneratorService pdfGeneratorService;

	private final AuditLogService auditLogService;

	@PostMapping("/generate")
	public StatementResponse generateStatement(@RequestBody StatementRequest request) {

		return statementService.generateStatement(request);
	}

	@PostMapping("/download")
	public ResponseEntity<byte[]> downloadStatement(@RequestBody StatementRequest request) {

		StatementResponse statement = statementService.generateStatement(request);

		byte[] pdf = pdfGeneratorService.generateStatementPdf(statement);

		auditLogService.saveAuditLog(request.getAccountNumber(), "STATEMENT_DOWNLOAD", "SUCCESS",
				"Statement downloaded");

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=statement.pdf")
				.contentType(MediaType.APPLICATION_PDF).body(pdf);
	}

	@GetMapping("/test-pdf")
	public String testPdf() {

		return "PDF Service Ready";
	}
}