package org.gauravagrwl.myApp.model.profileAccount.accountDocument;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class LoanAccountDocument extends AccountDocument {

	private BigDecimal loanAmount;
	private BigDecimal remaingAmount;
	private BigDecimal amountPaid;
	private String rateOfIntrest;
	private String purpose;
	private String remaingInstallment;

	@Override
	public void calculate(BigDecimal amount) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'calculate'");
	}
}
