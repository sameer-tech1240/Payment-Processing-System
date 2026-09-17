package com.paymentprocessing.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.paymentprocessing.dto.statementDto.StatementResponse;
import com.paymentprocessing.dto.transactionDto.TransactionHistoryResponse;

@Service
public class PdfGeneratorService {

	private static final Color PRIMARY = new Color(15, 76, 129);

	private static final Color SECONDARY = new Color(220, 53, 69);

	private static final Color SUCCESS = new Color(25, 135, 84);

	private static final Color LIGHT = new Color(248, 249, 250);

	private static final Color BORDER = new Color(222, 226, 230);

	public byte[] generateStatementPdf(StatementResponse statement) {

		try {

			Document document = new Document(PageSize.A4, 20, 20, 20, 20);

			ByteArrayOutputStream out = new ByteArrayOutputStream();

			PdfWriter writer = PdfWriter.getInstance(document, out);

			String password = getPdfPassword(statement.getAccountNumber());

			writer.setEncryption(password.getBytes(), password.getBytes(), PdfWriter.ALLOW_PRINTING,
					PdfWriter.ENCRYPTION_AES_128);

			document.open();

			try {

				Image logo = Image.getInstance("src/main/resources/static/logo.png");

				logo.scaleToFit(120, 60);

				logo.setAlignment(Element.ALIGN_CENTER);

				document.add(logo);

			} catch (Exception ex) {

				System.out.println("Logo not found");
			}

			addHeader(document);

			addCustomerSection(document, statement);

			addAccountSummary(document, statement);

			addTransactionSection(document, statement);

			addFooter(document);

			document.close();

			return out.toByteArray();

		} catch (Exception ex) {

			ex.printStackTrace();

			throw new RuntimeException("PDF generation failed : " + ex.getMessage());
		}
	}

	private void addHeader(Document document) throws Exception {

		Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.WHITE);

