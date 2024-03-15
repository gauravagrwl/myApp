package org.gauravagrwl.myApp.model.accountTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;

import org.gauravagrwl.myApp.helper.CsvAmountStringToBigDecimalConverter;
import org.gauravagrwl.myApp.helper.CsvMDYDateStringToDateConverter;
import org.gauravagrwl.myApp.model.audit.AuditMetadata;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByNames;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvCustomBindByNames;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "transactions_document")
public class BankAccountTransactionDocument {
	// Chase - chk / sav: Details Posting Date Description Amount Type Balance Check
	// or Slip #
	// AMEX: Date Description Amount Extended Details Appears On Your Statement As
	// Address City/State Zip Code Country Reference Category

	@MongoId
	private String id;

	private Long Sno = 0L;
	@CsvCustomBindByNames({
			@CsvCustomBindByName(column = "Posting Date", converter = CsvMDYDateStringToDateConverter.class, profiles = {
					"SAV", "CHK" }), })
	private LocalDate transactionDate; // Date Of Transactions

	private LocalDate postingDate; // Date Of Posting

	@CsvBindByNames({ @CsvBindByName, @CsvBindByName(column = "Description", profiles = { "SAV", "CHK" }), })
	private String descriptions; // Descriptions

	@CsvBindByNames({ @CsvBindByName, @CsvBindByName(column = "Details", profiles = { "SAV", "CHK" }), })
	private String type; // If Dr., Cr.

	private BigDecimal debit = BigDecimal.ZERO; // Amount
	private BigDecimal credit = BigDecimal.ZERO; // Amount
	private BigDecimal balance = BigDecimal.ZERO; // Need to be calculated

	@CsvCustomBindByNames({
			@CsvCustomBindByName(column = "Amount", converter = CsvAmountStringToBigDecimalConverter.class, profiles = {
					"SAV", "CHK" }), })
	@Transient
	private BigDecimal transient_amount = BigDecimal.ZERO;

	@Indexed
	private boolean reconciled = Boolean.FALSE;

	private boolean duplicate = Boolean.FALSE;
	// private String notes; // contents memo, slip and category
	// private String category; // Chase credit card
	// Combined to one fields notes
	// private String memo; // Memo
	private String slip, referenceNo, notes;

	@ReadOnlyProperty
	@DocumentReference(lookup = "{'transactionDocumentId':?#{#self._id} }", lazy = true)
	private CashFlowTransactionDocument cashFlowTransactionId;

	public static Comparator<BankAccountTransactionDocument> sortBankStatment = Comparator
			.comparing(BankAccountTransactionDocument::getTransactionDate)
			.thenComparing(BankAccountTransactionDocument::getType);

	public static Comparator<BankAccountTransactionDocument> sortBankStatmentBySerialNumber = Comparator
			.comparingLong(BankAccountTransactionDocument::getSno);

	private String accountDocumentId;

	private AuditMetadata audit = new AuditMetadata();

	@Version
	private Integer version;
}
// SAV - debit goes in to the account
// SAV - Credit goes out of the account
// CRE - debits goes out of the account
// CRE - Credit goes in the account
