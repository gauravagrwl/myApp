package org.gauravagrwl.myApp.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;

import org.gauravagrwl.myApp.helper.CsvDateStringToDateConverter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByNames;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvCustomBindByNames;
import com.opencsv.bean.CsvDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "account_transactions")
public class AccountTransactionDocument {

	@MongoId
	private String id;

	private Long Sno = 0L;
	@CsvCustomBindByNames({
			@CsvCustomBindByName(column = "Posting Date", converter = CsvDateStringToDateConverter.class, profiles = {
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

	// @ReadOnlyProperty
	// @DocumentReference(lookup = "{'bankStatemenId':?#{#self._id} }", lazy = true)
	// private CashFlowStatement cashflowId;

	public static Comparator<AccountTransactionDocument> sortBankStatment = Comparator
			.comparing(AccountTransactionDocument::getTransactionDate)
			.thenComparing(AccountTransactionDocument::getType);

	public static Comparator<AccountTransactionDocument> sortBankStatmentBySerialNumber = Comparator
			.comparingLong(AccountTransactionDocument::getSno);

	private String accountDocumentId;
}
// SAV - debit goes in to the account
// SAV - Credit goes out of the account
// CRE - debits goes out of the account
// CRE - Credit goes in the account