		Font subTitle = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.WHITE);

		PdfPTable table = new PdfPTable(1);

		table.setWidthPercentage(100);

		PdfPCell cell = new PdfPCell();

		cell.setBackgroundColor(PRIMARY);

		cell.setBorder(Rectangle.NO_BORDER);

		cell.setPadding(15);

		Phrase phrase = new Phrase();

		phrase.add(new com.lowagie.text.Chunk("PAYMENT PROCESSING BANK\n", titleFont));

		phrase.add(new com.lowagie.text.Chunk("Official Account Statement", subTitle));

		cell.addElement(phrase);

		table.addCell(cell);

		document.add(table);
	}

	private void addCustomerSection(Document document, StatementResponse statement) throws Exception {

		Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);

		Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

		PdfPTable table = new PdfPTable(2);

		table.setWidthPercentage(100);

		table.setSpacingBefore(15);

		table.setWidths(new float[] { 30, 70 });

		PdfPCell title = new PdfPCell(new Phrase("CUSTOMER DETAILS", headerFont));

		title.setColspan(2);

		title.setBackgroundColor(SECONDARY);

		title.setPadding(8);

		title.setBorder(Rectangle.NO_BORDER);

		table.addCell(title);

		addDetailRow(table, "Customer Name", statement.getCustomerName(), labelFont);

		addDetailRow(table, "Account Number", statement.getAccountNumber(), labelFont);

		addDetailRow(table, "Account Type", statement.getAccountType(), labelFont);

		addDetailRow(table, "IFSC Code", statement.getIfscCode(), labelFont);

		addDetailRow(table, "Email", statement.getEmail(), labelFont);

		addDetailRow(table, "Mobile", statement.getMobileNumber(), labelFont);

		addDetailRow(table, "Status", statement.getAccountStatus(), labelFont);

		addDetailRow(table, "Statement Period", statement.getStatementPeriod(), labelFont);

		document.add(table);
	}

	private void addAccountSummary(Document document, StatementResponse statement) throws Exception {

		Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);

		PdfPTable table = new PdfPTable(2);

		table.setWidthPercentage(100);

		table.setSpacingBefore(15);

		PdfPCell title = new PdfPCell(new Phrase("ACCOUNT SUMMARY", headerFont));

		title.setColspan(2);

		title.setBackgroundColor(SUCCESS);

		title.setPadding(8);

		title.setBorder(Rectangle.NO_BORDER);

		table.addCell(title);

		addSummaryRow(table, "Opening Balance", "₹ " + statement.getOpeningBalance());

		addSummaryRow(table, "Total Credit", "₹ " + statement.getTotalCredit());

		addSummaryRow(table, "Total Debit", "₹ " + statement.getTotalDebit());

		addSummaryRow(table, "Closing Balance", "₹ " + statement.getClosingBalance());

		addSummaryRow(table, "Total Transactions", String.valueOf(statement.getTotalTransactions()));

		addSummaryRow(table, "Generated On",
				statement.getGeneratedOn().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));

		document.add(table);
	}

	private void addDetailRow(PdfPTable table, String label, String value, Font labelFont) {

		PdfPCell left = new PdfPCell(new Phrase(label, labelFont));

		left.setBackgroundColor(LIGHT);

		left.setPadding(6);

		PdfPCell right = new PdfPCell(new Phrase(value == null ? "-" : value));

		right.setPadding(6);

		table.addCell(left);

		table.addCell(right);
	}

	private void addSummaryRow(PdfPTable table, String label, String value) {

		PdfPCell left = new PdfPCell(new Phrase(label));

		left.setBackgroundColor(LIGHT);

		left.setPadding(6);

		PdfPCell right = new PdfPCell(new Phrase(value));

		right.setHorizontalAlignment(Element.ALIGN_RIGHT);

		right.setPadding(6);

		table.addCell(left);

		table.addCell(right);
	}

	private void addTransactionSection(Document document, StatementResponse statement) throws Exception {

		Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

		PdfPTable table = new PdfPTable(6);

		table.setWidthPercentage(100);

		table.setSpacingBefore(15);

		table.setWidths(new float[] { 18, 25, 12, 15, 12, 18 });

		addTableHeader(table, "Date", headerFont);

		addTableHeader(table, "Txn ID", headerFont);

		addTableHeader(table, "Type", headerFont);

		addTableHeader(table, "Amount", headerFont);

		addTableHeader(table, "Status", headerFont);

		addTableHeader(table, "Remarks", headerFont);

		for (TransactionHistoryResponse txn : statement.getTransactions()) {

			table.addCell(txn.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

			table.addCell(txn.getTransactionId());

			PdfPCell typeCell = new PdfPCell(new Phrase(txn.getTransactionDirection()));

			if ("CREDIT".equalsIgnoreCase(txn.getTransactionDirection())) {

				typeCell.setBackgroundColor(new Color(212, 237, 218));

			} else {

				typeCell.setBackgroundColor(new Color(248, 215, 218));
			}

			table.addCell(typeCell);

			PdfPCell amountCell = new PdfPCell(new Phrase("₹ " + txn.getAmount()));

			amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

			table.addCell(amountCell);

			PdfPCell statusCell = new PdfPCell(new Phrase(txn.getStatus()));

			if ("SUCCESS".equalsIgnoreCase(txn.getStatus())) {

				statusCell.setBackgroundColor(new Color(212, 237, 218));

			} else {

				statusCell.setBackgroundColor(new Color(255, 243, 205));
			}

			table.addCell(statusCell);

			table.addCell(txn.getRemarks() == null ? "-" : txn.getRemarks());
		}

		document.add(table);
	}

	private void addTableHeader(PdfPTable table, String text, Font font) {

		PdfPCell cell = new PdfPCell(new Phrase(text, font));

		cell.setBackgroundColor(PRIMARY);

		cell.setHorizontalAlignment(Element.ALIGN_CENTER);

		cell.setPadding(8);

		table.addCell(cell);
	}

	private void addFooter(Document document) throws Exception {

		PdfPTable footer = new PdfPTable(1);

		footer.setWidthPercentage(100);

		footer.setSpacingBefore(20);


		document.add(footer);
	}

	private String getPdfPassword(String accountNumber) {

		if (accountNumber == null || accountNumber.length() < 4) {

			return "1234";
		}

		return accountNumber.substring(accountNumber.length() - 4);
	}

}