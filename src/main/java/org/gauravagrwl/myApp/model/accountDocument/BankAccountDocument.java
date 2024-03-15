package org.gauravagrwl.myApp.model.accountDocument;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class supports below Institution Sub Category:
 * 
 * CHECKING(InstitutionCategoryEnum.BANKING, "CHK", "101"),
 * SAVING(InstitutionCategoryEnum.BANKING, "SAV", "102"),
 * DEPOSIT(InstitutionCategoryEnum.BANKING, "DEP", "103"),
 * PPF(InstitutionCategoryEnum.BANKING, "PPF", "104"),
 * CREDIT(InstitutionCategoryEnum.BANKING, "CRE", "105"),
 * 
 */

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class BankAccountDocument extends AccountDocument {

    // Account Calculated Balance
    private BigDecimal accountBalance = BigDecimal.ZERO;

    // Account holding type
    private String holdingType;

    // Account Code: Routing code or IIFC code.
    private String accountCode;

    // Code Type: Routing or IIFC
    private String accountCodeType;

    @Override
    public void calculate(BigDecimal amount) {
        this.accountBalance = amount;
    }

}
