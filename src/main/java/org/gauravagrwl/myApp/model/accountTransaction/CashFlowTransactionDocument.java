package org.gauravagrwl.myApp.model.accountTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.gauravagrwl.myApp.model.audit.AuditMetadata;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.Data;

@Data
@Document(collection = "cashflow_document")
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

    private String statementDocumentId; // Which Account Transactions

    private AuditMetadata audit = new AuditMetadata();

    @Version
    private Integer version;

}
