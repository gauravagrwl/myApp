package org.gauravagrwl.myApp.model.profileAccount.accountDocument;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AssetsAccountDocument extends AccountDocument {

    // Total amount for the purchase.
    private BigDecimal amountInvestment;

    // Asset name
    private String propertyName;

    // Asset acquired Year
    private String acquiredYear;

    private boolean isRentable;

    private String tenantName;

    private String tenantNumber;

    @Override
    public void calculate(BigDecimal amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculate'");
    }

}
