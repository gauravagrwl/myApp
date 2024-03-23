package org.gauravagrwl.myApp.model.profileAccount.accountStatement;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.gauravagrwl.myApp.helper.CsvAmountStringToBigDecimalConverter;
import org.gauravagrwl.myApp.helper.CsvMDYDateStringToDateConverter;
import org.springframework.data.annotation.Transient;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByNames;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvCustomBindByNames;
import com.opencsv.bean.HeaderColumnNameMappingStrategyBuilder;
import com.opencsv.bean.MappingStrategy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountStatementDocument extends AccountStatementDocument {
	// Chase - chk / sav: Details Posting Date Description Amount Type Balance Check
	// or Slip #
	// AMEX: Date Description Amount Extended Details Appears On Your Statement As
	// Address City/State Zip Code Country Reference Category

	@CsvCustomBindByNames({
			@CsvCustomBindByName(column = "Posting Date", converter = CsvMDYDateStringToDateConverter.class, profiles = {
					"SAV", "CHK" }), })
	private LocalDate transactionDate; // Date Of Transactions private LocalDate postingDate; // Date Of Posting

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

	// private String notes; // contents memo, slip and category
	// private String category; // Chase credit card
	// Combined to one fields notes
	// private String memo; // Memo
	private String slip, referenceNo, notes;

	public static MappingStrategy<BankAccountStatementDocument> getHeaderColumnNameMappingStrategy(
			String profileType) {
		MappingStrategy<BankAccountStatementDocument> headerColumnNameMappingStrategy = new HeaderColumnNameMappingStrategyBuilder<BankAccountStatementDocument>()
				.withForceCorrectRecordLength(true).build();
		headerColumnNameMappingStrategy.setProfile(profileType);
		headerColumnNameMappingStrategy.setType(BankAccountStatementDocument.class);
		return headerColumnNameMappingStrategy;
	}

}
// SAV - debit goes in to the account
// SAV - Credit goes out of the account
// CRE - debits goes out of the account
// CRE - Credit goes in the account
