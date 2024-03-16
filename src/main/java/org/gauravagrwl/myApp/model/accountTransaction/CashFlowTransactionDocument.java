package org.gauravagrwl.myApp.model.accountTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.gauravagrwl.myApp.model.audit.AuditMetadata;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Document(collection = "cashflow_document")
@RequiredArgsConstructor(onConstructor = @__(@PersistenceCreator))
public class CashFlowTransactionDocument {

    @MongoId
    private String id;

    private LocalDate transactionDate; // Date Of Transactions

    private String description;

    private String transactionType; // CashIn or CashOut

    private BigDecimal cashIn;

    private BigDecimal cashOut;

    private Boolean reconciled;

    private Boolean duplicate;

    private String accountTransactionId; // Which Account Transactions

    private AuditMetadata audit = new AuditMetadata();

    @Version
    private Integer version;

}
