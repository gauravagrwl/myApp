package org.gauravagrwl.myApp.model;

import java.math.BigDecimal;
import java.util.Currency;

import org.gauravagrwl.myApp.helper.AccountTypeEnum;
import org.gauravagrwl.myApp.helper.InstitutionCategoryEnum;
import org.gauravagrwl.myApp.model.audit.AuditMetadata;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "account_document")
@JsonTypeInfo(use = Id.NAME, include = As.EXISTING_PROPERTY, property = "institutionCategory", defaultImpl = InstitutionCategoryEnum.class, visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BankAccountDocument.class, name = "BankAccount"),
        @JsonSubTypes.Type(value = InvestmentAccountDocument.class, name = "InvestmentAccount"),
        @JsonSubTypes.Type(value = LoanAccountDocument.class, name = "LoanAccount"),
        @JsonSubTypes.Type(value = AssetsAccountDocument.class, name = "AssetsAccount"),
})
public abstract class AccountDocument {

    @MongoId
    private String id;

    // Financial institute Name
    private String institutionName;

    // Financial institute Currency
    private Currency institutionCurrency;

    // Financial institute Type can be: BANKING, INVESTMENT, MARKET, LOAN
    private InstitutionCategoryEnum institutionCategory;

    // Financial institute Account Number must be unique
    @Indexed(unique = true, background = true)
    private String accountNumber;

    private AccountTypeEnum accountType;

    // User profile who holds the account
    private String profileDocumentId;

    // Document name which hold statement and ledger data of the account.
    private String statementCollectionName;

    // Is this account still Active.
    private Boolean isActive = Boolean.TRUE;

    private AuditMetadata audit = new AuditMetadata();

    @Version
    private Integer version;

    // Indicator if respective Account Balance Is Calculated.
    private Boolean isBalanceCalculated = Boolean.FALSE;

    public abstract void calculate(BigDecimal amount);

}
